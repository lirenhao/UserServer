package com.yada.ssp.user.config

import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration

@Configuration
open class MongoConfig constructor(
        @Value("\${yada.db.mongo.uri:mongodb://localhost/yada_auth?replicaSet=rs}")
        private val uri: String
) : AbstractReactiveMongoConfiguration() {
    private val connectionString = ConnectionString(uri)
    override fun reactiveMongoClient(): MongoClient = MongoClients.create(connectionString)

    override fun getDatabaseName(): String = connectionString.database!!

    @Bean
    open fun transactionManager(factory: ReactiveMongoDatabaseFactory) = ReactiveMongoTransactionManager(factory)

    override fun autoIndexCreation(): Boolean {
        return false
    }
}