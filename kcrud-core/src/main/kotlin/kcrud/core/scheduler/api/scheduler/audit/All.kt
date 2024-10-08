/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.scheduler.api.scheduler.audit

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.core.scheduler.api.SchedulerRouteAPI
import kcrud.core.scheduler.audit.AuditService
import kcrud.core.scheduler.model.audit.AuditLog

/**
 * Returns all existing audit logs for the scheduler.
 */
@SchedulerRouteAPI
internal fun Route.schedulerAllAuditRoute() {
    /**
     * Returns all existing audit logs for the scheduler.
     * @OpenAPITag Scheduler - Maintenance
     */
    get("scheduler/audit") {
        val audit: List<AuditLog> = AuditService.findAll()
        call.respond(status = HttpStatusCode.OK, message = audit)
    }
}
