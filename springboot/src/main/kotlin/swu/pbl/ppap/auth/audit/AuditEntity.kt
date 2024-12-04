package swu.pbl.ppap.auth.audit

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime



@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
abstract class AuditEntity {
    @CreatedDate
    @Column(updatable = false)
    //private val createdDate: LocalDateTime = LocalDateTime.now()
    //protected var createdDate: LocalDateTime? = null
    protected var createdDate: LocalDateTime? = LocalDateTime.MIN


    @CreatedBy
    @Column(updatable = false)
    var createdBy: String? = null

    @LastModifiedDate
    @Column
    //private var modifiedDate: LocalDateTime = LocalDateTime.now()
    protected var modifiedDate: LocalDateTime? = LocalDateTime.MIN

    @LastModifiedBy
    @Column(nullable = true)
    var modifiedBy: String? = null
}