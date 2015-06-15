package org.springframework.showcase.carplant.dao;

import java.util.List;

/**
 * A DAO capable of retrieving parts for building a car.
 * Implementing using {@link JdbcCarPartsInventoryImpl JDBC},
 * using {@link PlainHibernateInventoryImpl Hibernate using the plain Session API}
 * and {@link TemplateHibernateInventoryImpl Hibernate using the template API},
 * showing various Spring 2 and Java5 techniques.
 * 
 * Sample originally described in a bigger sample application available from
 * http://blog.interface21.com/main/2007/03/12/carplant-not-accepting-null-carmodels/
 *  
 * @author Alef Arendsen
 * @since 2.0.4
 */
public interface CarPartsInventory {
	
	/** Retrieves a list of Parts specific for a CarModel */
	public List<Part> getPartsForModel(CarModel defaultCarModel);
	
	/** Updates stock for a specific part */
	public void updatePartStock(String partNo, int i);
	
	/** Adds a new part to the inventory */
	public void addPart(String model, String number, String name);

}
