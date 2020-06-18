package com.example.demo

import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
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

	protected lateinit var mvc: MockMvc

	protected lateinit var mockMvcBuilder: DefaultMockMvcBuilder

	@Autowired
	private val context: WebApplicationContext? = null

	@BeforeEach
	fun setUp() {
		mockMvcBuilder = MockMvcBuilders.webAppContextSetup(context!!)
				.addFilter(CharacterEncodingFilter("UTF-8", true))

		mvc = mockMvcBuilder.build()

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

	@Test
	fun `Should retrieve a user record from api call`() {
		mvc.perform(MockMvcRequestBuilders.post("/saveData"))
				.andExpect(MockMvcResultMatchers.status().isOk)

		mvc.perform(MockMvcRequestBuilders.get("/getData"))
				.andExpect(MockMvcResultMatchers.status().isOk)
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]",
													  Matchers.`is`("morph's id")))
	}

}
