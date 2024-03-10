/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduler.service

import io.ktor.server.application.*
import kcrud.base.infrastructure.utils.Tracer
import kcrud.base.scheduler.annotation.JobSchedulerAPI
import kcrud.base.scheduler.entities.JobScheduleEntity
import kcrud.base.scheduler.listeners.KcrudJobListener
import kcrud.base.scheduler.listeners.KcrudTriggerListener
import kcrud.base.security.service.AuthenticationTokenService
import kcrud.base.settings.AppSettings
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import java.io.InputStream
import java.util.*

/**
 * Configures the job scheduler for chron jobs and scheduling tasks.
 *
 * Although tasks can be done via coroutines, the job scheduler is a more robust solution for
 * tasks that need to be executed at specific times or intervals, or need to ensure execution
 * even if the server is restarted.
 *
 * See: [Quartz Scheduler Documentation](https://github.com/quartz-scheduler/quartz/blob/main/docs/index.adoc)
 *
 * See: [Quartz Scheduler Configuration](https://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/configuration.html)
 */
object JobSchedulerService {
    private val tracer = Tracer<AuthenticationTokenService>()

    private const val PROPERTIES_FILE = "quartz.properties"
    private val scheduler: Scheduler

    init {
        tracer.debug("Configuring the job scheduler.")

        // Load the configuration properties from the quartz.properties file.
        val schema: Properties = loadConfigurationFile()
        val dataSourceName: String = schema["org.quartz.jobStore.dataSource"].toString()

        // Set the database connection properties.
        schema["org.quartz.dataSource.$dataSourceName.driver"] = AppSettings.database.jdbcDriver
        schema["org.quartz.dataSource.$dataSourceName.URL"] = AppSettings.database.jdbcUrl
        schema["org.quartz.dataSource.$dataSourceName.user"] = AppSettings.database.username ?: ""
        schema["org.quartz.dataSource.$dataSourceName.password"] = AppSettings.database.password ?: ""
        schema["org.quartz.dataSource.$dataSourceName.maxConnections"] = AppSettings.database.connectionPoolSize

        // Create the scheduler and configure it with the properties.
        val schedulerFactory: SchedulerFactory = StdSchedulerFactory(schema)
        scheduler = schedulerFactory.scheduler

        tracer.debug("Job scheduler configured.")
    }

    /**
     * Loads the configuration properties from the quartz.properties file.
     */
    private fun loadConfigurationFile(): Properties {
        val properties = Properties()
        val inputStream: InputStream? = Thread.currentThread().contextClassLoader.getResourceAsStream(PROPERTIES_FILE)
        inputStream?.use { properties.load(it) }
        return properties
    }

    /**
     * Starts the job scheduler.
     */
    fun start() {
        tracer.info("Starting job scheduler.")
        hookListeners()
        scheduler.start()
        tracer.info("Job scheduler started.")
    }

    @OptIn(JobSchedulerAPI::class)
    private fun hookListeners() {
        scheduler.listenerManager.addJobListener(KcrudJobListener())
        scheduler.listenerManager.addTriggerListener(KcrudTriggerListener())
    }

    /**
     * Configures the job scheduler to shut down when the application is stopped.
     */
    fun configure(environment: ApplicationEnvironment) {
        // Add a shutdown hook to stop the scheduler when the application is stopped.
        environment.monitor.subscribe(ApplicationStopping) {
            tracer.info("Shutting down the scheduler.")
            scheduler.shutdown()
            tracer.info("Scheduler shut down.")
        }
    }

    /**
     * Stops the job scheduler.
     */
    fun stop() {
        scheduler.shutdown()
    }

    /**
     * Schedules a new job with the given trigger.
     */
    fun newJob(job: JobDetail, trigger: Trigger) {
        scheduler.scheduleJob(job, trigger)
    }

    /**
     * Deletes a job from the scheduler.
     */
    fun deleteJob(key: JobKey) {
        scheduler.deleteJob(key)
    }

    /**
     * Returns a snapshot list of all actual jobs currently scheduled in the job scheduler.
     */
    fun getJobs(): List<JobScheduleEntity> {
        return scheduler.getJobKeys(GroupMatcher.anyGroup()).map { jobKey ->
            val jobDetail: JobDetail = scheduler.getJobDetail(jobKey)

            JobScheduleEntity(
                name = jobKey.name,
                group = jobKey.group,
                className = jobDetail.jobClass.simpleName,
                description = jobDetail.description,
                isDurable = jobDetail.isDurable,
                shouldRecover = jobDetail.requestsRecovery(),
                dataMap = jobDetail.jobDataMap.toList().toString()
            )
        }
    }
}
