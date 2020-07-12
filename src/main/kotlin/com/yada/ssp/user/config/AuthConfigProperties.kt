package com.yada.ssp.user.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "yada.auth")
class AuthConfigProperties (
        var exPaths: List<String> = listOf("/res_list")
)
