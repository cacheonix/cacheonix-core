package org.springframework.showcase.carplant.dao;

/**
 * Implementation of the CarPartsInventoryTest specific for JDBC.
 * Nothing special here, just a lookup for the JDBC DAO which
 * internally uses the SimpleJdbcTemplate
 * 
 * @author Alef Arendsen
 * @since 2.0.4
 */
public class JdbcCarPartsInventoryTest extends AbstractCarPartsInventoryTest {
	
	protected CarPartsInventory getCarPartsInventory() {
		return (CarPartsInventory)applicationContext.getBean("jdbcCarPartsInventory");
	}
	
	protected void flush() {
	}

}
