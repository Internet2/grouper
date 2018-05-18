/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.morphString.MorphPropertyFileUtils;


/**
 *
 */
public class GrouperThreadLocalStateTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperThreadLocalStateTest("testCryptoThreadlocal"));
  }
  
  /**
   * @param name
   */
  public GrouperThreadLocalStateTest(String name) {
    super(name);
  }

  /**
   */
  public GrouperThreadLocalStateTest() {
    super();
  }

  /**
   * 
   */
  public void testCryptoThreadlocal() {
    
    //dont try if dont have a morphString
    if (GrouperUtil.propertiesFromResourceName("/morphString.properties", true, false) == null) {
      return;
    }
    
    if (GrouperUtil.isBlank(MorphPropertyFileUtils.retrievePropertyString(Morph.ENCRYPT_KEY))) {
      Morph.testMorphKey = "fh43IRJ4Nf5";
    }
    
    try {
      ThreadLocal threadLocalCrypto = (ThreadLocal)GrouperUtil.fieldValue(Crypto.class, null, "threadLocalCrypto", false, true, false);
      threadLocalCrypto.remove();
      if (threadLocalCrypto.get() != null) {
        fail("not null");
      }
      
      Crypto.getThreadLocalCrypto();
      
      threadLocalCrypto = (ThreadLocal)GrouperUtil.fieldValue(Crypto.class, null, "threadLocalCrypto", false, true, false);
  
      if (threadLocalCrypto.get() == null) {
        fail("null");
      }
      
      GrouperThreadLocalState.removeCurrentThreadLocals();
      
      threadLocalCrypto = (ThreadLocal)GrouperUtil.fieldValue(Crypto.class, null, "threadLocalCrypto", false, true, false);
  
      if (threadLocalCrypto.get() != null) {
        fail("not null");
      }
    } finally {
      Morph.testMorphKey = null;
    }
  }
  
}
