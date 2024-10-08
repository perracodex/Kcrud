/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.plugins

import io.ktor.server.application.*
import kcrud.core.database.plugin.DbPlugin
import kcrud.core.database.schema.admin.actor.ActorTable
import kcrud.core.database.schema.admin.rbac.RbacFieldRuleTable
import kcrud.core.database.schema.admin.rbac.RbacRoleTable
import kcrud.core.database.schema.admin.rbac.RbacScopeRuleTable
import kcrud.core.database.schema.contact.ContactTable
import kcrud.core.database.schema.employee.EmployeeTable
import kcrud.core.database.schema.employment.EmploymentTable
import kcrud.core.database.schema.scheduler.SchedulerAuditTable
import kcrud.core.env.Telemetry

/**
 * Configures the custom [DbPlugin].
 *
 * This will set up and configure database, including the connection pool, and register
 * the database schema tables so that the ORM can interact with them.
 *
 * @see [DbPlugin]
 */
public fun Application.configureDatabase() {

    install(plugin = DbPlugin) {
        telemetryRegistry = Telemetry.registry

        tables.addAll(
            listOf(
                // System tables.
                RbacFieldRuleTable,
                RbacScopeRuleTable,
                RbacRoleTable,
                ActorTable,

                // Domain tables
                ContactTable,
                EmployeeTable,
                EmploymentTable,

                // Scheduler tables.
                SchedulerAuditTable
            )
        )
    }
}
