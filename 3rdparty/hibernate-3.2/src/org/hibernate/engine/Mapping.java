//$Id: Mapping.java 7586 2005-07-21 01:11:52Z oneovthafew $
package org.hibernate.engine;

import org.hibernate.MappingException;
import org.hibernate.type.Type;

/**
 * Defines operations common to "compiled" mappings (ie. <tt>SessionFactory</tt>)
 * and "uncompiled" mappings (ie. <tt>Configuration</tt>) that are used by
 * implementors of <tt>Type</tt>.
 *
 * @see org.hibernate.type.Type
 * @see org.hibernate.impl.SessionFactoryImpl
 * @see org.hibernate.cfg.Configuration
 * @author Gavin King
 */
public interface Mapping {
	public Type getIdentifierType(String className) throws MappingException;
	public String getIdentifierPropertyName(String className) throws MappingException;
	public Type getReferencedPropertyType(String className, String propertyName) throws MappingException;
}







