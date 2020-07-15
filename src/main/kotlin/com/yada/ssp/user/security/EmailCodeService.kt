package com.yada.ssp.user.security

import com.yada.ssp.user.model.EmailCode
import com.yada.ssp.user.repository.EmailCodeRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

interface IEmailCodeService {
    fun send(key: String, email: String): Mono<Boolean>
    fun check(key: String, code: String): Mono<Boolean>
}

@Service
class EmailCodeService @Autowired constructor(
        private val emailCodeRepo: EmailCodeRepository,
        private val emailSender: JavaMailSender,
        @Qualifier("templateEngine") private val templateEngine: ITemplateEngine,
        @Value("\${email.message.expire:0}")
        private var messageExpire: Int,
        @Value("\${email.message.form:}")
        private val messageForm: String,
        @Value("\${email.message.Subject:}")
        private val messageSubject: String
) : IEmailCodeService {

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val sdf = SimpleDateFormat("yyyyMMddHHmmss")

    @Transactional
    override fun send(key: String, email: String): Mono<Boolean> = emailCodeRepo
            .save(EmailCode(key, email, genCode(), sdf.format(Date())))
            .flatMap {
                val mailMessage = emailSender.createMimeMessage()
                val messageHelper = MimeMessageHelper(mailMessage)
                messageHelper.setTo(it.email)
                messageHelper.setFrom(messageForm)
                messageHelper.setSubject(messageSubject)
                val ctx = Context(Locale.ENGLISH)
                ctx.setVariable("code", it.code)
                messageHelper.setText(templateEngine.process("email", ctx), true)

                emailSender.send(mailMessage)
                logger.info("E-mail to [{}] send success!", email)
                Mono.just(true)
            }.switchIfEmpty {
                logger.warn("E-mail to [{}] send failed! [{}]", email, "save code error")
                Mono.just(false)
            }.doOnError {
                logger.warn("E-mail to [{}] send failed! [{}]", email, it.message)
                throw it
            }

    override fun check(key: String, code: String): Mono<Boolean> = emailCodeRepo.findById(key)
            .flatMap {
                if (isAble(it.dataTime, messageExpire)) {
                    Mono.just(it.code == code)
                } else {
                    Mono.just(false)
                }
            }
            .switchIfEmpty(Mono.just(false))

    private fun genCode(len: Int = 6): String = (0 until len)
            .map {
                (0 until 10).random()
            }.joinToString("")

    private fun isAble(dataTime: String?, expire: Int): Boolean =
            try {
                val curTime = System.currentTimeMillis()
                val hisTime: Long = sdf.parse(dataTime).time + (expire * 1000)
                curTime < hisTime
            } catch (e: ParseException) {
                false
            }
}