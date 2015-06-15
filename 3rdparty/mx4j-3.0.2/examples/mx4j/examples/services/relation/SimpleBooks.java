/**
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */
package mx4j.examples.services.relation;

/**
 * @version $Revision: 1.3 $
 */
interface SimpleBooksMBean
{
   public void setBook(String bookName);

   public String getBook();
}

public class SimpleBooks implements SimpleBooksMBean
{
   private String m_name = null;

   public SimpleBooks(String bookName)
   {
      m_name = bookName;
   }

   public void setBook(String bookName)
   {
      m_name = bookName;
   }

   public String getBook()
   {
      return m_name;
   }
}