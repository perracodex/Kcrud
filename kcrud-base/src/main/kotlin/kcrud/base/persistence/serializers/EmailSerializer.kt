/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.persistence.serializers

import kcrud.base.persistence.validators.IValidator
import kcrud.base.persistence.validators.impl.EmailValidator
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for Email strings.
 */
object EmailStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "EmailString",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: String) {
        if (EmailValidator.validate(value = value) is IValidator.Result.Failure) {
            throw SerializationException(EmailValidator.message(text = value))
        }
        encoder.encodeString(value = value)
    }

    override fun deserialize(decoder: Decoder): String {
        val string: String = decoder.decodeString()
        if (EmailValidator.validate(value = string) is IValidator.Result.Failure) {
            throw SerializationException(EmailValidator.message(text = string))
        }
        return string
    }
}

/**
 * Represents a serializable Email String.
 *
 * @property EmailString The type representing the serializable Email.
 *
 * @see EmailStringSerializer
 */
typealias EmailString = @Serializable(with = EmailStringSerializer::class) String
