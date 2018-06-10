package org.cacheonix.impl.cache.web;

import java.util.Arrays;
import javax.servlet.ServletOutputStream;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * A tester for {@link ServletOutputStreamWrapper}.
 */
public final class ServletOutputStreamWrapperTest extends TestCase {


   private static final int BUFFER_SIZE = 1234;

   private static final byte[] BYTES = "Test bytes".getBytes();

   private ServletOutputStreamWrapper servletOutputStreamWrapper;

   private ServletOutputStream delegate;


   public void testWrite() throws Exception {

      servletOutputStreamWrapper.write(BYTES);
      verify(delegate).write(BYTES[0]);
      verify(delegate, times(2)).write(BYTES[BYTES.length - 1]);

   }


   public void testGetByteOutput() throws Exception {

      servletOutputStreamWrapper.write(BYTES);
      assertTrue(Arrays.equals(BYTES, servletOutputStreamWrapper.getByteOutput()));
   }


   public void testToString() {

      assertNotNull(servletOutputStreamWrapper.toString());

   }


   public void setUp() throws Exception {

      super.setUp();

      delegate = mock(ServletOutputStream.class);
      servletOutputStreamWrapper = new ServletOutputStreamWrapper(delegate, BUFFER_SIZE);
   }


   public void tearDown() throws Exception {

      servletOutputStreamWrapper = null;
      delegate = null;

      super.tearDown();
   }


   @SuppressWarnings("ObjectToString")
   public String toString() {

      return "ServletOutputStreamWrapperTest{" +
              "servletOutputStreamWrapper=" + servletOutputStreamWrapper +
              ", delegate=" + delegate +
              "} " + super.toString();
   }
}