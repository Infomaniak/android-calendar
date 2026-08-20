/*
 * Infomaniak Calendar - Android
 * Copyright (C) 2026 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.calendar.utils

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.infomaniak.calendar.BuildConfig
import com.infomaniak.multiplatform_calendar.data.remote.caldav.CaldavDebugInterception

/**
 * Opt-in routing of CalDAV traffic through an intercepting proxy (Proxyman, Charles, mitmproxy, …).
 * Those requests are issued by Rust, not OkHttp, so they ignore the system proxy settings and
 * `network_security_config.xml` — hence this plumbing. To enable it, export the proxy's root CA to any
 * `.pem` file inside `src/debug/assets/certificates/`, git-ignored: Android's system trust store ignores
 * user-installed certificates, so it is passed explicitly. Without any of them, HTTPS would fail anyway,
 * so they are what opts in.
 *
 * The proxy defaults to the host machine as seen from an emulator; for a physical device, override it
 * with `caldavDebugProxyUrl=http://<machine LAN address>:9090` in `local.properties`.
 */
object CaldavDebugConfig {

    private const val TAG = "CaldavDebugConfig"
    private const val CERTIFICATES_ASSETS_DIR = "certificates"
    private const val PEM_EXTENSION = ".pem"

    fun interception(context: Context): CaldavDebugInterception? {
        val rootCertificates = readRootCertificates(context)
        if (rootCertificates.isEmpty()) return null

        val defaultInterception = CaldavDebugInterception(extraRootCertificatesPem = rootCertificates)
        val interception = BuildConfig.CALDAV_DEBUG_PROXY_URL.takeIf { it.isNotBlank() }
            ?.let { defaultInterception.copy(proxyUrl = it) }
            ?: defaultInterception

        Log.i(TAG, "Proxying CalDAV through ${interception.proxyUrl}, trusting ${rootCertificates.count()} root certificate(s)")

        return interception
    }

    private fun readRootCertificates(context: Context): List<String> = with(context.assets) {
        listPemFileNames().mapNotNull { readPemFile(it) }
    }

    private fun AssetManager.listPemFileNames(): List<String> = runCatching {
        list(CERTIFICATES_ASSETS_DIR)
    }.getOrNull()?.filter { it.endsWith(PEM_EXTENSION, ignoreCase = true) }.orEmpty()

    private fun AssetManager.readPemFile(fileName: String): String? = runCatching {
        open("$CERTIFICATES_ASSETS_DIR/$fileName").bufferedReader().use { it.readText() }
    }.getOrElse {
        Log.w(TAG, "Could not read $CERTIFICATES_ASSETS_DIR/$fileName", it)
        null
    }
}
