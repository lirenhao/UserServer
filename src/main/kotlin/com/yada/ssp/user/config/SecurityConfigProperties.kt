package com.yada.ssp.user.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "yada.security")
class SecurityConfigProperties(
        var defaultPwd: String = "111111",
        var recaptcha: RecaptchaProperties = RecaptchaProperties(),
        var pwdStrength: Int = 1
) {
    enum class RecaptchaType { None, Google, GoogleCN }

    data class RecaptchaProperties(
            var type: RecaptchaType = RecaptchaType.None,
            var secret: String = "",
            var sitekey: String = "",
            var proxyHost: String = "",
            var proxyPort: Int = 80
    )
}