/*
 * @author mchyzer
 * $Id: Hib3GrouperDdlTest.java,v 1.1 2008-05-13 07:11:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader.db;

import edu.internet2.middleware.grouper.loader.util.GrouperLoaderHibUtils;
import junit.framework.TestCase;


/**
 *
 */
public class Hib3GrouperDdlTest extends TestCase {

  /**
   * Constructor for Hib3GrouperDdlTest.
   * 
   * @param arg0
   */
  public Hib3GrouperDdlTest(String arg0) {
    super(arg0);
  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(Hib3GrouperDdlTest.class);
  }

  /**
   * 
   */
  public void testPersistence() {

    String testObjectName = "unitTestingOnlyIgnore";
    
    //clean up before test
    Hib3GrouperDdl hib3GrouperDdl = GrouperLoaderHibUtils.select(Hib3GrouperDdl.class, 
        "from Hib3GrouperDdl where objectName = '" + testObjectName + "'");
    if (hib3GrouperDdl != null) {
      GrouperLoaderHibUtils.delete(hib3GrouperDdl);
      hib3GrouperDdl = null;
    }
    
    hib3GrouperDdl = new Hib3GrouperDdl();
    hib3GrouperDdl.setDbVersion(-5);
    hib3GrouperDdl.setJavaVersion(-10);
    hib3GrouperDdl.setObjectName(testObjectName);

    assertNull("Not stored, no id", hib3GrouperDdl.getId());
    GrouperLoaderHibUtils.store(hib3GrouperDdl);
    assertNotNull("Stored, should have id", hib3GrouperDdl.getId());
    
    //try an update
    hib3GrouperDdl.setJavaVersion(-12);
    GrouperLoaderHibUtils.store(hib3GrouperDdl);
    
    //now clean up, just delete
    GrouperLoaderHibUtils.delete(hib3GrouperDdl);
  }
  
}
