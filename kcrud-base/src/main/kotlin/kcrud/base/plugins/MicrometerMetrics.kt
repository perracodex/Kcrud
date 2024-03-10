/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kcrud.base.infrastructure.utils.NetworkUtils
import kcrud.base.settings.AppSettings

/**
 * The [MicrometerMetrics] plugin enables Micrometer metrics in your Ktor server application
 * and allows you to choose the required monitoring system, such as Prometheus, JMX, Elastic, and so on.
 *
 * By default, Ktor exposes metrics for monitoring HTTP requests and a set of low-level metrics for
 * monitoring the JVM. You can customize these metrics or create new ones.
 *
 * See: [MicrometerMetrics](https://ktor.io/docs/micrometer-metrics.html)
 *
 * See: [Micrometer](https://micrometer.io/docs/concepts)
 */
fun Application.configureMicroMeterMetrics() {

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry

        meterBinders = listOf(
            ClassLoaderMetrics(),
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics(),
            FileDescriptorMetrics(),
            UptimeMetrics()
        )
    }

    routing {
        authenticate(AppSettings.security.basic.providerName, optional = !AppSettings.security.isEnabled) {
            get("/metrics") {
                call.respond(status = HttpStatusCode.OK, message = appMicrometerRegistry.scrape())
            }
        }
    }

    NetworkUtils.logEndpoints(
        reason = "Micrometer Metrics",
        endpoints = listOf("metrics")
    )
}

/**
 * The PrometheusMeterRegistry is a Micrometer registry that allows you to monitor your application
 * using Prometheus, a popular open-source monitoring system and time series database.
 *
 * See: [Prometheus](https://prometheus.io/)
 *
 * See: [Micrometer Prometheus](https://micrometer.io/docs/registry/prometheus)
 */
val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
