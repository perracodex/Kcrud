/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.domain.employee.entity

import kcrud.base.database.custom_columns.validVarChar
import kcrud.base.database.schema.employee.types.Honorific
import kcrud.base.database.schema.employee.types.MaritalStatus
import kcrud.base.persistence.serializers.NoBlankString
import kcrud.base.utils.KLocalDate
import kcrud.domain.contact.entity.ContactRequest
import kcrud.domain.employee.errors.EmployeeError
import kotlinx.serialization.Serializable

/**
 * Represents the request to create/update an employee.
 *
 * This entity serves as example of how to use the [NoBlankString],
 * which is a typealias for a serializable String that cannot be blank.
 *
 * Note that the project also includes examples demonstrating how perform verifications
 * at service level or database field level, instead of using serializers.
 * Such validation variants can send to the client a more detailed error than
 * would do a serializer. See: [EmployeeError], [validVarChar]
 *
 * @property firstName The first name of the employee. Must not be blank.
 * @property lastName The last name of the employee. Must not be blank.
 * @property dob The date of birth of the employee.
 * @property maritalStatus The marital status of the employee.
 * @property honorific The honorific or title of the employee.
 * @property contact Optional contact details of the employee.
 */
@Serializable
data class EmployeeRequest(
    val firstName: NoBlankString,
    val lastName: NoBlankString,
    val dob: KLocalDate,
    val maritalStatus: MaritalStatus,
    val honorific: Honorific,
    val contact: ContactRequest? = null
)
