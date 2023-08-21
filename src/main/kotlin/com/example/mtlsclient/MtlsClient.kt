package com.example.mtlsclient

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory


class MtlsClient(
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val serverUrl = "https://localhost"

//    correct trust store
    private val truststorePath = "src/main/resources/static/clientkeystore.p12"

//    in-correct trust store will show "javax.net.ssl.SSLHandshakeException: Received fatal alert: bad_certificate"
//    private val truststorePath = "src/main/resources/static/truststore.p12"
    private val truststoretype = "pkcs12"
    private val truststorePassword = "willson"

    fun sendRequest() {
        // Load the truststore containing the server's certificate
        val truststore = KeyStore.getInstance(truststoretype)
        FileInputStream(truststorePath).use { truststoreStream ->
            truststore.load(truststoreStream, truststorePassword.toCharArray())
        }

        // Create a TrustManagerFactory to use the truststore
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(truststore)

        val keyStore = KeyStore.getInstance("pkcs12")
        keyStore.load(FileInputStream(truststorePath), "willson".toCharArray())
        val socketFactory = SSLConnectionSocketFactory(
            SSLContextBuilder()
                .loadTrustMaterial(null, TrustSelfSignedStrategy())
                .loadKeyMaterial(keyStore, "willson".toCharArray()).build(),
            NoopHostnameVerifier.INSTANCE
        )

        val httpClient: CloseableHttpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory)
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .build()
        // Create a URI builder and add any necessary query parameters
        val uriBuilder = URIBuilder(serverUrl).setPort(8080).setPath("/simulation")
        log.info("uri = ${uriBuilder.path}")

        // Create an HTTP GET request with the URI
        val httpPost = HttpPost(uriBuilder.build())

        // Send the request and retrieve the response
        val response: HttpResponse?
        try {
            response = httpClient.execute(httpPost)
            val entity: HttpEntity? = response.entity

            if (entity != null) {
                // Process the response entity
                val responseString = EntityUtils.toString(entity)
                println(responseString)
            }

            // Cleanup resources
            EntityUtils.consume(entity)
            httpClient.close()
        } catch (error: Error) {
            log.error(error.toString())
        }
    }
}