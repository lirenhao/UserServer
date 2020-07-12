package com.yada.ssp.user.filters

import com.yada.ssp.user.config.MockConfigProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Configuration
class MockHandlerFilter(
        private val config: MockConfigProperties
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return if (config.open) {
            val request = exchange.request.mutate()
                    .header("X-YADA-ORG-ID", config.orgId)
                    .header("X-YADA-USER-ID", config.userId)
                    .build()
            chain.filter(exchange.mutate().request(request).build())
        } else {
            chain.filter(exchange)
        }
    }

}