package org.springframework.showcase.carplant.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Traditional  implemetation of the CarPartsInventory interface using
 * the HibernateTemplate API.
 * 
 * @author Alef Arendsen
 * @since 2.0.4
 */
public class TemplateHibernateCarPartsInventoryImpl implements CarPartsInventory {
	
	private HibernateTemplate hibernateTemplate;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}
	
	public void addPart(String model, String number, String name) {
		Part part = new Part();
		part.setName(name);
		part.setPartNo(number);
		part.setModel(model);
		hibernateTemplate.saveOrUpdate(part);
	}
	
	@SuppressWarnings("unchecked")
	public List<Part> getPartsForModel(CarModel carModel) {
		return (List<Part>)hibernateTemplate.find("from Part where model = ?", new Object[] { carModel.getName() });
	}
	
	public void updatePartStock(String partNo, int i) {
		Part part = (Part)hibernateTemplate.load(Part.class, partNo);
		if (part == null) {
			throw new PartNotFoundException();
		}
		part.updateStock(i);
	}

}
