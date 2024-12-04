package swu.pbl.ppap.auth.common.authority
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import swu.pbl.ppap.openapi.generated.model.User
import swu.pbl.ppap.openapi.generated.model.UserToken
import java.util.Date
import javax.crypto.SecretKey


const val ACCESS_EXPIRATION_TIME: Long = 1000 * 60 * 30 //1시간
const val REFRESH_EXPIRATION_TIME: Long = 1000 * 60 * 60 //2시간

@Component
class JwtTokenProvider {

    @Value("\${spring.jwt.access-secret}")
    private lateinit var accessSecret: String

    @Value("\${spring.jwt.refresh-secret}")
    private lateinit var refreshSecret: String

    private lateinit var accessKey: SecretKey
    private lateinit var refreshKey: SecretKey

    @PostConstruct
    fun init() {

        if (accessSecret.isBlank() || refreshSecret.isBlank()) {
            throw IllegalArgumentException("Access secret or refresh secret must be set")
        }
        accessKey = getSigningAccessKey()
        refreshKey = getSigningRefreshKey()
    }

    private fun getSigningAccessKey(): SecretKey{
        var accessKeyBytes: ByteArray? = Decoders.BASE64.decode(accessSecret)
        return Keys.hmacShaKeyFor(accessKeyBytes)
    }
    private fun getSigningRefreshKey(): SecretKey{
        var refreshKeyBytes: ByteArray? = Decoders.BASE64.decode(refreshSecret)
        return Keys.hmacShaKeyFor(refreshKeyBytes)
    }





    //token 생성
    fun createToken(authentication: Authentication): UserToken {
        val authorities: String = authentication
            .authorities
            .joinToString(",", transform = GrantedAuthority::getAuthority)

        val now = Date()
        val accessExpiration = Date(now.time + ACCESS_EXPIRATION_TIME )
        val refreshExpiration = Date(now.time + REFRESH_EXPIRATION_TIME )

        val accessToken = Jwts.builder()
            .subject(authentication.name)
            .claim("auth", authorities)
            .issuedAt(now)
            .expiration(accessExpiration)
            .signWith(accessKey)
            .compact()
        val refreshToken = Jwts.builder()
            .subject(authentication.name)
            .claim("auth", authorities)
            .issuedAt(now)
            .expiration(refreshExpiration)
            .signWith(refreshKey)
            .compact()
        //생성자 파라미터를 잘못 전달함
        //return UserToken("Bearer", accessToken,  refreshToken)
        return UserToken(accessToken, refreshToken, "Bearer", loginId = authentication.name )
    }

    //token 정보 추출
    fun getAuthentication(token: String?): Authentication {
        val claims: Claims = getAccessTokenClaims(token)
        val auth = claims["auth"] ?: throw RuntimeException("Wrong Token")

        //권한 정보 추출
        val authorities: Collection<GrantedAuthority> = (auth as String)
            .split(",")
            .map { SimpleGrantedAuthority(it)}

        val principal = CustomUser(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, "", authorities)
    }

    fun validateRefreshTokenAndCreateToken(refreshToken: String) : UserToken {
        try {
            //사용자 정보 추출
            val refreshClaims: Claims = getRefreshTokenClaims(refreshToken)
            val now = Date()

            //claims에서 추출한 사용자 정보를 사용해 새로운 access token 발급
            val newAccessToken = Jwts.builder()
                .subject(refreshClaims.subject)
                .claim("auth", refreshClaims["auth"])
                .issuedAt(now)
                .expiration(Date(now.time + ACCESS_EXPIRATION_TIME ))
                .signWith(accessKey)
                .compact()

            //새로운 refresh token 발급
            val newRefreshToken = Jwts.builder()
                .subject(refreshClaims.subject)
                .claim("auth", refreshClaims["auth"])
                .issuedAt(now)
                .expiration(Date(now.time + REFRESH_EXPIRATION_TIME ))
                .signWith(refreshKey)
                .compact()

            return UserToken("Bearer", newAccessToken,  newRefreshToken, loginId = refreshClaims.subject)
        } catch (e: Exception) {
            throw e
        }
    }
    //
    fun validateAccessTokenForFilter(token: String?) : Boolean {
        try {
            getAccessTokenClaims(token)
            return true
        } catch (e: Exception) {
            when(e) {
                is SecurityException -> {}
                is MalformedJwtException -> {}
                is ExpiredJwtException -> {}
                is UnsupportedJwtException -> {}
                is IllegalArgumentException -> {}  //JWT Claims string is empty
                else -> {}
            }
            throw e
        }
    }
    fun getAccessTokenClaims(token: String?): Claims =
        Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(token)
            .payload

    fun getRefreshTokenClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(refreshKey)
            .build()
            .parseSignedClaims(token)
            .payload

}