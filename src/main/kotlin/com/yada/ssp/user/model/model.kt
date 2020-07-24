package com.yada.ssp.user.model

import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Org(
        val id: String,
        val name: String
)

@Document
data class User(
        val id: String,
        val orgId: String,
        val roles: Set<String>,
        val status: String,
        val email: String
)

@Document
data class EmailCode(
        val id: String,
        val email: String,
        val code: String,
        val dataTime: String
)
