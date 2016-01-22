package org.cacheonix.impl.util.logging.xml;

import org.cacheonix.impl.util.logging.LogManager;
import org.cacheonix.impl.util.logging.helpers.FileWatchdog;

final class XMLWatchdog extends FileWatchdog {

   XMLWatchdog(final String filename) {

      super(filename);
   }


   /**
    * Call {@link DOMConfigurator#configure(String)} with the <code>filename</code> to reconfigure log4j.
    */
   public void doOnChange() {

      new DOMConfigurator().doConfigure(filename,
              LogManager.getLoggerRepository());
   }
}
