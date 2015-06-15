/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.services.relation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.Role;
import javax.management.relation.RoleList;
import javax.management.relation.RoleResult;

import mx4j.log.Log;
import mx4j.log.Logger;

/**
 * @version $Revision: 1.4 $
 */

/**
 * This class will demonstrate the use-case scenarios described in the docs, under chapter "examples" and sub-section RelationService
 * Some methods will also use the MBeanServerInvocationHandler.
 */
public class RelationServiceExample
{
   private MBeanServer m_server = null;
   private RelationServiceMBean m_proxy = null;
   private String m_relationServiceClass = "javax.management.relation.RelationService";
   private String m_libraryClassName = "mx4j.examples.services.relation.SimplePersonalLibrary";
   private ObjectName m_libraryObjectName = null;
   private ObjectName m_relationObjectName = null;
   private SimplePersonalLibrary m_library = null;

   public RelationServiceExample()
   {
      m_server = MBeanServerFactory.createMBeanServer("RelationExample");
   }

   public void setUpRelations()
   {
      // build the object name and register the relationService
      try
      {
         System.out.println("Creating RelationService in the MBeanServer");
         Object[] params = {new Boolean(true)};
         String[] signature = {"boolean"};
         m_relationObjectName = new ObjectName("relations:class=" + m_relationServiceClass);
         m_server.createMBean(m_relationServiceClass, m_relationObjectName, null, params, signature);

         // we will create the proxy now so we can make some simple calls through the proxy
         m_proxy = (RelationServiceMBean)MBeanServerInvocationHandler.newProxyInstance(m_server, m_relationObjectName, RelationServiceMBean.class, false);
         System.out.println("----------------------- done ----------------------------");

         System.out.println("create the relationType");
         String libraryTypeName = "personal_library";
         m_library = new SimplePersonalLibrary(libraryTypeName);
         // add it to the relationService
         addRelationType();
         printRelationTypeInfo();
         System.out.println("----------------------- done ----------------------------");

         System.out.println("create RelationId for the relationType");
         String personalLibraryId = libraryTypeName + "_internal";
         //....Done....
         System.out.println("----------------------- done ----------------------------");

         // we now need to build the Roles and MBeans that will represent those relations
         String ownerClassName = "mx4j.examples.services.relation.SimpleOwner";  // create 2 instance of this
         String bookClassName = "mx4j.examples.services.relation.SimpleBooks";   // create 5 instances of this

         System.out.println("Creating MBeans to represent our relations");
         ObjectName ownerName1 = new ObjectName("library:name=" + ownerClassName + "1");
         ObjectName ownerName2 = new ObjectName("library:name=" + ownerClassName + "2");
         ObjectName bookName1 = new ObjectName("library:name=" + bookClassName + "1");
         ObjectName bookName2 = new ObjectName("library:name=" + bookClassName + "2");
         ObjectName bookName3 = new ObjectName("library:name=" + bookClassName + "3");
         ObjectName bookName4 = new ObjectName("library:name=" + bookClassName + "4");
         ObjectName bookName5 = new ObjectName("library:name=" + bookClassName + "5");

         m_server.createMBean(bookClassName, bookName1, null, new Object[]{"Lord of the rings"}, new String[]{"java.lang.String"});
         m_server.createMBean(bookClassName, bookName2, null, new Object[]{"The Hobbit"}, new String[]{"java.lang.String"});
         m_server.createMBean(bookClassName, bookName3, null, new Object[]{"Harry Potter"}, new String[]{"java.lang.String"});
         m_server.createMBean(bookClassName, bookName4, null, new Object[]{"UML Distilled"}, new String[]{"java.lang.String"});
         m_server.createMBean(bookClassName, bookName5, null, new Object[]{"Applying UML"}, new String[]{"java.lang.String"});

         m_server.createMBean(ownerClassName, ownerName1, null, new Object[]{"Fred"}, new String[]{"java.lang.String"});
         m_server.createMBean(ownerClassName, ownerName2, null, new Object[]{"Humpty Dumpty"}, new String[]{"java.lang.String"});
         System.out.println("----------------------- done ----------------------------");

         System.out.println("Build the roles");
         // build our Lists of values for our first use case an owner registers and takes out one book
         ArrayList ownerList = new ArrayList();
         ownerList.add(ownerName1);  // can only add owner to an owner role can only be 1
         Role ownerRole = new Role("owner", ownerList);

         System.out.println("created owner Role");

         ArrayList bookList = new ArrayList();
         // we can have between 1 and 4 books more than 4 invalidates out relation and less than 1 invalidates it
         bookList.add(bookName1);
         bookList.add(bookName2);
         bookList.add(bookName3);
         Role bookRole = new Role("books", bookList);

         System.out.println("Created book role");
         System.out.println("----------------------- done ----------------------------");

         System.out.println("Creating the relation");
         // add our roles to the RoleList
         RoleList libraryList = new RoleList();
         libraryList.add(ownerRole);
         libraryList.add(bookRole);
         // now to create the relation
         createLibraryRelation(personalLibraryId, libraryTypeName, libraryList);
         System.out.println("Getting all the related info");
         printAllRelationInfo();
         System.out.println("----------------------- done ----------------------------");

         // borrow one book still within our stated quota
         System.out.println("borrow a book we have 3 one more does not invalidate our relation");
         borrowBooks(personalLibraryId, "books", bookName4);
         ArrayList newBookList4 = getRoleValue(personalLibraryId, "books");
         System.out.println("we now have 4 books: " + newBookList4.toString());
         System.out.println("----------------------- done ----------------------------");

         // remove 2 books from the MBeanServer an see if our owner has only 2 left
         System.out.println("2 MBeans removed from the MBeanServer - no problem we still have a valid relation.");
         m_server.unregisterMBean(bookName1);
         m_server.unregisterMBean(bookName2);

         ArrayList newBookList = getRoleValue(personalLibraryId, "books");
         System.out.println("After removing the 2 MBeans we have only 2 Book MBeans left " + newBookList.toString());
         System.out.println("----------------------- done ----------------------------");

         // we will now demonstrate the unhappy scenarios.
         //invalidate the relation and borrow too many books throws InvalidRoleValueException
         // note we cannot add bookName1 or bookName2 as they have been unregistered from the MBeanServer
         // register
         System.out.println("Deregistering the last of our books from the MBeanServer");
         m_server.unregisterMBean(bookName3);
         m_server.unregisterMBean(bookName4);
         System.out.println("----------------------- done ----------------------------");

         System.out.println("Testing access by running queries: ");
         System.out.println("The relation should have been removed and an exception of RelationNotFoundException returned");
         testAllAccessQueries(personalLibraryId);
         System.out.println("----------------------- done ----------------------------");

      }
      catch (Exception ex)
      {
         System.out.println("Could Not create the RelationService: " + ex);
         ex.printStackTrace();
      }
   }

