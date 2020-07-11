package com.yada.ssp.user.handlers

data class InitData(val newPwd: String?, val code: String?)

data class ResetPwdData(val oldPwd: String?, val newPwd: String?, val captcha: String?)

data class ChangePwdData(val oldPwd: String?, val newPwd: String?, val captcha: String?)

data class UserInfoData(val userId: String?, val status: String?)
