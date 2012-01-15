package edu.internet2.middleware.grouperClient.failover;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig.FailoverStrategy;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * <pre>
 * testing grouper client failover client:
 * 
 * test affinity
 * test active/active random
 * test that first tier comes before second tier
 * test serialize state to file
 * test unserialize state from file
 * test active/standby
 * 
 * </pre>
 * @author mchyzer
 *
 */
public class FailoverClientTest extends TestCase {

  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new FailoverClientTest("testFailoverActiveStandbyLogicAffinity"));
  }

  /**
   * 
   * @param name
   */
  public FailoverClientTest(String name) {
    super(name);
  }
  
  /** failover config */
  private FailoverConfig failoverConfig = null;

  /**
   * set up
   */
  @Override
  protected void setUp() throws Exception {
    
    GrouperClientUtils.grouperClientOverrideMap().clear();

    super.setUp();
    
    this.failoverConfig = new FailoverConfig();
    this.failoverConfig.setAffinitySeconds(10);
    this.failoverConfig.setConnectionNames(GrouperUtil.toList("test1", "test2", "test3"));
    this.failoverConfig.setConnectionNamesSecondTier(GrouperUtil.toList("testTier2_1", "testTier2_2", "testTier2_3"));
    this.failoverConfig.setConnectionType("testConnectionType");
    this.failoverConfig.setExtraTimeoutSeconds(10);
    this.failoverConfig.setFailoverStrategy(FailoverStrategy.activeActive);
    this.failoverConfig.setMinutesToKeepErrors(1);
    this.failoverConfig.setTimeoutSeconds(15);

    GrouperClientUtils.grouperClientOverrideMap().put("grouperClient.saveFailoverStateEverySeconds", "1");
    
  }

  /**
   * 
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    GrouperClientUtils.grouperClientOverrideMap().clear();
  }

  /**
   * this tests that is saves to file periodically, and that it reads from file and maintains state
   */
  public void testFailoverLogicSerialize() {

    //try 20 times, should be the same
    String previousConnectionName = null;
    String currentConnectionName = null;

    //saves to file every second
    GrouperUtil.sleep(2000);

    FailoverClient.initFailoverClient(this.failoverConfig);

    for (int i=0;i<10;i++) {

      currentConnectionName = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {

        /**
         * @see FailoverLogic#logic
         */
        @Override
        public String logic(FailoverLogicBean failoverLogicBean) {
          return failoverLogicBean.getConnectionName();
        }
      });

      assertTrue(!StringUtils.isBlank(currentConnectionName));
      if (!StringUtils.isBlank(previousConnectionName)) {
        //System.out.println("Current connection name: " + currentConnectionName);
        assertEquals(previousConnectionName, currentConnectionName);
      }
      previousConnectionName = currentConnectionName;

      //lets delete the current state
      FailoverClient.instanceMapFromType = null;

      //look at file and make sure it is fresh
      File failoverState = FailoverClient.fileSaveFailoverClientState();
      assertNotNull(failoverState);
      assertTrue(failoverState.getAbsolutePath() + ", last modified: " 
          + new Date(failoverState.lastModified()) + ", current time: " 
          + new Date(System.currentTimeMillis() - 2000)
      + ", " + failoverState.lastModified() + ", " + (System.currentTimeMillis() - 2000),  
      failoverState.lastModified() >= (System.currentTimeMillis() - 2000));
      

      GrouperUtil.sleep(1000);

    }  
    
  }

  
  /**
   * 
   */
  public void testFailoverLogicAffinity() {

    Set<String> connectionNames = new HashSet<String>();
    
    FailoverClient.initFailoverClient(this.failoverConfig);

    //try 20 times, should find each primary at least once, but should keep affinity
    for (int j=0;j<20;j++) {

      //try 20 times, should be the same
      String previousConnectionName = null;
      String currentConnectionName = null;
      
      for (int i=0;i<20;i++) {
      
        currentConnectionName = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
          
          /**
           * @see FailoverLogic#logic
           */
          @Override
          public String logic(FailoverLogicBean failoverLogicBean) {
            return failoverLogicBean.getConnectionName();
          }
        });
        connectionNames.add(currentConnectionName);
        assertTrue(!StringUtils.isBlank(currentConnectionName));
        if (!StringUtils.isBlank(previousConnectionName)) {
          //System.out.println("Current connection name: " + currentConnectionName);
          assertEquals(previousConnectionName, currentConnectionName);
        }
        previousConnectionName = currentConnectionName;
      }  
      
    }


    assertEquals("make sure only all are there:", 3, connectionNames.size());

    assertTrue("make sure each are there", connectionNames.contains("test1"));
    assertTrue("make sure each are there", connectionNames.contains("test2"));
    assertTrue("make sure each are there", connectionNames.contains("test3"));
    
  }
  
  /**
   * 
   */
  public void testFailoverActiveStandbyLogicAffinity() {

    this.failoverConfig.setFailoverStrategy(FailoverStrategy.activeStandby);

    Set<String> connectionNames = new HashSet<String>();
    
    //try 20 times, should find each primary at least once, but should keep affinity
    for (int j=0;j<20;j++) {

      //try 20 times, should be the same
      String previousConnectionName = null;
      String currentConnectionName = null;
      
      FailoverClient.initFailoverClient(this.failoverConfig);
      
      for (int i=0;i<20;i++) {
      
        currentConnectionName = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
          
          /**
           * @see FailoverLogic#logic
           */
          @Override
          public String logic(FailoverLogicBean failoverLogicBean) {
            return failoverLogicBean.getConnectionName();
          }
        });
        connectionNames.add(currentConnectionName);
        assertTrue(!StringUtils.isBlank(currentConnectionName));
        if (!StringUtils.isBlank(previousConnectionName)) {
          //System.out.println("Current connection name: " + currentConnectionName);
          assertEquals(previousConnectionName, currentConnectionName);
        }
        previousConnectionName = currentConnectionName;
      }  
      
    }


    assertEquals("make sure only all are there:", 1, connectionNames.size());

    assertTrue("make sure each are there", connectionNames.contains("test1"));
    
  }
}
