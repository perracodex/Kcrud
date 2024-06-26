/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.scheduler.audit

import kcrud.base.env.Tracer
import kcrud.base.scheduler.annotation.SchedulerAPI
import kcrud.base.scheduler.audit.entity.AuditEntity
import kcrud.base.scheduler.audit.entity.AuditRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Service to manage the persistence and retrieval of the scheduler audit logs.
 */
@OptIn(SchedulerAPI::class)
internal object AuditService {
    private val tracer = Tracer<AuditService>()

    /**
     * Creates a new audit entry.
     *
     * @param request The [AuditRequest] to create.
     */
    suspend fun create(request: AuditRequest): UUID = withContext(Dispatchers.IO) {
        tracer.debug("Creating a new audit entry for task '${request.taskName}' in group '${request.taskGroup}'.")
        return@withContext AuditRepository.create(request = request)
    }

    /**
     * Finds all the audit entries, ordered bby the most recent first.
     *
     * @return The list of [AuditEntity] instances.
     */
    suspend fun findAll(): List<AuditEntity> = withContext(Dispatchers.IO) {
        tracer.debug("Finding all audit entries.")
        return@withContext AuditRepository.findAll()
    }

    /**
     * Finds all the audit logs for a concrete task by name and group, ordered by the most recent first.
     *
     * @param taskName The name of the task.
     * @param taskGroup The group of the task.
     * @return The list of [AuditEntity] instances, or an empty list if none found.
     */
    suspend fun find(taskName: String, taskGroup: String): List<AuditEntity> = withContext(Dispatchers.IO) {
        tracer.debug("Finding all audit entries for task '$taskName' in group '$taskGroup'.")
        return@withContext AuditRepository.find(taskName = taskName, taskGroup = taskGroup)
    }

    /**
     * Finds the most recent audit log for a specific task.
     *
     * @param taskName The name of the task.
     * @param taskGroup The group of the task.
     * @return The most recent [AuditEntity] instance, or `null` if none found.
     */
    suspend fun mostRecent(taskName: String, taskGroup: String): AuditEntity? = withContext(Dispatchers.IO) {
        tracer.debug("Finding the most recent audit entry for task '$taskName' in group '$taskGroup'.")
        return@withContext AuditRepository.mostRecent(taskName = taskName, taskGroup = taskGroup)
    }

    /**
     * Returns the total count of audit entries for a specific task.
     *
     * @param taskName The name of the task.
     * @param taskGroup The group of the task.
     * @return The total count of audit entries for the task.
     */
    suspend fun count(taskName: String, taskGroup: String): Int = withContext(Dispatchers.IO) {
        tracer.debug("Counting the total audit entries for task '$taskName' in group '$taskGroup'.")
        return@withContext AuditRepository.count(taskName = taskName, taskGroup = taskGroup)
    }
}
