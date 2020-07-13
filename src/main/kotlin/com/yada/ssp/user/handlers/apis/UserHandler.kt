package com.yada.ssp.user.handlers.apis

import com.yada.ssp.user.model.User
import com.yada.ssp.user.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Component
class UserHandler @Autowired constructor(private val userService: UserService) {

    fun getPage(req: ServerRequest): Mono<ServerResponse> =
            ok().body(userService.getPage(
                    req.queryParam("orgId").orElse(""),
                    req.queryParam("userId").orElse(""),
                    PageRequest.of(
                            req.queryParam("page").orElse("0").toInt(),
                            req.queryParam("size").orElse("10").toInt()
                    )
            ))

    fun getOne(req: ServerRequest): Mono<ServerResponse> =
            ok().body(userService.get(req.pathVariable("id")).switchIfEmpty(Mono.empty()))

    fun exist(req: ServerRequest): Mono<ServerResponse> =
            ok().body(userService.exist(req.pathVariable("id")))

    fun createOrUpdate(req: ServerRequest): Mono<ServerResponse> =
            ok().body(req.bodyToMono(User::class.java).flatMap(userService::createOrUpdate))

    fun delete(req: ServerRequest): Mono<ServerResponse> =
            ok().body(userService.delete(req.pathVariable("id")))

    fun resetPwd(req: ServerRequest): Mono<ServerResponse> =
            userService.get(req.pathVariable("id")).flatMap {
                when (it.status) {
                    "00" -> userService.updateStatus(it.id, "03").then(Mono.just(true))
                    "02" -> userService.updateStatus(it.id, "04").then(Mono.just(true))
                    else -> Mono.just(true)
                }
            }.flatMap {
                ok().body(userService.resetPwd(req.pathVariable("id")))
            }.switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "重置密码失败")))
}