   public void borrowBooks(String relationId, String roleName, ObjectName bookToAdd)
   {
      Logger logger = getLogger();
      try
      {
         // get the old values
         ArrayList oldRoleValue = getRoleValue(relationId, roleName);
         ArrayList newRoleValue = (ArrayList)oldRoleValue.clone();
         newRoleValue.add(bookToAdd);
         // now we update the values
         Role role = new Role(roleName, newRoleValue);
         Object[] params1 = {relationId, role};
         String[] signature1 = {"java.lang.String", "javax.management.relation.Role"};
         m_server.invoke(m_relationObjectName, "setRole", params1, signature1);
      }
      catch (Exception ex)
      {
         logger.error("Unable to add a book");
         ex.printStackTrace();
      }
   }

   private void printList(List list)
   {
      for (Iterator i = list.iterator(); i.hasNext();)
      {
         System.out.println(">>>> Names representing roles: " + i.next());
      }
   }

   private ArrayList getRoleValue(String relationId, String roleName)
   {
      Logger logger = getLogger();
      try
      {
         Object[] params = {relationId, roleName};
         String[] signature = {"java.lang.String", "java.lang.String"};
         return ((ArrayList)(m_server.invoke(m_relationObjectName, "getRole", params, signature)));
      }
      catch (Exception ex)
      {
         logger.error("Unable to get the list of roles for ID: " + relationId);
         return null;
      }
   }

