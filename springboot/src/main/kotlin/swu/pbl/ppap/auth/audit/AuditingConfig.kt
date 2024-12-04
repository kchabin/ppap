package swu.pbl.ppap.auth.audit

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import swu.pbl.ppap.auth.common.authority.CustomUser
import swu.pbl.ppap.auth.user.entity.UserEntity
import swu.pbl.ppap.openapi.generated.model.User
import java.util.*

@Configuration
@EnableJpaAuditing
class AuditingConfig {

    @Bean
    fun auditorAware(): AuditorAware<String> {
        return AuditorAware<String> {
            Optional.ofNullable(SecurityContextHolder.getContext())
                .map { it.authentication }
                .filter { it.isAuthenticated && !it.name.equals("anonymousUser") }
                .map { it.principal as CustomUser }
                .map { it.username }
        }
    }

}

//class AuditorAwareImpl : AuditorAware<String> {
//    private val logger = LoggerFactory.getLogger(AuditorAwareImpl::class.java)
//
//    override fun getCurrentAuditor(): Optional<String> {
//        val authentication = SecurityContextHolder.getContext().authentication
//        if (authentication == null || !authentication.isAuthenticated) {
//            logger.warn("No authenticated user found.")
//            return Optional.of("Unknown")
//        }
//
//        val user = authentication.principal as? CustomUser // Replace with your UserDetails implementation
//        val username = user?.username ?: "Unknown"
//        logger.info("Current Auditor: $username")
//        return Optional.of(username)
//    }
//
//}