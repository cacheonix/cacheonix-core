package org.springframework.showcase.carplant.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * Abstract class defining test behavior for all types of CarPartInventories.
 * Is subclassed by individual implementations for for example Hibernate and JDBC.
 *  
 * @author Alef Arendsen
 * @since 2.0.4
 */
public abstract class AbstractCarPartsInventoryTest extends AbstractTransactionalDataSourceSpringContextTests {
	
	private static final String DDL = 
			"CREATE TABLE t_car_part (" +
			"    name VARCHAR(32)," +
			"    number VARCHAR(32) NOT NULL PRIMARY KEY," +
			"    model VARCHAR(32)," +
			"    stock INTEGER)";
	
	@Override
	protected String[] getConfigLocations() {
		return new String[] { "java5-dao.xml" };
	}
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		SimpleJdbcTemplate template = new SimpleJdbcTemplate((DataSource)applicationContext.getBean("dataSource"));
		template.update(DDL);
	}
	
	@Override
	protected void onTearDownAfterTransaction() throws Exception {
		jdbcTemplate.update("DROP TABLE t_car_part");
	}
	
	protected abstract CarPartsInventory getCarPartsInventory();
	
	protected abstract void flush();
	
	public void testAddPart() {
		int oldCount = countParts();		
		getCarPartsInventory().addPart("SuperHummer", "GTYUI-1234788", "Nut");
		flush();
		assertEquals(oldCount + 1, countParts());
		assertEquals("Nut", jdbcTemplate.queryForObject("select name from t_car_part where number = ?", new Object[] { "GTYUI-1234788" }, String.class));
		assertEquals(0, jdbcTemplate.queryForInt("select stock from t_car_part where number = ?", new Object[] { "GTYUI-1234788" }));
	}
	
	public void testUpdateStock() {	
		getCarPartsInventory().addPart("SuperHummer", "GTYUI-1234", "Nut");
		flush();
		getCarPartsInventory().updatePartStock("GTYUI-1234", 10);
		flush();
		assertEquals(10, jdbcTemplate.queryForInt("select stock from t_car_part where number = ?", new Object[] { "GTYUI-1234" }));
	}
	
	// TODO fix this. The exception is thrown, but somehow the test still fails!?
//	@ExpectedException(PartNotFoundException.class)
//	public void testUpdateStockForNonExistingPart() {
//		getCarPartsInventory().updatePartStock("GJGJGJTT-1234", 10);
//	}
	
	public void testGetPartsForModel() {
		getCarPartsInventory().addPart("SuperHummer1234", "GTYUI-1238", "Nut");
		getCarPartsInventory().addPart("SuperHummer1234", "GTYUI-1239", "Bolt");
		getCarPartsInventory().addPart("MiniHummer", "GTYUI-1237", "Bolt");
		flush();
		
		CarModel model = new CarModel();
		model.setName("SuperHummer1234");
		List<Part> parts = getCarPartsInventory().getPartsForModel(model);
		
		assertEquals(2, parts.size());
		// both need to be super hummer parts
		Part p = parts.get(0);
		assertEquals("SuperHummer1234", p.getModel());
		p = parts.get(1);
		assertEquals("SuperHummer1234", p.getModel());
	}
	
	private int countParts() {
		return jdbcTemplate.queryForInt("select count(*) from t_car_part");
	}

}
