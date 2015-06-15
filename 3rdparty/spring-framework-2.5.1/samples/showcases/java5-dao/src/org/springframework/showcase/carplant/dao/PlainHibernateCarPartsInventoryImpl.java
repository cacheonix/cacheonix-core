package org.springframework.showcase.carplant.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.stereotype.Repository;

/**
 * Implementation of the CarPartsInventory interface using the plain Hibernate 3 API.
 * Using this API means we don't automatically get nice Spring 
 * DataAccessExceptions from our data access code. This is why we add the
 * @Repository annotation to our class. Completing the picture is the
 * the {@link PersistenceExceptionTranslationPostProcessor} to create a little
 * proxy that does the translation.
 * 
 * Note that the @Repository annotation does not only work for Hibernate but
 * also for other data access technologies such as JPA.
 * 
 * @author Alef Arendsen
 * @since 2.0.4
 */
@Repository
public class PlainHibernateCarPartsInventoryImpl implements CarPartsInventory {
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void addPart(String model, String number, String name) {
		Part part = new Part();
		part.setModel(model);
		part.setPartNo(number);
		part.setName(name);
		sessionFactory.getCurrentSession().saveOrUpdate(part);
	}
	
	@SuppressWarnings("unchecked")
	public List<Part> getPartsForModel(CarModel model) {
		return (List<Part>)sessionFactory.getCurrentSession().createQuery("from Part p where p.model = ?").setString(0, model.getName()).list();
	}	
	
	public void updatePartStock(String partNo, int i) {
		Part part = (Part)sessionFactory.getCurrentSession().createQuery("from Part p where p.partNo = :number").setString("number", partNo).uniqueResult();
		if (part == null) {
			throw new PartNotFoundException();
		} else {
			part.updateStock(i);
		}
	}
}
