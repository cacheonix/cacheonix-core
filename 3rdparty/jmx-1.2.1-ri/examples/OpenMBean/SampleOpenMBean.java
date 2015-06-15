/*
 * @(#)file      SampleOpenMBean.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.6
 * @(#)lastedit      03/07/15
 *
 * Copyright 2000-2003 Sun Microsystems, Inc.  All rights reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 * 
 * Copyright 2000-2003 Sun Microsystems, Inc.  Tous droits réservés.
 * Ce logiciel est proprieté de Sun Microsystems, Inc.
 * Distribué par des licences qui en restreignent l'utilisation. 
 *
 */


// JDK imports
//
import java.util.*;
import java.io.*;

// RI imports
//
import javax.management.*;
import javax.management.openmbean.*;


public class SampleOpenMBean implements DynamicMBean {


    // Open MBean Info 
    //
    private OpenMBeanInfoSupport OMBInfo;
    
    // Attributes exposed for management
    //
    private TabularDataSupport tShirts;
    private int		       nbChanges = 0;

    // Custom open types (and related info) used by this Open MBean class
    //
    private static String[]      itemNames = {"model", "color", "size", "price"};
    private static String[]      itemDescriptions = {"TShirt's model name", "TShirt's color", "TShirt's size", "TShirt's price"};
    private static OpenType[]    itemTypes = {SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.FLOAT};
    private static CompositeType tShirtType = null;

    // TShirts are indexed according to their model, color and size:
    private static String[]      indexNames = {"model", "color", "size"};
    private static TabularType   tShirtsType = null;


    // Legal values for TShirt features
    //
    private static String[] legalModels = {"JDMK", "JMX", "JAVA"};
    private static OpenMBeanParameterInfoSupport modelParamInfo;

    private static String[] legalColors = {"black", "white", "red", "green", "blue"};
    private static OpenMBeanParameterInfoSupport colorParamInfo;

    private static String[] legalSizes  = {"S", "M", "L", "XL", "XXL"};
    private static OpenMBeanParameterInfoSupport sizeParamInfo;

    private static float    minPrice    =  9.00f;
    private static float    maxPrice    = 19.99f;
    private static OpenMBeanParameterInfoSupport priceParamInfo;

    
    
    /* *** Static initialization *** */

    static {

	
	// initializes OpenType instances and ParameterInfo instances
	//
	try {

	    // CompositeType instance for a TShirt 
	    //
	    tShirtType = new CompositeType("tShirt",
					   "a TShirt", 
					   itemNames,
					   itemDescriptions,
					   itemTypes);
	    
	    // TabularType instance for the list of TShirts
	    //
	    tShirtsType = new TabularType("tShirts",
					  "List of available TShirts", 
					  tShirtType, // row type
					  indexNames);

	    // Parameter info for the model, color, size and price parameters
	    //
	    modelParamInfo = new OpenMBeanParameterInfoSupport("model", 
							       "Valid TShirt model name. Legal models: "+ Arrays.asList(legalModels).toString(), 
							       SimpleType.STRING,
							       "JMX",        // default model is JMX
							       legalModels); // array of legal models
	    colorParamInfo = new OpenMBeanParameterInfoSupport("color", 
							       "Valid product color. Legal colors: "+ Arrays.asList(legalColors).toString(), 
							       SimpleType.STRING,
							       "white",      // default color is white
							       legalColors); // array of legal colors
	    sizeParamInfo  = new OpenMBeanParameterInfoSupport("size", 
							       "Valid product size. Legal sizes: "+ Arrays.asList(legalSizes).toString(), 
							       SimpleType.STRING,
							       "XL",        // default size is XL
							       legalSizes); // array of legal sizes
	    priceParamInfo = new OpenMBeanParameterInfoSupport("price", 
							       "Valid product price (ranging from $"+ minPrice +" to $"+ maxPrice +")", 
							       SimpleType.FLOAT,
							       null,  // no default price
							       new Float(minPrice),   // Min legal value for price
							       new Float(maxPrice));  // Max legal value for price

	    
	} catch (OpenDataException e) {
	    // should not happen
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    PrintWriter pout = new PrintWriter(bout);
	    e.printStackTrace(pout);
	    pout.flush();
	    throw new RuntimeException(bout.toString());
	}
	
    }
    
    
    /* *** Contructor *** */

