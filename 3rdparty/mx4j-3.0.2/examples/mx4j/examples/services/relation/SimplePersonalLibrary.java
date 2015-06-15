/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.services.relation;

import javax.management.relation.RelationTypeSupport;
import javax.management.relation.RoleInfo;

/**
 * @version $Revision: 1.3 $
 */
public class SimplePersonalLibrary extends RelationTypeSupport
{

   public SimplePersonalLibrary(String relationTypeName)
   {
      super(relationTypeName);

      try
      {
         RoleInfo ownerRoleInfo = new RoleInfo("owner",
                                               // the name of the MBean class of which all members must be an instance.
                                               "mx4j.examples.services.relation.SimpleOwner",
                                               true, //read
                                               true, //write
                                               1, // only one owner
                                               1,
                                               "Owner");
         addRoleInfo(ownerRoleInfo);

         RoleInfo booksRoleInfo = new RoleInfo("books",
                                               "mx4j.examples.services.relation.SimpleBooks",
                                               true,
                                               true,
                                               1, // feeling nasty can only own max 4 books and no fewer than 1
                                               4,
                                               "Books");
         addRoleInfo(booksRoleInfo);
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex.getMessage());
      }
   }
}