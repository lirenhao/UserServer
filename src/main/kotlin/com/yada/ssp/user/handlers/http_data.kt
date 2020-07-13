package com.yada.ssp.user.handlers

data class InitData(val newPwd: String?, val code: String?)

data class ResetPwdData(val oldPwd: String?, val newPwd: String?, val captcha: String?)

data class ChangePwdData(val oldPwd: String?, val newPwd: String?, val captcha: String?)

data class UserInfoData(val userId: String, val status: String, val emailAddress: String)

data class UserUpdateData(val orgId: String?, val roles: Set<String>?, val status: String?, val emailAddress: String?)