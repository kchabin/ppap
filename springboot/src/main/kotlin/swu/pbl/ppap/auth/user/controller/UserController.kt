package swu.pbl.ppap.auth.user.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import swu.pbl.ppap.auth.user.entity.UserEntity
import swu.pbl.ppap.auth.user.repository.UserRepository
import swu.pbl.ppap.auth.user.service.UserService
import swu.pbl.ppap.openapi.generated.controller.UsersApi
import swu.pbl.ppap.openapi.generated.model.GetNewTokenRequest
import swu.pbl.ppap.openapi.generated.model.LoginDto
import swu.pbl.ppap.openapi.generated.model.User
import swu.pbl.ppap.openapi.generated.model.UserToken

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val userRepository: UserRepository
): UsersApi {

    @PostMapping("/signup")
    override fun createUser(@RequestBody user: User): ResponseEntity<User> {
        val createdUser= userService.signup(user)
        return ResponseEntity.ok(createdUser)
    }

    @PostMapping("/login")
    override fun userLogin(@RequestBody loginDto: LoginDto): ResponseEntity<UserToken> {
        val userToken = userService.login(loginDto)
        return ResponseEntity.ok(userToken)
    }

    @PostMapping("/token/refresh")
    override fun getNewToken(@RequestBody getNewTokenRequest: GetNewTokenRequest): ResponseEntity<UserToken> {
        val refreshToken = getNewTokenRequest.refreshToken
        val loginId = getNewTokenRequest.loginId

        val newToken = userService.getNewToken(refreshToken, loginId)
        return ResponseEntity.ok(newToken)
    }

}