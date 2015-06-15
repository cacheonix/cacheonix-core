/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.tools.adaptor.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
import javax.management.relation.RoleResult;

import mx4j.tools.adaptor.http.DefaultProcessor;
import mx4j.tools.adaptor.http.XSLTProcessor;

/**
 * Example as how to use the HttpAdaptor and the XSLTProcessor and the RelationProcessor
 * To use this example please add as arguments to the command line in this order <host><port><path to xsl files>
 *
 * @version $Revision: 1.3 $
 */
public class RelationServiceAdaptor
{
   private MBeanServer m_server = null;
   private RelationServiceMBean m_proxy = null;
   private mx4j.tools.adaptor.http.HttpAdaptor m_adaptor = null;
   private DefaultProcessor m_processor = null;
   private XSLTProcessor m_xsltProcessor = null;
   private ObjectName httpAdaptorObjectName = null;
   private ObjectName processorName = null;
   private ObjectName m_relationServiceObjectName = null;

   public RelationServiceAdaptor()
   {
      m_server = MBeanServerFactory.createMBeanServer("MyAdaptorTests");
      m_adaptor = new mx4j.tools.adaptor.http.HttpAdaptor();
      m_processor = new DefaultProcessor();
      m_xsltProcessor = new XSLTProcessor();
   }

