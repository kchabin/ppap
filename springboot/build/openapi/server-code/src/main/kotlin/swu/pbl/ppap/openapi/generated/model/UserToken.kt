package swu.pbl.ppap.openapi.generated.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
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
 * 사용자 json web token
 * @param accessToken access token
 * @param refreshToken refresh token
 */
data class UserToken(

    @Schema(example = "null", required = true, description = "access token")
    @field:JsonProperty("accessToken", required = true) val accessToken: kotlin.String,

    @Schema(example = "null", required = true, description = "refresh token")
    @field:JsonProperty("refreshToken", required = true) val refreshToken: kotlin.String
) {

}

