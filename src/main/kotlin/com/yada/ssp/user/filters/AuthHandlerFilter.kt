package com.yada.ssp.user.filters

import com.yada.ssp.user.config.AuthConfigProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono

data class Auth(val orgId: String?, val userId: String?)

@Component
@Configuration
@EnableConfigurationProperties(AuthConfigProperties::class)
class AuthHandlerFilter(
        private val config: AuthConfigProperties
) : Filter {

    override fun invoke(request: ServerRequest, next: Next): Mono<ServerResponse> {
        val uri = request.path()
        if (config.exPaths.stream().anyMatch { path: String? -> PathPatternParser().parse(path!!).matches(PathContainer.parsePath(uri)) }) {
            return next(request)
        } else {
            return if (config.mockOpen) {
                val auth = Auth(config.mockOrgId, config.mockUserId)
                request.attributes()["auth"] = auth
                next(request)
            } else {
                val orgId: String? = request.headers().firstHeader("X-YADA-ORG-ID")
                val userId: String? = request.headers().firstHeader("X-YADA-USER-ID")
                if (orgId != null && userId != null) {
                    val auth = Auth(orgId, userId)
                    request.attributes()["auth"] = auth
                    next(request)
                } else {
                    Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"))
                }
            }
        }
    }

}