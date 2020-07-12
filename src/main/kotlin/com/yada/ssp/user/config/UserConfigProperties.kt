package com.yada.ssp.user.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "yada.user")
class UserConfigProperties (
        var exPaths: List<String> = listOf("/res_list"),
        var statusPaths: Map<String, List<String>> = mapOf(),
        var statusNext: Map<String, String> = mapOf()
)