    /**
     * Constructs a SampleOpenMBean instance containing an empty TShirts list
     */
    public SampleOpenMBean() throws OpenDataException {

	buildMBeanInfo();

	// Create empty TShirts list
	tShirts = new TabularDataSupport(tShirtsType);
    }


    /* *** Getters *** */

    /**
     * Returns a clone of the TShirts list
     */
    public TabularData getTShirts() {
	return (TabularData) tShirts.clone();
    }

    /**
     * Returns the number of time the TShirts list has been updated
     */
    public Integer getNbChanges() {
	return new Integer(nbChanges);
    }


    /* *** Operations *** */

    /**
     * Adds the tShirt given in parameter to the list of available tShirts, if it does not already exist 
     * and returns Boolean.TRUE if succesful, Boolean.FALSE otherwise. 
     */
    public Boolean addTShirt(CompositeData tShirt) {

	try {
	    tShirts.put(tShirt); //  throws KeyAlreadyExistsException if index for tShirt already exists in tShirts
	    nbChanges++;
	    return Boolean.TRUE;
	} catch (KeyAlreadyExistsException e) {
	    return Boolean.FALSE;
	}
    }


    /**
     * Checks param is a valid value for the specified paramInfo and returns param's value 
     * (returns the default value if param is null and paramInfo defines one), 
     * or throws an OpenDataException otherwise.
     */
    protected Object checkParam(OpenMBeanParameterInfo paramInfo, Object param) throws OpenDataException {

	Object result;

	if ( ! paramInfo.isValue(param) ) {
	    throw new OpenDataException("parameter "+paramInfo.getName()+"'s value ["+param+"] is not valid");
	}
	else if ( param == null && paramInfo.hasDefaultValue() ) {
	    result = paramInfo.getDefaultValue();
	}
	else {
	    result = param;
	}

	return result;
    }

    /**
     * Builds and returns a new CompositeData TShirt instance from the specified parameters. 
     * If parameter values are not legal according to the OpenMBeanParameterInfo instances for this method,
     * it throws an OpenDataException.
     * If model, color or size are null, it uses the default value provided in the OpenMBeanParameterInfo instances for this method.
     */
    public CompositeData buildTShirt(String model, String color, String size, Float price) throws OpenDataException {

	// Check parameter values are legal, assign default if necessary, or throws OpenDataException
	//
	model = (String) checkParam(modelParamInfo, model);
	color = (String) checkParam(colorParamInfo, color);
	size  = (String) checkParam(sizeParamInfo,  size);
	price = (Float) checkParam(priceParamInfo, price);

	Object[] itemValues = {model, color, size, price};
	CompositeData result = new CompositeDataSupport(tShirtType, itemNames, itemValues);

	return result;
    }

    /**
     * Removes the given tshirt from the list if a tshirt with the same index existed in the list,
     * or does nothing otherwise.
     */
    public void removeTShirt(CompositeData tShirt) {
	
	// Calculate index 
	Object[] index = tShirts.calculateIndex(tShirt); 

	// returns removed tshirt, or null if it did not exist 
	// (alternately we could have tested with a containsValue or containsKey call before removing)
	CompositeData removed = tShirts.remove(index);
	if ( removed != null ) {
	    nbChanges++ ;
	}
    }


    /* *** DynamicMBean interface implementation *** */

