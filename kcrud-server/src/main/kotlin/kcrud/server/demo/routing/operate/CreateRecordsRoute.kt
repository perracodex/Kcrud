/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.server.demo.routing.operate

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.domain.employee.service.EmployeeService
import kcrud.domain.employment.service.EmploymentService
import kcrud.server.demo.DemoAPI
import kcrud.server.demo.DemoUtils
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope

@DemoAPI
internal fun Route.createRecordsRoute() {
    val maxAllowedBatch = 100_000

    // Create a batch of demo records.
    post {
        val count: Int? = call.request.queryParameters["count"]?.toIntOrNull()

        if (count != null && count in 1..maxAllowedBatch) {
            val sessionContext: SessionContext? = SessionContext.from(call = call)
            val employeeService: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
            val employmentService: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }

            DemoUtils.createDemoRecords(
                employeeService = employeeService,
                employmentService = employmentService,
                count = count
            )
            call.respond(status = HttpStatusCode.OK, "Created $count employees.")
        } else {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = "Invalid count. Must be between 1 and $maxAllowedBatch."
            )
        }
    }
}