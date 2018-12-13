package com.example.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.UUID;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {PersonRepositoryTest.Initializer.class})
class PersonRepositoryTest {

	private static final PostgreSQLContainer DB = new PostgreSQLContainer("postgres:latest");

	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			TestPropertyValues.of(
					"spring.datasource.url=" + DB.getJdbcUrl(),
					"spring.datasource.username=" + DB.getUsername(),
					"spring.datasource.password=" + DB.getPassword()
			).applyTo(applicationContext);
		}
	}

	@BeforeAll
	static void beforeAll() {
		DB.start();
	}

	@AfterAll
	static void afterAll() {
		DB.stop();
	}

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void findById() {
		// Given
		Person p = new Person();
		p.setName("Mickey");
		UUID id = personRepository.save(p).getId();

		// When
		Person person = personRepository.select(id);

		// Then
		Assertions.assertEquals(id, person.getId());
		Assertions.assertEquals("Mickey", person.getName());
	}

	@Test
	void selectName() {
		// Given
		Person p = new Person();
		p.setName("Donald");
		UUID id = personRepository.save(p).getId();

		// When
		PersonName personName = personRepository.selectName(id);

		// Then
		Assertions.assertEquals("Donald", personName.getName());
	}

	@Test
	void selectId() {
		// Given
		Person p = new Person();
		p.setName("Daisy");
		UUID id = personRepository.save(p).getId();

		// When
		PersonId personId = personRepository.selectId(id);

		// Then
		Assertions.assertEquals(id, personId.getId());
	}


	@Test
	void selectJpaAll() {

		Person p = new Person();
		p.setName("Daisy");
		UUID id = personRepository.save(p).getId();

		UUID reloadedId = entityManager.createQuery("select id from Person ", UUID.class).getSingleResult();

		Assertions.assertEquals(id, reloadedId);
	}

	@Test
	void selectJpaId() {

		Person p = new Person();
		p.setName("Daisy");
		UUID id = personRepository.save(p).getId();

		UUID reloadedId = entityManager //
				.createQuery("select id from Person where id = :id", UUID.class) //
				.setParameter("id", id)
				.getSingleResult();

		Assertions.assertEquals(id, reloadedId);
	}


	@Test
	void selectJpaNoTargetClass() {

		Person p = new Person();
		p.setName("Daisy");
		UUID id = personRepository.save(p).getId();

		Object reloadedId = entityManager //
				.createQuery("select id from Person") //
				.getSingleResult();

		Assertions.assertEquals(id, reloadedId);
	}

	@Test
	void selectNativeJpaNoTargetClass() {

		Person p = new Person();
		p.setName("Daisy");
		UUID id = personRepository.save(p).getId();

		Object reloadedId = entityManager //
				.createNativeQuery("select id from Person") //
				.getSingleResult();

		Assertions.assertEquals(id, reloadedId);
	}

}