   public void endExample()
   {
      try
      {
         System.out.println("Cleaning up......");
         // this query will return the set of mbeans which have a class attribute of "management*" which is our MBeans
         Set mbeanSet = m_server.queryMBeans(null, Query.initialSubString(Query.classattr(), Query.value("management*")));
         for (Iterator i = mbeanSet.iterator(); i.hasNext();)
         {
            m_server.unregisterMBean(((ObjectInstance)i.next()).getObjectName());
         }
         // release the relationService
         m_server.unregisterMBean(m_relationObjectName);
         // release the MBeanServer
         MBeanServerFactory.releaseMBeanServer(m_server);
         System.exit(0);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         System.exit(1);
      }
   }

   private void addRelationType()
   {
      try
      {
         Object[] params = {m_library};
         String[] signature = {"javax.management.relation.RelationType"};
         m_server.invoke(m_relationObjectName, "addRelationType", params, signature);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   private void printRelationTypeInfo()
   {
      try
      {
         ArrayList relTypeNameList = (ArrayList)(m_server.getAttribute(m_relationObjectName, "AllRelationTypeNames"));
         System.out.println("The RelationType Names found in the RelationService: " + relTypeNameList.toString());
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   private void createLibraryRelation(String personalLibraryId, String libraryTypeName, RoleList libraryList)
   {
      Logger logger = getLogger();
      try
      {
         Object[] params = {personalLibraryId, libraryTypeName, libraryList};
         String[] signature = {"java.lang.String", "java.lang.String", "javax.management.relation.RoleList"};
         m_server.invoke(m_relationObjectName, "createRelation", params, signature);
      }
      catch (Exception ex)
      {
         logger.error("Exception creating Library Relation: " + ex.getMessage());
         ex.printStackTrace();
      }
   }

   private void printAllRelationInfo()
   {
      Logger logger = getLogger();
      try
      {
         ArrayList allRelationIds = (ArrayList)m_server.getAttribute(m_relationObjectName, "AllRelationIds");
         for (Iterator i = allRelationIds.iterator(); i.hasNext();)
         {
            String currentRelationId = (String)i.next();
            System.out.println("All RelationIds: " + currentRelationId);
            testAllAccessQueries(currentRelationId);
         }
      }
      catch (Exception ex)
      {
         logger.error("Unable to print the relations");
         ex.printStackTrace();
      }
   }

   private void testAllAccessQueries(String relationId)
   {
      Logger logger = getLogger();
      // retrieve all roles
      try
      {
         Object[] params = {relationId};
         String[] signature = {"java.lang.String"};
         RoleResult roleResult = (RoleResult)(m_server.invoke(m_relationObjectName, "getAllRoles", params, signature));
         RoleList roleList = roleResult.getRoles();
         for (Iterator i = roleList.iterator(); i.hasNext();)
         {
            Role currentRole = (Role)i.next();
            System.out.println(">>>> role name: " + currentRole.getRoleName());
            System.out.println(">>>> role values: " + currentRole.getRoleValue().toString());
         }
         System.out.println("No unresolved Roles roleUnresolved size: " + roleResult.getRolesUnresolved().size());
      }
      catch (Exception ex)
      {
         logger.error("Exception printing the results from relationId: " + relationId);
         System.out.println("Printing the Exception message to validate exception: " + ex.getMessage());
      }

   }

   private Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }

   public static void main(String[] args)
   {
      RelationServiceExample example = new RelationServiceExample();
      example.setUpRelations();
      example.endExample();
   }
}