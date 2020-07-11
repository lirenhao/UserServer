package com.yada.ssp.user.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono

typealias Next = (ServerRequest) -> Mono<ServerResponse>
typealias Filter = (request: ServerRequest, next: Next) -> Mono<ServerResponse>

data class Auth(val orgId: String?, val userId: String?)

@Component
class AuthFilter(
        @Value("#{'\${auth.exclude.paths:/res_list,/error}'.split(',')}")
        private val exPaths: List<String>,
        @Value("\${auth.mock.open:false}")
        private val mockOpen: Boolean,
        @Value("\${auth.mock.orgId:}")
        private val mockOrgId: String,
        @Value("\${auth.mock.userId:}")
        private val mockUserId: String
) : Filter {

    override fun invoke(request: ServerRequest, next: Next): Mono<ServerResponse> {
        val uri = request.path()
        if (exPaths.stream().anyMatch { path: String? -> PathPatternParser().parse(path!!).matches(PathContainer.parsePath(uri)) }) {
            return next(request)
        } else {
            return if (mockOpen) {
                val auth = Auth(mockOrgId, mockUserId)
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