package swu.pbl.ppap.openapi.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import swu.pbl.ppap.openapi.generated.model.Audit
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Email
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import javax.validation.Valid
import io.swagger.v3.oas.annotations.media.Schema

/**
 * user info
 * @param loginId 사용자 아이디
 * @param username 사용자 이름
 * @param password 사용자 비밀번호
 * @param confirmPassword 사용자 비밀번호 확인
 * @param email 사용자 이메일
 * @param userType 
 * @param userId user 테이블 pk
 * @param isActive 사용 여부
 * @param isWithdrawed 탈퇴 여부
 * @param audit 
 */
data class User(

    @Schema(example = "null", required = true, description = "사용자 아이디")
    @field:JsonProperty("loginId", required = true) val loginId: kotlin.String,

    @Schema(example = "null", required = true, description = "사용자 이름")
    @field:JsonProperty("username", required = true) val username: kotlin.String,

    @Schema(example = "null", required = true, description = "사용자 비밀번호")
    @field:JsonProperty("password", required = true) val password: kotlin.String,

    @Schema(example = "null", required = true, description = "사용자 비밀번호 확인")
    @field:JsonProperty("confirmPassword", required = true) val confirmPassword: kotlin.String,

    @Schema(example = "null", required = true, description = "사용자 이메일")
    @field:JsonProperty("email", required = true) val email: kotlin.String,

    @Schema(example = "null", required = true, description = "")
    @field:JsonProperty("userType", required = true) val userType: User.UserType,

    @Schema(example = "null", description = "user 테이블 pk")
    @field:JsonProperty("userId") val userId: kotlin.Long? = null,

    @Schema(example = "null", description = "사용 여부")
    @field:JsonProperty("isActive") val isActive: kotlin.Boolean? = true,

    @Schema(example = "null", description = "탈퇴 여부")
    @field:JsonProperty("isWithdrawed") val isWithdrawed: kotlin.Boolean? = false,

    @field:Valid
    @Schema(example = "null", description = "")
    @field:JsonProperty("audit") val audit: Audit? = null
) {

    /**
    * 
    * Values: iNDI,cORP
    */
    enum class UserType(val value: kotlin.String) {

        @JsonProperty("INDI") iNDI("INDI"),
        @JsonProperty("CORP") cORP("CORP")
    }

}

