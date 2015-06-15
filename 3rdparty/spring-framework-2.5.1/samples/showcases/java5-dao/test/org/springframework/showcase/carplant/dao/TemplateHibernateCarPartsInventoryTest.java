package org.springframework.showcase.carplant.dao;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Implementation of the CarPartsInventory using the HibernateTemplate API.
 * This class does not need an @Repository annotation as opposed to the
 * {@link PlainHibernateCarPartsInventoryImpl} for exception translation
 * since the HibernateTemplate already takes care of this for you.
 * 
 * @author Alef Arendsen
 * @since 2.0.4.
 */
public class TemplateHibernateCarPartsInventoryTest extends AbstractCarPartsInventoryTest {
	
	private HibernateTemplate template;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.template = new HibernateTemplate(sessionFactory);
	}
	
	protected CarPartsInventory getCarPartsInventory() {
		return (CarPartsInventory)applicationContext.getBean("templateHibernateCarPartsInventory");
	}
	
	@Override
	protected void flush() {
		template.flush();
	}

}