    /**
     *
     */
    public Object getAttribute(String attribute_name) 
	throws AttributeNotFoundException,
	       MBeanException,
	       ReflectionException {

	if (attribute_name == null) {
	    throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"), 
						 "Cannot call getAttribute with null attribute name");
	}
	if (attribute_name.equals("TShirts")) {
	    return getTShirts();
	} 
	if (attribute_name.equals("NbChanges")) {
	    return getNbChanges();
	}
	throw new AttributeNotFoundException("Cannot find " + attribute_name + " attribute ");
    }

    /**
     *
     */
    public void setAttribute(Attribute attribute) 
	throws AttributeNotFoundException,
	       InvalidAttributeValueException,
	       MBeanException, 
	       ReflectionException {

	throw new AttributeNotFoundException("No attribute can be set in this MBean");
    }

    /**
     *
     */
    public AttributeList getAttributes(String[] attributeNames) {

	if (attributeNames == null) {
	    throw new RuntimeOperationsException(new IllegalArgumentException("attributeNames[] cannot be null"),
						 "Cannot call getAttributes with null attribute names");
	}
	AttributeList resultList = new AttributeList();

	if (attributeNames.length == 0)
	    return resultList;
        
	for (int i=0 ; i<attributeNames.length ; i++){
	    try {        
		Object value = getAttribute((String) attributeNames[i]);     
		resultList.add(new Attribute(attributeNames[i],value));
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return(resultList);
    }

    /**
     *
     */
    public AttributeList setAttributes(AttributeList attributes) {
	return new AttributeList(); // always empty
    }

    /**
     *
     */
    public Object invoke(String operationName, Object[] params, String[] signature)
	throws MBeanException,
	       ReflectionException {

	if (operationName == null) {
	    throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"), 
						 "Cannot call invoke with null operation name");
	}

	// public SimpleData addTShirt(CompositeData tShirt)
	//
	if (operationName.equals("addTShirt")){

	    // check params
	    if (   (params.length != 1) ||
		 ! (params[0] instanceof CompositeData) ) {
		throw new RuntimeOperationsException
		    (new IllegalArgumentException("cannot invoke addTShirt: "+
						  "expecting params[i] instanceof CompositeData for i = 0"),
		     "Wrong content for array Object[] params to invoke addTShirt method");
	    }
	    // invoke  addTShirt
            try {
                return addTShirt( (CompositeData)params[0]);
            } catch (Exception e) {
                throw new MBeanException(e, "invoking addTShirt: "+ e.getClass().getName() +
					 "caught ["+ e.getMessage() +"]");
            }
	}

	// public void removeTShirt(CompositeData tShirt)
	//
	else if (operationName.equals("removeTShirt")){

	    // check params
	    if (   (params.length != 1) ||
		 ! (params[0] instanceof CompositeData) ) {
		throw new RuntimeOperationsException
		    (new IllegalArgumentException("cannot invoke removeTShirt: "+
						  "expecting params[i] instanceof CompositeData for i = 0"),
		     "Wrong content for array Object[] params to invoke removeTShirt method");
	    }
	    // invoke  removeTShirt
            try {
                removeTShirt( (CompositeData)params[0]);
		return null;
            } catch (Exception e) {
                throw new MBeanException(e, "invoking removeTShirt: "+ e.getClass().getName() +
					 "caught ["+ e.getMessage() +"]");
            }
	}

	// public CompositeData buildTShirt(SimpleData model, SimpleData color, SimpleData size, SimpleData price)
	//
	else if ( operationName.equals("buildTShirt") ) {

	    // check params
	    if (   (params.length != 4) ||
		 ! (params[0] instanceof String) ||
		 ! (params[1] instanceof String) ||
		 ! (params[2] instanceof String) ||
		 ! (params[3] instanceof Float)    ) {
		throw new RuntimeOperationsException
		    (new IllegalArgumentException("cannot invoke buildTShirt: "+
						  "expecting params[i] instanceof SimpleData for i = 0 to 3"),
		     "Wrong content for array Object[] params to invoke buildTShirt method");
	    }
	    // invoke  buildTShirt
            try {
                return buildTShirt( (String)params[0], (String)params[1], (String)params[2], (Float)params[3]);
            } catch (Exception e) {
                throw new MBeanException(e, "invoking buildTShirt: "+ e.getClass().getName() +
					 "caught ["+ e.getMessage() +"]");
            }
	}

	else { 
	    throw new ReflectionException(new NoSuchMethodException(operationName), 
					  "Cannot find the operation " + operationName );
	}
    } // invoke

    /**
     *
     */
    public MBeanInfo getMBeanInfo() {
	return OMBInfo;
    }


    /* *** Open MBean Info *** */

    /**
     *
     */
    private void buildMBeanInfo() throws OpenDataException {

	OpenMBeanAttributeInfoSupport[]   attributes    = new OpenMBeanAttributeInfoSupport[2];
	OpenMBeanConstructorInfoSupport[] constructors  = new OpenMBeanConstructorInfoSupport[1];
	OpenMBeanOperationInfoSupport[]   operations    = new OpenMBeanOperationInfoSupport[3];
	MBeanNotificationInfo       []    notifications = new MBeanNotificationInfo[0];
	
	// attribute TShirts (no default or legal values: not supported for tabular types anyway)
	attributes[0] = new OpenMBeanAttributeInfoSupport("TShirts",
							  "List of available T-Shirts",
							  tShirtsType,
							  true,
							  false,
							  false);

	// attribute NbChanges (no default or legal values)
	attributes[1] = new OpenMBeanAttributeInfoSupport("NbChanges",
							  "Number of times the TShirts list has been updated.",
							  SimpleType.INTEGER,
							  true,
							  false,
							  false);
        
	// constructor
	constructors[0] = new OpenMBeanConstructorInfoSupport("SampleOpenMBean",
							      "Constructs a SampleOpenMBean instance containing an empty TShirts list.",
							      new OpenMBeanParameterInfoSupport[0]);
        

	// operation addTShirt
	OpenMBeanParameterInfo[] params_add = new OpenMBeanParameterInfoSupport[1];
	params_add[0] = new OpenMBeanParameterInfoSupport("tShirt", 
							  "a TShirt", 
							  tShirtType);
	operations[0] = new OpenMBeanOperationInfoSupport
	    ("addTShirt",
	     "Adds the tShirt given in parameter to the list of available tShirts "+
	     "if it does not already exist, and returns Boolean.TRUE if succesful, Boolean.FALSE otherwise.",
	     params_add, 
	     SimpleType.BOOLEAN, 
	     MBeanOperationInfo.ACTION);
        
	// operation removeTShirt
	OpenMBeanParameterInfo[] params_remove = params_add;       
	operations[1] = new OpenMBeanOperationInfoSupport
	    ("removeTShirt",
	     "Removes the tShirt given in parameter to the list of available tShirts, " + 
	     "if a tshirt with the same index existed in the list, or does nothing otherwise.",
	     params_remove , 
	     SimpleType.VOID, 
	     MBeanOperationInfo.ACTION);
        
	// operation buildTShirt
	OpenMBeanParameterInfo[] params_build = new OpenMBeanParameterInfoSupport[4];        
	params_build[0] = modelParamInfo ;
	params_build[1] = colorParamInfo ;
	params_build[2] = sizeParamInfo ;
	params_build[3] = priceParamInfo ;
	operations[2] = new OpenMBeanOperationInfoSupport
	    ("buildTShirt",
	     "Builds and returns a CompositeData TShirt instance from the specified parameters. "+
	     "If parameter values are not legal according to the OpenMBeanParameterInfo instances for this method, "+
	     "it throws an OpenDataException. "+
	     "If model, color or size are null, it uses the default value provided in the OpenMBeanParameterInfo instances for this method.",
	     params_build , 
	     tShirtType, 
	     MBeanOperationInfo.INFO);


        // The OpenMBeanInfo
	OMBInfo = new OpenMBeanInfoSupport(this.getClass().getName(),
					   "Sample Open MBean",
					   attributes,
					   constructors,
					   operations,
					   notifications);
    }
}
