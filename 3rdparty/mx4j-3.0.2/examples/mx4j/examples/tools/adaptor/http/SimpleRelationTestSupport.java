/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.examples.tools.adaptor.http;

import javax.management.ObjectName;
import javax.management.relation.InvalidRoleValueException;
import javax.management.relation.RelationSupport;
import javax.management.relation.RelationSupportMBean;
import javax.management.relation.RoleList;

/**
 * @version $Revision: 1.3 $
 */
interface SimpleRelationTestSupportMBean extends RelationSupportMBean
{
}

public class SimpleRelationTestSupport extends RelationSupport implements SimpleRelationTestSupportMBean
{
   public SimpleRelationTestSupport(String relationId, ObjectName relationServiceName, String relationTypeName,
                                    RoleList roleList) throws InvalidRoleValueException, IllegalArgumentException
   {
      super(relationId, relationServiceName, relationTypeName, roleList);
   }
}
