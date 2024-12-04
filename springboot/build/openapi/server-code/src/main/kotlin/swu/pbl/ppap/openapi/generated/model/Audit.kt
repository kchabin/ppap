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
 * 
 * @param createdDate 생성일
 * @param createdBy 생성자
 * @param modifiedDate 수정일
 * @param modifiedBy 수정자
 */
data class Audit(

    @Schema(example = "null", description = "생성일")
    @field:JsonProperty("createdDate") val createdDate: java.time.LocalDateTime? = null,

    @Schema(example = "null", description = "생성자")
    @field:JsonProperty("createdBy") val createdBy: kotlin.String? = null,

    @Schema(example = "null", description = "수정일")
    @field:JsonProperty("modifiedDate") val modifiedDate: java.time.LocalDateTime? = null,

    @Schema(example = "null", description = "수정자")
    @field:JsonProperty("modifiedBy") val modifiedBy: kotlin.String? = null
) {

}

