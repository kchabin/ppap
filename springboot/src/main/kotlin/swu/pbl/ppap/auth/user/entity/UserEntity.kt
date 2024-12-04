package swu.pbl.ppap.auth.user.entity

import jakarta.persistence.*
import swu.pbl.ppap.openapi.generated.model.User
import jakarta.validation.constraints.NotBlank
import swu.pbl.ppap.auth.audit.AuditEntity

@Entity
@Table(
    //테이블에 고유 제약 조건 추가
    //특정 컬럼의 값이 테이블 내에서 중복되지 않도록 보장함.
    // 로그인 id 중복 방지
    uniqueConstraints = [
        UniqueConstraint(name = "uk_login_id", columnNames = ["loginId"])
    ]
)
class UserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var userId: Long? = null, //PK

    @Column(nullable = false, length = 30, updatable = false)
    @field:NotBlank
    val loginId: String,

    @Column(nullable = false, length = 100)
    val password: String,

    @Column(nullable = false, length = 100)
    val confirmPassword: String,

    @Column(nullable = false, length = 30)
    val username: String,

    @Column(nullable = false, length = 30)
    val email: String,

    @Column(nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    val userType: User.UserType, //UserType enum. 개인/법인 구분

    @Column(nullable=false)
    val isActive: Boolean = true,
    @Column(nullable = false)
    val isWithdrawed: Boolean = false

    //TODO: Audit 구현


) : AuditEntity(){

    //Lazy 로딩 설정
    //UserRole의 userEntity 필드가 양방향 관계의 주인.
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userEntity", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    protected val mutableUserRole: MutableList<UserRole> = mutableListOf() //기본적으로 빈 리스트로 초기화, NPE 발생 가능성을 줄임
    val userRole: List<UserRole> get() = mutableUserRole.toList() //데이터를 외부에서 접근해 변경하지 못하도록 함.

}

