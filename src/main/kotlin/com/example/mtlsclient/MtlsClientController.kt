package com.example.mtlsclient

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MtlsClientController {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping
    fun simpleGet(): ResponseEntity<String> {
        log.info("Received out-going request as client to mTLS server")
        val mtlsClient = MtlsClient()
        mtlsClient.sendRequest()
        return ResponseEntity.ok("Requested to server successfully")
    }
}