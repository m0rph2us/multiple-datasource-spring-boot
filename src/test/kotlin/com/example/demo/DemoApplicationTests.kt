package com.example.demo

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.EntityManagerFactory
import kotlin.test.assertEquals

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	@Qualifier(Sample.ENTITY_MANAGER)
	lateinit var emfSample: EntityManagerFactory

	@Autowired
	lateinit var tbUserRepository: TbUserRepository

	@BeforeEach
	fun setUp() {
		cleanUp()
	}

	@AfterEach
	fun tearDown() {
		cleanUp()
	}

	fun cleanUp() {
		// It is just a use case for the emf to show you how to use it.
		emfSample.createEntityManager().run {
			try {
				transaction.begin().let {
					createNativeQuery("""
                        TRUNCATE tb_user
                    """.trimIndent()).executeUpdate()

					transaction.commit()
				}
			} finally {
				close()
			}
		}
	}

	@Test
	fun `Should retrieve a user record`() {
		tbUserRepository.save(
				TbUser(
						null,
						"morph's id",
						"morph's name",
						"morph@email",
						1,
						"N",
						Date(),
						Date()
				)
		)

		tbUserRepository.findAll().run {
			assertEquals(1, size)

			forEach {
				assertEquals("morph's id", it.userId)
				assertEquals("morph's name", it.name)
				assertEquals("morph@email", it.email)
				assertEquals(1, it.status)
				assertEquals("N", it.deleteYn)
			}
		}
	}

}
