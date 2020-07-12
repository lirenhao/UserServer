package com.yada.ssp.user.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "yada.auth")
class AuthConfigProperties (
        var exPaths: List<String> = listOf("/res_list"),
        var mockOpen: Boolean = false,
        var mockOrgId: String = "00",
        var mockUserId: String = "admin"
)
