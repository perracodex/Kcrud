/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.api

import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.api.scheduler.audit.schedulerAllAuditRoute
import kcrud.base.scheduler.api.scheduler.audit.schedulerAuditByTaskRoute
import kcrud.base.scheduler.api.scheduler.pauseSchedulerRoute
import kcrud.base.scheduler.api.scheduler.restartSchedulerRoute
import kcrud.base.scheduler.api.scheduler.resumeSchedulerRoute
import kcrud.base.scheduler.api.scheduler.schedulerStateRoute
import kcrud.base.scheduler.api.tasks.delete.deleteAllSchedulerTasksRoute
import kcrud.base.scheduler.api.tasks.delete.deleteSchedulerTaskRoute
import kcrud.base.scheduler.api.tasks.get.getSchedulerTaskGroupsRoute
import kcrud.base.scheduler.api.tasks.get.getSchedulerTasksRoute
import kcrud.base.scheduler.api.tasks.operate.pauseSchedulerTaskRoute
import kcrud.base.scheduler.api.tasks.operate.resendSchedulerTaskRoute
import kcrud.base.scheduler.api.tasks.operate.resumeSchedulerTaskRoute
import kcrud.base.scheduler.api.view.schedulerDashboardRoute

/**
 * Route administers all scheduled tasks, allowing to list and delete them.
 */
public fun Route.schedulerRoutes() {

    // Sets up the routing to serve resources as static content for the scheduler.
    staticResources(remotePath = "/templates/scheduler", basePackage = "/templates/scheduler")

    // Maintenance related routes.
    schedulerDashboardRoute()
    schedulerStateRoute()
    pauseSchedulerRoute()
    resumeSchedulerRoute()
    restartSchedulerRoute()
    schedulerAllAuditRoute()
    schedulerAuditByTaskRoute()

    // Task related routes.
    getSchedulerTasksRoute()
    getSchedulerTaskGroupsRoute()
    deleteSchedulerTaskRoute()
    deleteAllSchedulerTasksRoute()
    pauseSchedulerTaskRoute()
    resumeSchedulerTaskRoute()
    resendSchedulerTaskRoute()
}