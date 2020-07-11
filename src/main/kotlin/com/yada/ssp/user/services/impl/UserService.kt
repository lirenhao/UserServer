package com.yada.ssp.user.services.impl

import com.yada.ssp.user.model.User
import com.yada.ssp.user.repository.UserRepository
import com.yada.ssp.user.security.IPwdDigestService
import com.yada.ssp.user.services.IUserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
open class UserService @Autowired constructor(
        private val userRepo: UserRepository,
        private val pwdDigestService: IPwdDigestService
) : IUserService {
    override fun get(id: String): Mono<User> = userRepo.findById(id)

    override fun getByOrgId(orgId: String): Flux<User> = userRepo.findByOrgIdOrderByIdAsc(orgId)

    @Transactional
    override fun createOrUpdate(user: User): Mono<User> =
            userRepo
                    .findById(user.id)
                    .flatMap { userRepo.save(user) }
                    .switchIfEmpty(
                            userRepo.save(user)
                                    .flatMap {
                                        resetPwd(it.id).then(Mono.just(it))
                                    }
                    )

    @Transactional
    override fun delete(id: String): Mono<Void> = userRepo.deleteById(id)

    override fun deleteByOrgId(orgId: String): Mono<Void> = userRepo.deleteByOrgId(orgId)

    override fun exist(id: String): Mono<Boolean> = userRepo.existsById(id)

    override fun getPwd(id: String): Mono<String> = userRepo.findPwdById(id)

    @Transactional
    override fun updateStatus(id: String, status: String): Mono<Void> = userRepo.updateStatus(id, status)

    @Transactional
    override fun resetPwd(id: String): Mono<Void> = userRepo.changePwd(id, pwdDigestService.getDefaultPwdDigest(id))

    @Transactional
    override fun initPwd(id: String, pwd: String, status: String): Mono<Void> = changePwd(id, pwd)
            .then(Mono.just(true))
            .flatMap {
                updateStatus(id, status)
            }

    @Transactional
    override fun changePwd(id: String, pwd: String): Mono<Void> = userRepo.changePwd(id, pwd)

    @Transactional
    override fun changePwd(id: String, oldPwd: String, newPwd: String): Mono<Boolean> {
        val nct = pwdDigestService.getPwdDigest(id, newPwd)
        val oct = pwdDigestService.getPwdDigest(id, oldPwd)

        return getPwd(id).map { oct == it }
                .filter { it }
                .flatMap { changePwd(id, nct).then(Mono.just(true)) }
                .defaultIfEmpty(false)
    }

    override fun getAll(): Flux<User> = userRepo.findAll()
}