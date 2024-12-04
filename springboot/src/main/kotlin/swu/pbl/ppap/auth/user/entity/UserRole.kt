package swu.pbl.ppap.auth.user.entity

import jakarta.persistence.*
import swu.pbl.ppap.openapi.generated.model.Role

@Entity
class UserRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    val role: Role,


    //user_id라는 외래 키 생성
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(name = "fk_user_role_login_id"))
    val userEntity: UserEntity
) {
}