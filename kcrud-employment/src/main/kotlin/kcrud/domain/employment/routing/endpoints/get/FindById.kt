/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.routing.endpoints.get

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcrud.base.env.SessionContext
import kcrud.base.persistence.utils.toUUID
import kcrud.domain.employment.entity.EmploymentEntity
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.routing.annotation.EmploymentRouteAPI
import kcrud.domain.employment.service.EmploymentService
import org.koin.core.parameter.parametersOf
import org.koin.ktor.plugin.scope
import java.util.*

@EmploymentRouteAPI
internal fun Route.findEmploymentById() {
    // Find an employment by ID.
    get {
        val employeeId: UUID = call.parameters["employee_id"].toUUID()
        val employmentId: UUID = call.parameters["employment_id"].toUUID()

        val sessionContext: SessionContext? = SessionContext.from(call = call)
        val service: EmploymentService = call.scope.get<EmploymentService> { parametersOf(sessionContext) }
        val employment: EmploymentEntity? = service.findById(
            employeeId = employeeId,
            employmentId = employmentId
        )

        employment?.let {
            call.respond(status = HttpStatusCode.OK, message = employment)
        } ?: throw EmploymentError.EmploymentNotFound(employeeId = employeeId, employmentId = employmentId)
    }
}
