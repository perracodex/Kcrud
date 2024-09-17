/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.api.endpoints.delete

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUuid
import kcrud.domain.employee.api.annotation.EmployeeRouteAPI
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import kotlin.uuid.Uuid

@EmployeeRouteAPI
internal fun Route.deleteEmployeeByIdRoute() {
    /**
     * Deletes an employee by ID.
     * @OpenAPITag Employee
     */
    delete("v1/employees/{employee_id}") {
        val employeeId: Uuid = call.parameters.getOrFail(name = "employee_id").toUuid()

        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }

        val deletedCount: Int = service.delete(employeeId = employeeId)
        call.respond(status = HttpStatusCode.OK, message = deletedCount)
    }
}