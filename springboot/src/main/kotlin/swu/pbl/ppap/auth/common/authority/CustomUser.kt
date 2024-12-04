package swu.pbl.ppap.auth.common.authority

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomUser(
    loginId: String,
    password: String,
    authorities: Collection<GrantedAuthority>,
) : User(loginId, password, authorities) {
}
// 스프링 시큐리티의 User(UserDetails) 객체를 상속하여 Custom User 객체를 만든다.
// CustomUser로 Token에 loginId를 관리