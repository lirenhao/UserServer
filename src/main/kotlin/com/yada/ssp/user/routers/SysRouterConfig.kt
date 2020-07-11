package com.yada.ssp.user.routers

import com.yada.ssp.user.handlers.apis.OrgHandler
import com.yada.ssp.user.handlers.apis.UserHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
open class SysRouterConfig @Autowired constructor(
        private val orgHandler: OrgHandler,
        private val userHandler: UserHandler
) {
    @Bean
    open fun sysApiRouter() = router {
        "/sys".nest {
            "/org".nest {
                GET("", orgHandler::getTree)
                GET("/{id}", orgHandler::get)
                GET("/{id}/exist", orgHandler::exist)
                PUT("", orgHandler::createOrUpdate)
                DELETE("/{id}", orgHandler::delete)
            }
            "/user".nest {
                GET("", userHandler::getUsersBy)
                GET("/{id}", userHandler::get)
                GET("/{id}/exist", userHandler::exist)
                PUT("/{id}/reset_pwd", userHandler::resetPwd)
                PUT("", userHandler::createOrUpdate)
                DELETE("/{id}", userHandler::delete)
            }
        }
    }
}