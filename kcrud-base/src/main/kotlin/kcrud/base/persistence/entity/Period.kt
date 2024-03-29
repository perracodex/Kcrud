/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.persistence.entity

import kcrud.base.utils.KLocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

/**
 * Represents a time period.
 *
 * @property isActive Whether the period is currently active.
 * @property startDate The start date of the period.
 * @property endDate The end date of the period.
 */
@Serializable
data class Period(
    val isActive: Boolean,
    val startDate: KLocalDate,
    val endDate: KLocalDate? = null,
    val comments: String? = null
) {
    companion object {
        fun toEntity(row: ResultRow, table: Table): Period {
            val isActiveColumn: Column<*> = table.columns.single { it.name == "is_active" }
            val startDateColumn: Column<*> = table.columns.single { it.name == "start_date" }
            val endDateColumn: Column<*> = table.columns.single { it.name == "end_date" }
            val commentsColumn: Column<*> = table.columns.single { it.name == "comments" }

            return Period(
                isActive = row[isActiveColumn] as Boolean,
                startDate = row[startDateColumn] as KLocalDate,
                endDate = row[endDateColumn] as KLocalDate?,
                comments = row[commentsColumn] as String?
            )
        }
    }
}
