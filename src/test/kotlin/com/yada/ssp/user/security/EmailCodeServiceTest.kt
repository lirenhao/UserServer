package com.yada.ssp.user.security

import com.yada.ssp.user.model.EmailCode
import com.yada.ssp.user.repository.EmailCodeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono
import java.text.SimpleDateFormat
import java.util.*

@SpringBootTest
class EmailCodeServiceTest {

    private val sdf = SimpleDateFormat("yyyyMMddHHmmss")
    @MockBean
    private lateinit var emailCodeRepo: EmailCodeRepository
    @Autowired
    private lateinit var emailCodeService: EmailCodeService

    @Test
    fun `Assert check is false, findById empty`() {
        given(emailCodeRepo.findById(Mockito.anyString())).willReturn(Mono.empty())
        emailCodeService.check("key", "code").subscribe {
            assertThat(it).isEqualTo(false)
        }
    }

    @Test
    fun `Assert check is false, message expire`() {
        given(emailCodeRepo.findById(Mockito.anyString()))
                .willReturn(Mono.just(EmailCode(
                        "key", "email", "code",
                        sdf.format(Date(System.currentTimeMillis() - 300 * 1000))
                )))
        emailCodeService.check("key", "code").subscribe {
            assertThat(it).isEqualTo(false)
        }
    }

    @Test
    fun `Assert check is false, code no equals`() {
        given(emailCodeRepo.findById(Mockito.anyString()))
                .willReturn(Mono.just(EmailCode(
                        "key", "email", "code",
                        sdf.format(Date(System.currentTimeMillis()))
                )))
        emailCodeService.check("key", "123456").subscribe {
            assertThat(it).isEqualTo(false)
        }
    }

    @Test
    fun `Assert check is true`() {
        given(emailCodeRepo.findById(Mockito.anyString()))
                .willReturn(Mono.just(EmailCode(
                        "key", "email", "code",
                        sdf.format(Date(System.currentTimeMillis()))
                )))
        emailCodeService.check("key", "code").subscribe {
            assertThat(it).isEqualTo(true)
        }
    }
}