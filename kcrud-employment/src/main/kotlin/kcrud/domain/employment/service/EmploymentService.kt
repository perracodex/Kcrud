/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employment.service

import kcrud.base.env.SessionContext
import kcrud.base.env.Tracer
import kcrud.base.persistence.pagination.Page
import kcrud.base.persistence.pagination.Pageable
import kcrud.domain.employment.entity.EmploymentEntity
import kcrud.domain.employment.entity.EmploymentRequest
import kcrud.domain.employment.errors.EmploymentError
import kcrud.domain.employment.repository.IEmploymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Employment service, where all the employment business logic should be defined.
 */
class EmploymentService(
    @Suppress("unused") private val sessionContext: SessionContext,
    private val employmentRepository: IEmploymentRepository
) {
    private val tracer = Tracer<EmploymentService>()

    /**
     * Retrieves all employments.
     *
     * @param pageable The pagination options to be applied, or null for a single all-in-one page.
     * @return List of [EmploymentEntity] entities.
     */
    suspend fun findAll(pageable: Pageable? = null): Page<EmploymentEntity> = withContext(Dispatchers.IO) {
        return@withContext employmentRepository.findAll(pageable = pageable)
    }

    /**
     * Retrieves an employment by its ID.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @param employmentId The ID of the employment to be retrieved.
     * @return The resolved [EmploymentEntity] if found, null otherwise.
     */
    suspend fun findById(employeeId: UUID, employmentId: UUID): EmploymentEntity? = withContext(Dispatchers.IO) {
        return@withContext employmentRepository.findById(employeeId = employeeId, employmentId = employmentId)
    }

    /**
     * Retrieves all employment entities for a given employee.
     *
     * @param employeeId The ID of the employee associated with the employment.
     * @return List of [EmploymentEntity] entities.
     */
    suspend fun findByEmployeeId(employeeId: UUID): List<EmploymentEntity> = withContext(Dispatchers.IO) {
        return@withContext employmentRepository.findByEmployeeId(employeeId = employeeId)
    }

    /**
     * Creates a new employment.
     *
     * @param employeeId The employee ID associated with the employment.
     * @param employmentRequest The employment to be created.
     * @return The ID of the created employment.
     */
    suspend fun create(
        employeeId: UUID,
        employmentRequest: EmploymentRequest
    ): EmploymentEntity {
        tracer.debug("Creating employment for employee with ID: $employeeId")

        verify(
            employeeId = employeeId,
            employmentId = null,
            employmentRequest = employmentRequest,
            reason = "Create Employment."
        )

        val employmentId: UUID = employmentRepository.create(
            employeeId = employeeId,
            employmentRequest = employmentRequest
        )

        return withContext(Dispatchers.IO) {
            return@withContext findById(employeeId = employeeId, employmentId = employmentId)!!
        }
    }

    /**
     * Updates an employment's details.
     *
     * @param employeeId The employee ID associated with the employment.
     * @param employmentId The ID of the employment to be updated.
     * @param employmentRequest The new details for the employment.
     * @return The number of updated records.
     */
    suspend fun update(
        employeeId: UUID,
        employmentId: UUID,
        employmentRequest: EmploymentRequest
    ): EmploymentEntity? {
        tracer.debug("Updating employment with ID: $employmentId")

        verify(
            employeeId = employeeId,
            employmentId = employmentId,
            employmentRequest = employmentRequest,
            reason = "Update Employment."
        )

        return withContext(Dispatchers.IO) {
            val updateCount: Int = employmentRepository.update(
                employeeId = employeeId,
                employmentId = employmentId,
                employmentRequest = employmentRequest
            )

            return@withContext if (updateCount > 0) {
                findById(employeeId = employeeId, employmentId = employmentId)
            } else {
                null
            }
        }
    }

    /**
     * Deletes an employment using the provided ID.
     *
     * @param employmentId The ID of the employment to be deleted.
     * @return The number of delete records.
     */
    suspend fun delete(employmentId: UUID): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting employment with ID: $employmentId")
        return@withContext employmentRepository.delete(employmentId = employmentId)
    }

    /**
     * Deletes all an employments for the given employee ID.
     *
     * @param employeeId The ID of the employee to delete all its employments.
     * @return The number of delete records.
     */
    suspend fun deleteAll(employeeId: UUID): Int = withContext(Dispatchers.IO) {
        tracer.debug("Deleting all employments for employee with ID: $employeeId")
        return@withContext employmentRepository.deleteAll(employeeId = employeeId)
    }

    private fun verify(employeeId: UUID, employmentId: UUID?, employmentRequest: EmploymentRequest, reason: String) {
        // Verify that the employment period dates are valid.
        employmentRequest.period.endDate?.let { endDate ->
            if (endDate < employmentRequest.period.startDate) {
                throw EmploymentError.PeriodDatesMismatch(
                    employeeId = employeeId,
                    employmentId = employmentId,
                    startDate = employmentRequest.period.startDate,
                    endDate = endDate,
                    reason = reason
                )
            }
        }

        // Verify that the employment probation end date is valid.
        employmentRequest.probationEndDate?.let { probationEndDate ->
            if (probationEndDate < employmentRequest.period.startDate) {
                throw EmploymentError.InvalidProbationEndDate(
                    employeeId = employeeId,
                    employmentId = employmentId,
                    startDate = employmentRequest.period.startDate,
                    probationEndDate = probationEndDate,
                    reason = reason
                )
            }
        }
    }
}
