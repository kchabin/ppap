package swu.pbl.ppap

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import swu.pbl.ppap.auth.audit.AuditEntity
import swu.pbl.ppap.auth.user.entity.UserEntity
import swu.pbl.ppap.auth.user.repository.UserRepository
import swu.pbl.ppap.auth.user.service.UserService
import swu.pbl.ppap.openapi.generated.model.User
import java.time.LocalDateTime

@Transactional
@SpringBootTest
class UserTest(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val userService: UserService,

) {

    @Test
    @DisplayName("사용자 auditing 테스트")
    fun audit_test() {
        // given
        val user = UserEntity(
            loginId = "audittest3",
            password = "audit@12",
            confirmPassword = "audit@12",
            username = "audittest3",
            email = "audit3@exaple.com",
            userType = User.UserType.PRIVATE
        )

        // when
        val saved = userRepository.save(user)

        // then
        assertThat(saved.username).isEqualTo(user.username)

    }

}