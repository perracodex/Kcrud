/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.scheduling.listener

import kcrud.base.env.Tracer
import kcrud.base.scheduling.annotation.JobSchedulerAPI
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener

@JobSchedulerAPI
class KcrudJobListener : JobListener {
    private val tracer = Tracer<KcrudJobListener>()

    override fun getName() = KcrudJobListener::class.simpleName

    override fun jobToBeExecuted(context: JobExecutionContext) {
        tracer.debug("Job to be executed: ${context.jobDetail.key}")
    }

    override fun jobExecutionVetoed(context: JobExecutionContext) {
        tracer.debug("Job execution vetoed: ${context.jobDetail.key}")
    }

    override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?) {
        tracer.debug("Job executed: ${context.jobDetail.key}")
    }
}
