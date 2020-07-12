package com.yada.ssp.user.filters

import com.yada.ssp.user.config.UserConfigProperties
import com.yada.ssp.user.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono

@Component
class UserHandlerFilter(
        private val config: UserConfigProperties,
        private val userSvc: UserService
) : Filter {

    override fun invoke(request: ServerRequest, next: Next): Mono<ServerResponse> {
        val uri = request.path()
        if (config.exPaths.stream().anyMatch { path: String? -> PathPatternParser().parse(path!!).matches(PathContainer.parsePath(uri)) }) {
            return next(request)
        } else {
            val userId: String? = request.headers().firstHeader("X-YADA-USER-ID")
            return if (userId != null)
                userSvc.get(userId).flatMap {
                    val statusPaths = config.statusPaths[it.status]
                    if (statusPaths == null || statusPaths.isEmpty() || statusPaths.stream().anyMatch { path: String? ->
                                PathPatternParser().parse(path!!).matches(PathContainer.parsePath(uri))
                            }
                    ) {
                        next(request)
                    } else {
                        Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"))
                    }
                }
            else
                Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"))
        }
    }

}