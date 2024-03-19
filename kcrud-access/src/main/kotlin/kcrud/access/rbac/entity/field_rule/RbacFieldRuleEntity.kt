/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.access.rbac.entity.field_rule

import kcrud.base.database.schema.admin.rbac.RbacFieldRuleTable
import kcrud.base.database.schema.admin.rbac.types.RbacAccessLevel
import kcrud.base.database.schema.admin.rbac.types.RbacResource
import kcrud.base.persistence.entity.Meta
import kcrud.base.persistence.serializers.SUUID
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Represents a single RBAC field level rule.
 *
 * @property id The unique id of the field rule record.
 * @property resourceRuleId The associated parent [RbacResource] id.
 * @property fieldName The name of the field being targeted.
 * @property accessLevel The field [RbacAccessLevel].
 * @property meta The metadata of the record.
 */
@Serializable
data class RbacFieldRuleEntity(
    val id: SUUID,
    val resourceRuleId: SUUID,
    val fieldName: String,
    val accessLevel: RbacAccessLevel,
    val meta: Meta
) {
    companion object {
        fun from(row: ResultRow): RbacFieldRuleEntity {
            return RbacFieldRuleEntity(
                id = row[RbacFieldRuleTable.id],
                resourceRuleId = row[RbacFieldRuleTable.resourceRuleId],
                fieldName = row[RbacFieldRuleTable.fieldName],
                accessLevel = row[RbacFieldRuleTable.accessLevel],
                meta = Meta.toEntity(row = row, table = RbacFieldRuleTable)
            )
        }
    }
}
