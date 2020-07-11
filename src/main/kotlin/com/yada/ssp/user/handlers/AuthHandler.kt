package com.yada.ssp.user.handlers

import com.yada.ssp.user.auth.Auth
import com.yada.ssp.user.security.IEmailCodeService
import com.yada.ssp.user.security.IPwdStrengthService
import com.yada.ssp.user.security.IRecaptchaService
import com.yada.ssp.user.services.IUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Component
class AuthHandler @Autowired constructor(
        private val userService: IUserService,
        private val recaptchaService: IRecaptchaService,
        private val pwdStrengthService: IPwdStrengthService,
        private val emailCodeService: IEmailCodeService
) {

    /**
     * 从请求中提取用户Auth信息
     */
    private fun getAuth(req: ServerRequest): Mono<Auth> {
        val auth = req.attribute("auth").orElse(null) as Auth
        return Mono.just(auth)
    }

    /**
     * 获取用户信息
     */
    fun get(req: ServerRequest): Mono<ServerResponse> = getAuth(req)
            .flatMap { auth ->
                userService.get(auth.userId!!)
                        .map {
                            UserInfoData(it.id, it.status)
                        }
                        .flatMap {
                            ServerResponse.ok().bodyValue(it)
                        }
                        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "用户信息不存在")))
            }

    /**
     * 发送邮箱验证码
     */
    fun fa(req: ServerRequest): Mono<ServerResponse> = getAuth(req)
            .flatMap { auth ->
                userService.get(auth.userId!!)
                        .flatMap {
                            if (it.emailAddress != null)
                                emailCodeService.send(auth.userId, it.emailAddress)
                                        .flatMap { flag ->
                                            ServerResponse.ok().bodyValue(flag)
                                        }
                                        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "验证码发送失败")))
                            else
                                Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "验证码发送失败"))
                        }
            }

    /**
     * 用户初次登陆 01->02
     */
    fun init(req: ServerRequest): Mono<ServerResponse> = req.bodyToMono<InitData>()
            .flatMap { data ->
                val auth = req.attribute("auth").get() as Auth
                emailCodeService.check(auth.userId!!, data.code!!)
                        .filter { it }
                        .flatMap {
                            if (data.newPwd != null) {
                                if (!pwdStrengthService.checkStrength(data.newPwd))
                                    Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "密码强度不足"))
                                else
                                    userService.initPwd(auth.userId, data.newPwd, "02").then(ServerResponse.ok().build())
                            } else
                                Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "密码不能为空"))
                        }
                        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "验证码未通过")))
            }

    /**
     * 用户同意协议 02->00
     */
    fun policy(req: ServerRequest): Mono<ServerResponse> = getAuth(req)
            .flatMap {
                userService.updateStatus(it.userId!!, "00").then(ServerResponse.ok().build())
            }

    /**
     * 强制用户修改密码 03->00
     */
    fun resetPwd(req: ServerRequest): Mono<ServerResponse> = req.bodyToMono<ResetPwdData>()
            .flatMap { data ->
                recaptchaService.check(data.captcha!!)
                        .flatMap {
                            if (data.oldPwd != null && data.newPwd != null) {
                                if (!pwdStrengthService.checkStrength(data.newPwd)) {
                                    Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "密码强度不足"))
                                } else {
                                    val auth = req.attribute("auth").get() as Auth
                                    userService.changePwd(auth.userId!!, data.oldPwd, data.newPwd).flatMap { flag ->
                                        if (flag) {
                                            userService.updateStatus(auth.userId, "00").then(ServerResponse.ok().build())
                                        } else
                                            Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "修改密码时出错"))
                                    }
                                }
                            } else
                                Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "密码不能为空"))
                        }
                        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "未通过人机验证")))
            }

    /**
     * 用户修改密码
     */
    fun changePwd(req: ServerRequest): Mono<ServerResponse> = req.bodyToMono<ChangePwdData>()
            .flatMap { data ->
                recaptchaService.check(data.captcha!!)
                        .flatMap {
                            if (data.oldPwd != null && data.newPwd != null) {
                                if (!pwdStrengthService.checkStrength(data.newPwd)) {
                                    Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "密码强度不足"))
                                } else {
                                    val auth = req.attribute("auth").get() as Auth
                                    userService.changePwd(auth.userId!!, data.oldPwd, data.newPwd).flatMap { flag ->
                                        if (flag)
                                            ServerResponse.ok().build()
                                        else
                                            Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "修改密码时出错"))
                                    }
                                }
                            } else
                                Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "密码不能为空"))
                        }
                        .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.CONFLICT, "未通过人机验证")))
            }

}