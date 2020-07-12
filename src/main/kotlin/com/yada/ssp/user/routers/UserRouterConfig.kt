package com.yada.ssp.user.routers

import com.yada.ssp.user.filters.AuthHandlerFilter
import com.yada.ssp.user.filters.UserHandlerFilter
import com.yada.ssp.user.filters.WhitelistHandlerFilter
import com.yada.ssp.user.handlers.AuthHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class UserRouterConfig @Autowired constructor(
        private val authHandler: AuthHandler,
        private val authHandlerFilter: AuthHandlerFilter,
        private val userHandlerFilter: UserHandlerFilter,
        private val whitelistHandlerFilter: WhitelistHandlerFilter
) {
    @Bean
    fun authRouter() = router {
        "/user".nest {
            GET("", authHandler::get)
            POST("/fa", authHandler::fa)
            POST("/init", authHandler::init)
            POST("/policy", authHandler::policy)
            POST("/resetPwd", authHandler::resetPwd)
            POST("/changePwd", authHandler::changePwd)
            filter(authHandlerFilter)
            filter(userHandlerFilter)
        }
        filter(whitelistHandlerFilter)
    }
}