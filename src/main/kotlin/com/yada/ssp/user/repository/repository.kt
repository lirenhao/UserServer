package com.yada.ssp.user.repository

import com.yada.ssp.user.model.*
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.data.repository.reactive.ReactiveSortingRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.springframework.data.mongodb.core.query.Query as MonoQuery

interface IUserRepository {
    fun batchStatus(preStatus: String, status: String): Mono<Void>
    fun updateStatus(id: String, status: String): Mono<Void>
    fun changePwd(id: String, pwd: String): Mono<Void>
    fun findPwdById(id: String): Mono<String>
}

class UserRepositoryImpl @Autowired constructor(
        private val reactiveMongoTemplate: ReactiveMongoTemplate
) : IUserRepository {

    override fun batchStatus(preStatus: String, status: String): Mono<Void> {
        val query = MonoQuery(Criteria.where("status").`is`(preStatus))
        val update = Update().set("status", status)
        return reactiveMongoTemplate.updateMulti(query, update, User::class.java).then(Mono.empty())
    }

    override fun updateStatus(id: String, status: String): Mono<Void> {
        val query = MonoQuery(Criteria.where("id").`is`(id))
        val update = Update().set("status", status)
        return reactiveMongoTemplate.updateFirst(query, update, User::class.java).then(Mono.empty())
    }

    override fun changePwd(id: String, pwd: String): Mono<Void> {
        val query = MonoQuery(Criteria.where("id").`is`(id))
        val update = Update().set("pwd", pwd)
        return reactiveMongoTemplate.updateFirst(query, update, User::class.java).then(Mono.empty())
    }

    override fun findPwdById(id: String): Mono<String> {
        val query = MonoQuery(Criteria.where("_id").`is`(id))
        query.fields().include("pwd").exclude("_id")
        val colName = reactiveMongoTemplate.getCollectionName(User::class.java)
        return reactiveMongoTemplate.findOne(query, Document::class.java, colName)
                .map { it["pwd"] as String }
    }
}

interface OrgRepository : ReactiveCrudRepository<Org, String> {
    fun findByIdStartingWithOrderByIdAsc(regex: String): Flux<Org>
}

interface UserRepository : ReactiveCrudRepository<User, String>, ReactiveSortingRepository<User, String>, IUserRepository {

    fun findByOrgIdOrderByIdAsc(orgId: String): Flux<User>

    fun findByOrgIdAndIdLike(orgId: String, id: String, sort: Sort): Flux<User>

    fun countByOrgIdAndIdLike(orgId: String, id: String, sort: Sort): Mono<Long>

    fun deleteByOrgId(orgId: String): Mono<Void>
}

interface EmailCodeRepository : ReactiveCrudRepository<EmailCode, String>
