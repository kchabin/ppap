package swu.pbl.ppap.auth.common.authority

data class TokenInfo(
    val grantType: String, //jwt 권한 인증 타입
    val accessToken: String, //실제 검증 시 확인할 토큰
    val refreshToken: String,
)
