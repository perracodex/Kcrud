/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.domain.employee.routing.endpoints

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUUID
import kcrud.domain.employee.entity.EmployeeEntity
import kcrud.domain.employee.entity.EmployeeRequest
import kcrud.domain.employee.errors.EmployeeError
import kcrud.domain.employee.routing.annotation.EmployeeRouteAPI
import kcrud.domain.employee.service.EmployeeService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import java.util.*

@EmployeeRouteAPI
internal fun Route.updateEmployeeById() {
    // Update an employee by ID.
    put<EmployeeRequest> { employeeRequest ->
        val employeeId: UUID = call.parameters["employee_id"].toUUID()

        val sessionContext: SessionContext? = call.principal<SessionContext>()
        val service: EmployeeService = call.scope.get<EmployeeService> { parametersOf(sessionContext) }
        val updatedEmployee: EmployeeEntity? = service.update(
            employeeId = employeeId,
            employeeRequest = employeeRequest
        )

        if (updatedEmployee == null) {
            EmployeeError.EmployeeNotFound(employeeId = employeeId).raise()
        } else {
            call.respond(status = HttpStatusCode.OK, message = updatedEmployee)
        }
    }
}
