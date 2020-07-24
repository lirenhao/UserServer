package com.yada.ssp.user.handlers.apis

import com.yada.ssp.user.handlers.UserUpdateData
import com.yada.ssp.user.model.User
import com.yada.ssp.user.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Component
class UserHandler @Autowired constructor(private val userService: UserService) {

    fun getPage(req: ServerRequest): Mono<ServerResponse> =
            userService.getPage(
                    req.queryParam("orgId").orElse(""),
                    req.queryParam("userId").orElse(""),
                    PageRequest.of(
                            req.queryParam("page").orElse("0").toInt(),
                            req.queryParam("size").orElse("10").toInt()
                    )
            ).flatMap { ok().bodyValue(it) }

    fun getOne(req: ServerRequest): Mono<ServerResponse> =
            userService.get(req.pathVariable("id"))
                    .flatMap { ok().bodyValue(it) }
                    .switchIfEmpty(ok().build())

    fun exist(req: ServerRequest): Mono<ServerResponse> =
            userService.exist(req.pathVariable("id"))
                    .flatMap { ok().bodyValue(it) }

    fun create(req: ServerRequest): Mono<ServerResponse> =
            req.bodyToMono(User::class.java)
                    .flatMap { userService.create(it) }
                    .flatMap { ok().bodyValue(it) }

    fun update(req: ServerRequest): Mono<ServerResponse> =
            req.bodyToMono<UserUpdateData>()
                    .flatMap { data ->
                        userService.get(req.pathVariable("id"))
                                .map { user ->
                                    User(user.id,
                                            data.orgId ?: user.orgId,
                                            data.roles ?: user.roles,
                                            data.status ?: user.status,
                                            data.emailAddress ?: user.email
                                    )
                                }
                                .flatMap { userService.update(it) }
                    }
                    .flatMap { ok().bodyValue(it) }
                    .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在")))

    fun delete(req: ServerRequest): Mono<ServerResponse> =
            userService.delete(req.pathVariable("id")).then(ok().build())

    @Transactional
    fun reset(req: ServerRequest): Mono<ServerResponse> =
            userService.get(req.pathVariable("id"))
                    .flatMap {
                        userService.updateStatus(it.id, "01").then(Mono.just(true))
                    }.flatMap {
                        userService.resetPwd(req.pathVariable("id")).then(ok().build())
                    }.switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "重置密码失败")))

    fun updatePolicy(req: ServerRequest): Mono<ServerResponse> =
            userService.updatePolicy().then(ok().build())

}
