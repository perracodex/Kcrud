/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.tasks.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.entity.TaskStateChangeEntity
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Resume a concrete scheduler task.
 */
fun Route.resumeSchedulerTaskRoute() {
    // Resume a concrete scheduled task.
    post("{name}/{group}/resume") {
        val name: String = call.parameters["name"]!!
        val group: String = call.parameters["group"]!!
        val state: TaskStateChangeEntity = SchedulerService.tasks.resume(name = name, group = group)
        call.respond(status = HttpStatusCode.OK, message = state)
    }
}
