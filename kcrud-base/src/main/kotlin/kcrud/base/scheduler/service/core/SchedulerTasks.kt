/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.service.core

import it.burning.cron.CronExpressionDescriptor
import kcrud.base.env.Tracer
import kcrud.base.events.SEEService
import kcrud.base.scheduler.annotation.SchedulerAPI
import kcrud.base.scheduler.audit.AuditService
import kcrud.base.scheduler.audit.entity.AuditEntity
import kcrud.base.scheduler.entity.TaskScheduleEntity
import kcrud.base.scheduler.entity.TaskStateChangeEntity
import kcrud.base.scheduler.service.task.TaskState
import kcrud.base.security.snowflake.SnowflakeFactory
import kcrud.base.utils.DateTimeUtils
import org.quartz.*
import org.quartz.impl.matchers.GroupMatcher
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Helper object to manage tasks in the scheduler.
 */
@OptIn(SchedulerAPI::class)
class SchedulerTasks private constructor(private val scheduler: Scheduler) {
    private val tracer = Tracer<SchedulerService>()

    /**
     * Schedules a new task with the given trigger.
     */
    fun schedule(task: JobDetail, trigger: Trigger) {
        tracer.debug("Scheduling new task: $task. Trigger: $trigger.")
        scheduler.scheduleJob(task, trigger)
    }

    /**
     * Pauses a concrete task currently scheduled in the scheduler.
     *
     * @param name The name of the task to pause.
     * @param group The group of the task to pause.
     * @return [TaskStateChangeEntity] containing details of the operation.
     */
    fun pause(name: String, group: String): TaskStateChangeEntity {
        return TaskState.change(scheduler = scheduler, targetState = Trigger.TriggerState.PAUSED) {
            val jobKey: JobKey = JobKey.jobKey(name, group)
            scheduler.pauseJob(JobKey.jobKey(name, group))
            TaskState.getTriggerState(scheduler = scheduler, jobKey = jobKey).name
        }
    }

    /**
     * Resumes a concrete task currently scheduled in the scheduler.
     *
     * @param name The name of the task to resume.
     * @param group The group of the task to resume.
     * @return [TaskStateChangeEntity] containing details of the operation.
     */
    fun resume(name: String, group: String): TaskStateChangeEntity {
        return TaskState.change(scheduler = scheduler, targetState = Trigger.TriggerState.NORMAL) {
            val jobKey: JobKey = JobKey.jobKey(name, group)
            scheduler.resumeJob(jobKey)
            TaskState.getTriggerState(scheduler = scheduler, jobKey = jobKey).name
        }
    }

    /**
     * Deletes a task from the scheduler.
     *
     * @param name The name of the task to be deleted.
     * @param group The group of the task to be deleted.
     * @return The number of tasks deleted.
     */
    fun delete(name: String, group: String): Int {
        tracer.debug("Deleting task. Name: $name. Group: $group.")
        return if (scheduler.deleteJob(JobKey.jobKey(name, group))) 1 else 0
    }

    /**
     * Deletes all tasks from the scheduler.
     *
     * @return The number of tasks deleted.
     */
    fun deleteAll(): Int {
        tracer.debug("Deleting all tasks.")
        return scheduler.getJobKeys(GroupMatcher.anyGroup()).count { jobKey ->
            scheduler.deleteJob(jobKey)
        }
    }

    /**
     * Returns a list of all task groups currently scheduled in the scheduler.
     */
    fun groups(): List<String> {
        return scheduler.jobGroupNames
    }

    /**
     * Re-trigger an existing durable job.
     *
     * @param name The name of the task to re-trigger.
     * @param group The group of the task to re-trigger.
     * @throws IllegalArgumentException If the task is not found.
     */
    suspend fun resend(name: String, group: String) {
        val jobKey: JobKey = JobKey.jobKey(name, group)

        // Triggers require a unique identity.
        val identity = "$name-trigger-resend-${UUID.randomUUID()}"

        val triggerBuilder: TriggerBuilder<SimpleTrigger> = TriggerBuilder.newTrigger()
            .withIdentity(identity, group)
            .forJob(jobKey) // Associate with the existing durable job.
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withMisfireHandlingInstructionIgnoreMisfires()
            )

        val newTrigger: Trigger = triggerBuilder.build()

