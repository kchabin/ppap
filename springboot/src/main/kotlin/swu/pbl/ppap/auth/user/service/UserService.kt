package swu.pbl.ppap.auth.user.service
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.InvalidClaimException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.crypto.password.PasswordEncoder

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import swu.pbl.ppap.auth.common.authority.JwtTokenProvider
import swu.pbl.ppap.auth.user.entity.UserEntity
import swu.pbl.ppap.auth.user.entity.UserRole
import swu.pbl.ppap.auth.user.repository.UserRepository
import swu.pbl.ppap.auth.user.repository.UserRoleRepository
import swu.pbl.ppap.openapi.generated.model.LoginDto
import swu.pbl.ppap.openapi.generated.model.Role
import swu.pbl.ppap.openapi.generated.model.User
import swu.pbl.ppap.openapi.generated.model.UserToken


@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val passwordEncoder: PasswordEncoder,

    //로그인 구현 시 추가
    private val authenticationManagerBuilder: AuthenticationManagerBuilder,
    private val jwtTokenProvider: JwtTokenProvider,

) {

    // 회원가입
    @Transactional
    fun signup(user: User): User {
        // 이미 등록된 ID인지 확인
        if (isLoginIdExists(user.loginId)) {
            // 이미 존재하는 ID이면 메시지 반환
            throw IllegalArgumentException("중복된 ID입니다.")
        }

        //password 암호화
        val encodedPassword: String = passwordEncoder.encode(user.password)

        // user -> UserEntity 객체
        val userEntity =  convertToUserEntity(user, encodedPassword)


        //권한 저장
        val savedUser = saveUserAndRole(userEntity)

        return convertToUser(savedUser)
    }

    //중복 id 확인
     //회원가입 시 중복 ID 체크는 db에서 하는 게 더 안전하다
    fun isLoginIdExists(loginId: String): Boolean {
        return userRepository.findByLoginId(loginId) != null
    }



    //User 정보와 UserRole 정보 같이 저장
    @Transactional
    fun saveUserAndRole(userEntity: UserEntity) : UserEntity {
        // DB에 저장
        val savedUser = userRepository.save(userEntity)

        //권한 저장
        saveUserRole(savedUser, Role.USER)
        return savedUser
    }
    //UserRole 저장
    fun saveUserRole(userEntity: UserEntity, role: Role) {
        val userRole = UserRole(null, role, userEntity)
        userRoleRepository.save(userRole)
    }


    // User -> UserEntity(JPA객체)
    fun convertToUserEntity(user: User, encodedPassword: String): UserEntity {
        return UserEntity(
            loginId = user.loginId,
            username = user.username,
            password = encodedPassword, // 암호화된 비밀번호 사용
            email = user.email,
            userId = user.userId,
            confirmPassword = user.confirmPassword,
            isActive = true,
            isWithdrawed = false,
            userType = user.userType
        )
    }
    // UserEntity -> User
    fun convertToUser(userEntity: UserEntity): User {
        return User(
            loginId = userEntity.loginId,
            username = userEntity.username,
            password = userEntity.password,
            userId = userEntity.userId,
            email = userEntity.email,
            confirmPassword = userEntity.confirmPassword,
            isActive = userEntity.isActive,
            isWithdrawed = userEntity.isWithdrawed,
            userType = userEntity.userType
        )
    }

    //로그인 토큰 발행
    @Transactional
    fun login(loginDto: LoginDto): UserToken {
        val authenticationToken =
            UsernamePasswordAuthenticationToken(loginDto.loginId, loginDto.password)
        val authentication =
            authenticationManagerBuilder.`object`.authenticate(authenticationToken)

        val createdToken: UserToken = jwtTokenProvider.createToken(authentication)

        return createdToken
    }

//    @CacheEvict(cacheNames = ["tokenCache"], key = "#loginId")
//    fun deleteAllRefreshTokens(loginId: String)
//        println("모든 리프레시 토큰 삭제")
//    }

    @CachePut(cacheNames = ["tokenCache"], key = "#loginId")
    fun getNewToken(refreshToken: String?, loginId: String?): UserToken {
        // 1. 리프레시 토큰의 유효성을 검사
        try {
            jwtTokenProvider.getRefreshTokenClaims(refreshToken.toString())
        }catch (e: Exception){
            throw when(e) {
                is ExpiredJwtException -> RuntimeException("Refresh JWT token has expired.")
                is SecurityException -> RuntimeException("Security exception occurred while processing the JWT.")
                is MalformedJwtException -> RuntimeException("The JWT provided is malformed.")
                is UnsupportedJwtException -> RuntimeException("The JWT is unsupported.")
                is IllegalArgumentException -> RuntimeException("An illegal argument was provided.")
                else -> RuntimeException("An unknown error occurred: ${e.message}")
            }
        }

        // 2. 새로운 액세스 토큰을 생성
        val userToken: UserToken = jwtTokenProvider.validateRefreshTokenAndCreateToken(refreshToken.toString())

        // 3. 새로운 사용자 토큰을 반환
        return userToken
    }

//    // User 정보를 Redis에 캐시하는 메서드
//    @Cacheable(cacheNames = ["userCache"], key = "#loginId")
//    fun getUserInfo(): User {
//
//    }




}