/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.tls.HandshakeCertificates
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

class CustomTrust {
    private lateinit var context: Context
    private var clientBuilder: OkHttpClient.Builder? = null

    constructor(context: Context) {
        this.context = context
        if (BuildConfig.BUILD_TYPE.equals("release")) {
            val trustManager: X509TrustManager
            val sslSocketFactory: SSLSocketFactory
            try {
                val certificateFactory: CertificateFactory = CertificateFactory.getInstance("X.509")
                val certificates: Collection<Certificate?> =
                    certificateFactory.generateCertificates(trustedCertificatesInputStream())
                if (!certificates.isEmpty()) {
                    val certificate = certificates.single() as X509Certificate
                    val handshakeCertificates: HandshakeCertificates = HandshakeCertificates.Builder()
                        .addTrustedCertificate(certificate)
                        .build()
                    trustManager = handshakeCertificates.trustManager
                    sslSocketFactory = handshakeCertificates.sslSocketFactory()
                } else {
                    throw Exception("expected non-empty set of trusted certificates")
                }
            } catch (e: GeneralSecurityException) {
                throw RuntimeException(e)
            }
            clientBuilder = Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier(HostnameVerifier { hostname, sslSession ->
                    Log.i("INFO", "hostnameVerifier - string: $hostname")
                    hostname.equals(context.getString(R.string.parse_server_hostname))
                })
        } else {
            clientBuilder = Builder()
        }
    }

    fun getclientBuilder(): OkHttpClient.Builder? {
        return clientBuilder
    }

    private fun trustedCertificatesInputStream(): InputStream? {
        return context!!.resources.openRawResource(R.raw.certificate)
    }
}
