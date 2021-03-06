package com.yada.ssp.user.services

import com.yada.ssp.user.model.Org
import com.yada.ssp.user.repository.OrgRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

data class OrgTree(val org: Org, val children: Set<OrgTree>?)

fun Org.isMyOffspring(org: Org) = org.id.startsWith(this.id)

private fun makeTree(orgs: List<Org>): List<OrgTree> {
    val ret = ArrayList<OrgTree>()
    var tmp = orgs
    while (tmp.isNotEmpty()) {
        val o = tmp.first()
        val childrenOrgTree = tmp.drop(1).partition(o::isMyOffspring).run {
            tmp = second
            if (first.isEmpty()) null else makeTree(first).toSet()
        }

        ret.add(OrgTree(o, childrenOrgTree))
    }
    return ret
}

@Service
open class OrgService @Autowired constructor(
        private val repo: OrgRepository,
        private val userSvc: UserService
) {
    fun getTree(orgIdPrefix: String?): Flux<OrgTree> =
            repo.findByIdStartingWithOrderByIdAsc(orgIdPrefix ?: "")//("^${orgIdPrefix ?: ""}.*")
                    .collectList()
                    .map(::makeTree)
                    .flatMapMany { Flux.fromIterable(it) }

    @Transactional
    fun createOrUpdate(org: Org): Mono<Org> = repo.save(org)

    @Transactional
    fun delete(id: String): Mono<Void> = userSvc.deleteByOrgId(id).then(repo.deleteById(id))

    fun get(id: String): Mono<Org> = repo.findById(id)

    fun exist(id: String): Mono<Boolean> = repo.existsById(id)

}