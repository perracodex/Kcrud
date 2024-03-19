/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.env.health.checks

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import kcrud.base.env.EnvironmentType
import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.settings.AppSettings
import kcrud.base.utils.NetworkUtils
import kotlinx.serialization.Serializable

@HealthCheckAPI
@Serializable
data class DeploymentCheck(
    val errors: MutableList<String>,
    val configured: Configured,
    val serverSpec: ServerSpec,
    val connectors: MutableMap<String, List<String>>,
) {
    constructor(call: ApplicationCall?) : this(
        errors = mutableListOf(),
        configured = Configured(),
        serverSpec = ServerSpec(call = call),
        connectors = getConnectors(call = call, errors = mutableListOf())
    )

    @Serializable
    data class Configured(
        val protocol: String = NetworkUtils.getProtocol().name,
        val port: Int = AppSettings.deployment.port,
        val sslPort: Int = AppSettings.deployment.sslPort,
        val host: String = NetworkUtils.getServerUrl().toString(),
        val allowedHosts: List<String> = AppSettings.cors.allowedHosts,
    )

    @Serializable
    data class ServerSpec(
        val serverHost: String?,
        val serverPort: Int?,
        val localHost: String?,
        val localPort: Int?,
        val remoteHostHost: String?,
        val remoteAddress: String?,
        val remotePort: Int?,
        val httpVersion: String?,
        val scheme: String?
    ) {
        constructor(call: ApplicationCall?) : this(
            serverHost = call?.request?.local?.serverHost,
            serverPort = call?.request?.local?.serverPort,
            localHost = call?.request?.local?.localHost,
            localPort = call?.request?.local?.localPort,
            remoteHostHost = call?.request?.local?.remoteHost,
            remoteAddress = call?.request?.local?.remoteAddress,
            remotePort = call?.request?.local?.remotePort,
            httpVersion = call?.request?.httpVersion,
            scheme = call?.request?.local?.scheme
        )
    }

    init {
        val className: String? = this::class.simpleName
        val environment: EnvironmentType = AppSettings.runtime.environment

        if (connectors.isEmpty()) {
            errors.add("$className. No connectors detected.")
        }

        if (environment == EnvironmentType.PROD) {
            if (AppSettings.cors.allowAllHosts()) {
                errors.add("$className. Allowing all hosts. '${environment}'.")
            }

            if (!NetworkUtils.isSecureProtocol(protocol = configured.protocol)) {
                errors.add(
                    "$className. Configured insecure '${configured.protocol}' protocol. '${environment}'."
                )
            }

            serverSpec.scheme?.let { scheme ->
                if (!NetworkUtils.isSecureProtocol(protocol = scheme)) {
                    errors.add(
                        "$className. Running with insecure '${scheme}' protocol. '${environment}'."
                    )
                }
            }

            if (configured.port == configured.sslPort) {
                errors.add(
                    "$className. Secure and insecure ports are the same: ${configured.port}. " +
                            "${environment}."
                )
            }

            if (AppSettings.security.useSecureConnection) {
                val configuredPort = listOf(configured.sslPort)
                if (NetworkUtils.isInsecurePort(ports = configuredPort)) {
                    errors.add(
                        "$className. Configured port is not secure or not set. " +
                                "Port: $configuredPort. ${environment}."
                    )
                }

                val runtimePorts: List<Int?> = listOf(serverSpec.serverPort, serverSpec.localPort, serverSpec.remotePort)
                if (NetworkUtils.isInsecurePort(ports = runtimePorts)) {
                    errors.add(
                        "$className. Runtime ports are not secure or not set. " +
                                "Port: $configuredPort. ${environment}."
                    )
                }
            } else {
                if (configured.port == 0) {
                    errors.add("$className. Insecure port is not set. ${environment}.")
                }
            }
        }
    }
}

private fun getConnectors(call: ApplicationCall?, errors: MutableList<String>): MutableMap<String, List<String>> {
    val connectors: MutableMap<String, List<String>> = mutableMapOf()

    call?.application?.environment.let {
        (it as ApplicationEngineEnvironmentReloading).connectors.forEach { connection ->
            val connectorData: String = connection.type.name
            var attributes: MutableList<String>? = null

            when (connection) {
                is EngineSSLConnectorBuilder -> {
                    attributes = mutableListOf(
                        "host: ${connection.host}",
                        "post: ${connection.port}",
                        "keyStoreType: ${connection.keyStore.type}",
                        "keyStoreProvider: ${connection.keyStore.provider}",
                        "keyStorePath: ${connection.keyStorePath}",
                        "keyAlias: ${connection.keyAlias}",
                        "trustStorePath: ${connection.trustStorePath}",
                        "trustStore: ${connection.trustStore}",
                        "enabledProtocols: ${connection.enabledProtocols}"
                    )
                }

                is EngineConnectorBuilder -> {
                    attributes = mutableListOf(
                        "host: ${connection.host}",
                        "post: ${connection.port}"
                    )
                }
            }

            attributes?.let { data ->
                connectors[connectorData] = data
            } ?: run {
                errors.add("Unknown Connector: $connection")
            }
        }
    }

    return connectors
}
