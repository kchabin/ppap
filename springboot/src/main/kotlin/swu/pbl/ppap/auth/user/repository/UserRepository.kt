package swu.pbl.ppap.auth.user.repository

import org.springframework.data.jpa.repository.JpaRepository
//import swu.pbl.ppap.openapi.generated.model.User
import swu.pbl.ppap.auth.user.entity.UserEntity

interface UserRepository : JpaRepository<UserEntity, Long> {
    //ID 중복 검사용
    fun findByLoginId(loginId: String): UserEntity?
}