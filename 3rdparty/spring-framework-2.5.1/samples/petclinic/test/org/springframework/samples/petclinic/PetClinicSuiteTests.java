package org.springframework.samples.petclinic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.springframework.samples.petclinic.hibernate.HibernateClinicTests;
import org.springframework.samples.petclinic.jdbc.SimpleJdbcClinicTests;
import org.springframework.samples.petclinic.jpa.EntityManagerClinicTests;
import org.springframework.samples.petclinic.jpa.HibernateEntityManagerClinicTests;
import org.springframework.samples.petclinic.jpa.OpenJpaEntityManagerClinicTests;

/**
 * <p>
 * JUnit 4 based test suite for all PetClinic tests.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.5
 */
@RunWith(Suite.class)
@SuiteClasses({
		OwnerTests.class,
		SimpleJdbcClinicTests.class,
		HibernateClinicTests.class,
		EntityManagerClinicTests.class,
		HibernateEntityManagerClinicTests.class,
		OpenJpaEntityManagerClinicTests.class
})
public class PetClinicSuiteTests {

}
