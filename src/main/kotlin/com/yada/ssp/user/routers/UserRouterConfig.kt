package com.yada.ssp.user.routers

import com.yada.ssp.user.auth.AuthFilter
import com.yada.ssp.user.handlers.AuthHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class UserRouterConfig @Autowired constructor(
        private val authHandler: AuthHandler,
        private val authFilter: AuthFilter
) {
    @Bean
    fun authRouter() = router {
        "/user".nest {
            GET("", authHandler::get)
            POST("/fa", authHandler::sendCode)
            POST("/initPwd", authHandler::initPwd)
            POST("/policy", authHandler::policy)
            POST("/changePwd", authHandler::changePwd)
            filter(authFilter)
        }
    }
}