   public void startTests(String[] args)
   {
      int defaultPort = 1999;
      String defaultHost = "localhost";
      String defaultPath = ".";
      if (args.length > 0)
      {
         defaultHost = args[0];
      }
      if (args.length > 1)
      {
         defaultPort = Integer.parseInt(args[1]);
      }
      if (args.length > 2)
      {
         defaultPath = args[2];
      }

      try
      {
         System.out.println("Building the objectNames and registering the HttpAdaptor, and XSLTProcessor");
         // build object names
         httpAdaptorObjectName = new ObjectName("Server:name=HttpAdaptor");
         processorName = new ObjectName("processor:name=XSLTProcessor");

         // register adaptor in server
         m_server.registerMBean(m_adaptor, httpAdaptorObjectName);
         m_server.registerMBean(m_xsltProcessor, processorName);

         m_server.setAttribute(processorName, new Attribute("File", defaultPath));

         m_adaptor.setPort(defaultPort);
         m_adaptor.setHost(defaultHost);
         m_adaptor.setProcessor(m_xsltProcessor);
         m_adaptor.setAuthenticationMethod("none");

         System.out.println("------------------------------------------- done --------------------------------------------");

         System.out.println("starting the adpator and then checking all is active");
         m_adaptor.start();

         if (m_adaptor.isActive())
         {
            System.out.println("Adaptor is active");
            System.out.println("The name of the processor: " + m_adaptor.getProcessor().getName());
         }

         System.out.println("------------------------------------------- done --------------------------------------------");
         System.out.println("Press enter to register relationService");
         waitForEnterPressed();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   public void buildMBeans()
   {
      try
      {
         System.out.println("Building and registering the relationService and 6 MBeans");
         String m_relationServiceClassName = "javax.management.relation.RelationService";
         m_relationServiceObjectName = new ObjectName("relations:type=" + m_relationServiceClassName + "_1");
         Object[] params = {new Boolean(true)};
         String[] signature = {"boolean"};
         m_server.createMBean(m_relationServiceClassName, m_relationServiceObjectName, null, params, signature);

         // create a proxy
         m_proxy = (RelationServiceMBean)MBeanServerInvocationHandler.newProxyInstance(m_server, m_relationServiceObjectName, RelationServiceMBean.class, false);

         System.out.println("Press ENTER to register 6 MBeans");
         waitForEnterPressed();
         String mbeanClassName = "mx4j.examples.tools.adaptor.http.SimpleStandard";
         ObjectName mbeanObjectName1 = new ObjectName("domain:type=SimpleStandard_1");
         ObjectName mbeanObjectName2 = new ObjectName("domain:type=SimpleStandard_2");
         ObjectName mbeanObjectName3 = new ObjectName("domain:type=SimpleStandard_3");
         ObjectName mbeanObjectName4 = new ObjectName("domain:type=SimpleStandard_4");
         ObjectName mbeanObjectName5 = new ObjectName("domain:type=SimpleStandard_5");
         ObjectName mbeanObjectName6 = new ObjectName("domain:type=SimpleStandard_6");

         m_server.createMBean(mbeanClassName, mbeanObjectName1, null);
         m_server.createMBean(mbeanClassName, mbeanObjectName2, null);
         m_server.createMBean(mbeanClassName, mbeanObjectName3, null);
         m_server.createMBean(mbeanClassName, mbeanObjectName4, null);
         m_server.createMBean(mbeanClassName, mbeanObjectName5, null);
         m_server.createMBean(mbeanClassName, mbeanObjectName6, null);
         System.out.println("------------------------------------------- done --------------------------------------------");

         System.out.println("Creating RoleInfos for RelationType");
         RoleInfo[] roleInfos = new RoleInfo[2];
         String roleName1 = "primary";
         roleInfos[0] = new RoleInfo(roleName1, "mx4j.examples.tools.adaptor.http.SimpleStandard", true, true, 1, 1, null);

         String roleName2 = "secondary";
         roleInfos[1] = new RoleInfo(roleName2, "mx4j.examples.tools.adaptor.http.SimpleStandard", true, true, 0, -1, null);

         // create a relation type with those role infos
         String relationTypeName = "Building_relation_view1";
         m_proxy.createRelationType(relationTypeName, roleInfos);

         // creating more relationTypes to test
         String relationTypeName2 = "Testing_2";
         m_proxy.createRelationType(relationTypeName2, roleInfos);

         System.out.println("Creating relationIds for relationTypeName: " + relationTypeName);
         System.out.println("First create the roles...");

         ArrayList roleValue1 = new ArrayList();
         roleValue1.add(mbeanObjectName1);

         Role role1 = new Role(roleName1, roleValue1);

         ArrayList roleValue2 = new ArrayList();
         roleValue2.add(mbeanObjectName2);
         roleValue2.add(mbeanObjectName3);
         roleValue2.add(mbeanObjectName4);

         Role role2 = new Role(roleName2, roleValue2);
         RoleList roleList1 = new RoleList();
         roleList1.add(role1);
         roleList1.add(role2);

         /// testing form here
         ArrayList role5Value = new ArrayList();
         role5Value.add(mbeanObjectName2);

         Role role5 = new Role(roleName1, role5Value);
         ArrayList roleValue5 = new ArrayList();
         roleValue5.add(mbeanObjectName4);

         Role role6 = new Role(roleName2, roleValue5);
         RoleList roleList5 = new RoleList();
         roleList5.add(role5);
         roleList5.add(role6);

         System.out.println("------------------------------------------- done --------------------------------------------");
         System.out.println("Now create relations with ids:::");

         String relationId1 = "relationId_1";
         m_proxy.createRelation(relationId1, relationTypeName, roleList1);

         String relationId2 = "relationId_2";
         m_proxy.createRelation(relationId2, relationTypeName, roleList5);

         String relationId3 = "relationId_3";
         m_proxy.createRelation(relationId3, relationTypeName, roleList1);
         System.out.println("------------------------------------------- done --------------------------------------------");

         System.out.println("creating relationIds for relationtypeName: " + relationTypeName2);

         String relationId4 = "relationId_number2_1";
         m_proxy.createRelation(relationId4, relationTypeName2, roleList1);

         String relationId5 = "relationId_number2_2";
         m_proxy.createRelation(relationId5, relationTypeName2, roleList1);

         String relationId6 = "relationId_number2_3";
         m_proxy.createRelation(relationId6, relationTypeName2, roleList1);
         System.out.println("------------------------------------------- done --------------------------------------------");
         waitForEnterPressed();

         System.out.println("create a relation MBean and add it in the Relation Service");
         String relMBeanClassName = "mx4j.examples.tools.adaptor.http.SimpleRelationTestSupport";
         String relationId7 = "relationId_relationMBean_1";
         ObjectName relMBeanObjName1 = new ObjectName("relationType:name=RelationTypeSupportInstance");
         m_server.createMBean(relMBeanClassName, relMBeanObjName1, null,
                              new Object[]{relationId7, m_relationServiceObjectName, relationTypeName2, roleList1},
                              new String[]{"java.lang.String", "javax.management.ObjectName", "java.lang.String", "javax.management.relation.RoleList"});
         m_proxy.addRelation(relMBeanObjName1);
         System.out.println("------------------------------------------- done --------------------------------------------");
         waitForEnterPressed();
         RoleResult result = m_proxy.getAllRoles(relationId1);
         Iterator i = result.getRoles().iterator();
         while (i.hasNext())
         {
            Role r = (Role)i.next();
            List l = r.getRoleValue();
            Iterator j = l.iterator();
            while (j.hasNext())
            {
               ObjectName objName = (ObjectName)j.next();
               System.out.println("ObjectName: " + objName.getCanonicalName() + " for relationId: " + relationId1);
            }
         }

         RoleResult result2 = m_proxy.getAllRoles(relationId2);
         Iterator i2 = result2.getRoles().iterator();
         while (i2.hasNext())
         {
            Role r = (Role)i2.next();
            List l = r.getRoleValue();
            Iterator j = l.iterator();
            while (j.hasNext())
            {
               ObjectName objName = (ObjectName)j.next();
               System.out.println("ObjectName: " + objName.getCanonicalName() + " for relationId: " + relationId2);
            }
         }

         System.out.println("getting all relationIds");
         List li = m_proxy.getAllRelationIds();
         System.out.println("allrelationIds list: " + li.toString());

         System.out.println("You can view the adaptor at url http://......");
         System.out.println(">>>>>>>>>>>>>>>> PRESS ENTER TO END THE DEMO <<<<<<<<<<<<<<<<<<<<");

         waitForEnterPressed();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   public void endTests()
   {
      try
      {
         m_adaptor.stop();
         Set mbeanSet = m_server.queryMBeans(null, Query.initialSubString(Query.classattr(), Query.value("test*")));
         for (Iterator i = mbeanSet.iterator(); i.hasNext();)
         {
            m_server.unregisterMBean(((ObjectInstance)i.next()).getObjectName());
         }
         // release the relationService
         m_server.unregisterMBean(m_relationServiceObjectName);
         m_server.unregisterMBean(processorName);
         m_server.unregisterMBean(httpAdaptorObjectName);
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

   public static void main(String[] args)
   {
      RelationServiceAdaptor bnb = new RelationServiceAdaptor();
      bnb.startTests(args);
      bnb.buildMBeans();
      bnb.endTests();
   }

   private static void waitForEnterPressed()
   {
      try
      {
         boolean done = false;
         while (!done)
         {
            char ch = (char)System.in.read();
            if (ch < 0 || ch == '\n')
            {
               done = true;
            }
         }
      }
      catch (IOException ex)
      {
         ex.printStackTrace();
      }
      return;
   }
}