        // Schedule the new trigger with the existing job.
        scheduler.scheduleJob(newTrigger)

        SEEService.push(
            "Task resent. Name: $name | Group: $group"
        )

        tracer.debug("Trigger for task ${jobKey.name} has been scheduled.")
    }

    /**
     * Returns a snapshot list of all tasks currently scheduled in the scheduler.
     *
     * @param groupId Optional group ID to filter the tasks by.
     * @param executing True if only actively executing tasks should be returned; false to return all tasks.
     * @param groupId The group ID of the tasks to return. Null to return all tasks.
     * @return A list of [TaskScheduleEntity] objects representing the scheduled tasks.
     */
    suspend fun all(groupId: UUID? = null, executing: Boolean = false): List<TaskScheduleEntity> {
        var taskList: List<TaskScheduleEntity> = if (executing) {
            scheduler.currentlyExecutingJobs.map { task -> toEntity(taskDetail = task.jobDetail) }
        } else {
            scheduler.getJobKeys(GroupMatcher.anyGroup()).mapNotNull { jobKey ->
                scheduler.getJobDetail(jobKey)?.let { detail -> toEntity(taskDetail = detail) }
            }
        }

        if (groupId != null) {
            taskList = taskList.filter { it.group == groupId.toString() }
        }

        // Sort the task list by nextFireTime.
        // Tasks without a nextFireTime will be placed at the end of the list.
        return taskList.sortedBy { it.nextFireTime }
    }

    /**
     * Helper method to create a [TaskScheduleEntity] from a [JobDetail] including the next fire time.
     *
     * @param taskDetail The task detail from which to create the [TaskScheduleEntity].
     * @return The constructed [TaskScheduleEntity].
     */
    private suspend fun toEntity(taskDetail: JobDetail): TaskScheduleEntity {
        val jobKey: JobKey = taskDetail.key
        val triggers: List<Trigger> = scheduler.getTriggersOfJob(jobKey)

        // Get the most restrictive state from the list of trigger states.
        val mostRestrictiveState: Trigger.TriggerState = TaskState.getTriggerState(
            scheduler = scheduler,
            taskDetail = taskDetail
        )

        // Resolve the last execution outcome.
        val mostRecentAudit: AuditEntity? = AuditService.mostRecent(taskName = jobKey.name, taskGroup = jobKey.group)
        val outcome: String? = mostRecentAudit?.outcome?.name

        // Get how many times the task has been executed.
        val runs: Int = AuditService.count(taskName = jobKey.name, taskGroup = jobKey.group)

        // Resolve the schedule metrics.
        val (schedule: String?, scheduleInfo: String?) = triggers.firstOrNull()?.let { trigger ->
            if (trigger is SimpleTrigger) {
                val repeatInterval: Duration = trigger.repeatInterval.toDuration(unit = DurationUnit.MILLISECONDS)
                if (repeatInterval.inWholeSeconds != 0L) {
                    "Every $repeatInterval" to null
                } else null
            } else if (trigger is CronTrigger) {
                CronExpressionDescriptor.getDescription(trigger.cronExpression) to trigger.cronExpression
            } else null
        } ?: (null to null)

        // Resolve the concrete parameters of the task.
        val dataMap: List<String> = taskDetail.jobDataMap
            .entries.map { (key, value) -> "$key: $value" }
            .sorted()

        // Determine the next fire time of the task.
        val nextFireTime: Date? = triggers.mapNotNull { it.nextFireTime }.minOrNull()

        // Resolve the snowflake data.
        val snowflakeData: String = SnowflakeFactory.parse(id = jobKey.name).toString()

        return TaskScheduleEntity(
            name = jobKey.name,
            snowflakeData = snowflakeData,
            group = jobKey.group,
            consumer = taskDetail.jobClass.simpleName,
            nextFireTime = nextFireTime?.let { DateTimeUtils.javaDateToLocalDateTime(datetime = it) },
            state = mostRestrictiveState.name,
            outcome = outcome,
            log = mostRecentAudit?.log,
            schedule = schedule,
            scheduleInfo = scheduleInfo,
            runs = runs,
            dataMap = dataMap,
        )
    }

    companion object {
        @SchedulerAPI
        internal fun create(scheduler: Scheduler): SchedulerTasks {
            return SchedulerTasks(scheduler = scheduler)
        }
    }
}
