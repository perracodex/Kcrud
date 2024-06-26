/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.routing.scheduler

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.scheduler.service.core.SchedulerService

/**
 * Returns the state of the task scheduler.
 */
fun Route.schedulerStateRoute() {
    // Returns the state of the task scheduler.
    get("state") {
        val state: SchedulerService.TaskSchedulerState = SchedulerService.state()
        call.respond(status = HttpStatusCode.OK, message = state.name)
    }
}

