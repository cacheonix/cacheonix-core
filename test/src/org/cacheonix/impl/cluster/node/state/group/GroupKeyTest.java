package org.cacheonix.impl.cluster.node.state.group;

import junit.framework.TestCase;

/**
 * A tester for {@link GroupKey}.
 */
public class GroupKeyTest extends TestCase {

   @SuppressWarnings("ObjectEqualsNull")
   public void testEquals() throws Exception {

      assertEquals(new GroupKey(1, "test"), new GroupKey(1, "test"));
      assertFalse(new GroupKey(1, "test").equals(new GroupKey(2, "test")));
      assertFalse(new GroupKey(1, "test").equals(new GroupKey(1, "test2")));
      final GroupKey same = new GroupKey(1, "test");
      assertEquals(same, same);
   }


   public void testToString() throws Exception {

      assertNotNull(new GroupKey(1, "test").toString());
   }


   public void testHashCode() throws Exception {

      assertEquals(3556527, new GroupKey(1, "test").hashCode());
   }
}