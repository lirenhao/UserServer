package com.yada.ssp.user.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "yada.mock")
class MockConfigProperties (
        var open: Boolean = false,
        var orgId: String = "00",
        var userId: String = "admin"
)
