package swu.pbl.ppap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableCaching //캐시 기능 사용하기
//@EnableJpaAuditing //Audit 기능 사용하기
@SpringBootApplication
class PpapApplication

fun main(args: Array<String>) {
	runApplication<PpapApplication>(*args)
}
