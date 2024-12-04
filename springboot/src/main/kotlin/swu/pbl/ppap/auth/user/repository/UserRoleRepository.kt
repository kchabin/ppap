package swu.pbl.ppap.auth.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import swu.pbl.ppap.auth.user.entity.UserRole


interface UserRoleRepository : JpaRepository<UserRole, Long> {
}