/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouperClient.failover;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig.FailoverStrategy;
import edu.internet2.middleware.grouperClient.util.GrouperClientCommonUtils;
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
 * test errors
 * test threads on startup
 * test timeouts
 * test minutes to keep errors
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
    TestRunner.run(new FailoverClientTest("testFailoverLogicAffinityWithErrors"));
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
   * 
   */
  private List<String> firstTierConnections = GrouperUtil.toList("test1", "test2", "test3");
  
  /**
   * 
   */
  private List<String> secondTierConnections = GrouperUtil.toList("testTier2_1", "testTier2_2", "testTier2_3");
  
  /**
   * set up
   */
  @Override
  protected void setUp() throws Exception {
    
    GrouperClientUtils.grouperClientOverrideMap().clear();

    super.setUp();
    
    this.failoverConfig = new FailoverConfig();
    this.failoverConfig.setAffinitySeconds(10);
    this.failoverConfig.setConnectionNames(this.firstTierConnections);
    this.failoverConfig.setConnectionNamesSecondTier(this.secondTierConnections);
    this.failoverConfig.setConnectionType("testConnectionType");
    this.failoverConfig.setExtraTimeoutSeconds(10);
    this.failoverConfig.setFailoverStrategy(FailoverStrategy.activeActive);
    this.failoverConfig.setMinutesToKeepErrors(1);
    this.failoverConfig.setTimeoutSeconds(15);
    this.failoverConfig.setSecondsForClassesToLoad(0);

    GrouperClientUtils.grouperClientOverrideMap().put("grouperClient.saveFailoverStateEverySeconds", "1");
    
    //lets delete the cached state file
    GrouperClientCommonUtils.deleteFile(FailoverClient.fileSaveFailoverClientState());
    FailoverClient.instanceMapFromType = null;
    
  }

  /**
   * 
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    GrouperClientUtils.grouperClientOverrideMap().clear();
    GrouperClientCommonUtils.deleteFile(FailoverClient.fileSaveFailoverClientState());
    FailoverClient.instanceMapFromType = null;
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
          assertTrue(failoverLogicBean.isRunningInNewThread());
          assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
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
            assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
            assertTrue(failoverLogicBean.isRunningInNewThread());
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
  public void testFailoverLogicAffinityWithErrors() {

    FailoverClient.initFailoverClient(this.failoverConfig);

    final String[] firstConnection = new String[1];
    final String[] secondConnection = new String[1];
    final String[] thirdConnection = new String[1];

    String firstConnectionResult = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {

      /**
       * @see FailoverLogic#logic
       */
      @Override
      public String logic(FailoverLogicBean failoverLogicBean) {

        assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
        assertTrue(failoverLogicBean.isRunningInNewThread());

        if (firstConnection[0] == null) {
          firstConnection[0] = failoverLogicBean.getConnectionName();
          throw new RuntimeException("Error on first connection!");
        }
        
        return failoverLogicBean.getConnectionName();
      }
    });

    assertTrue(!StringUtils.equals(firstConnection[0], firstConnectionResult));
    
    //try again
    
    String secondConnectionResult = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
      
      /**
       * @see FailoverLogic#logic
       */
      @Override
      public String logic(FailoverLogicBean failoverLogicBean) {
        
        assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
        assertTrue(failoverLogicBean.isRunningInNewThread());

        if (secondConnection[0] == null) {
          secondConnection[0] = failoverLogicBean.getConnectionName();
          throw new RuntimeException("Error on second connection!");
        }
        
        return failoverLogicBean.getConnectionName();
      }
    });
    
    //none of the results should equal each other
    assertTrue(!StringUtils.equals(secondConnection[0], secondConnectionResult));
    assertTrue(firstConnectionResult + ", " + secondConnectionResult, !StringUtils.equals(firstConnectionResult, secondConnectionResult));

    String thirdConnectionResult = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
      
      /**
       * @see FailoverLogic#logic
       */
      @Override
      public String logic(FailoverLogicBean failoverLogicBean) {
        assertTrue(failoverLogicBean.isRunningInNewThread());
        
        if (thirdConnection[0] == null) {
          assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));

          thirdConnection[0] = failoverLogicBean.getConnectionName();
          throw new RuntimeException("Error on third connection!");
        }

        //after the first round, then all first tiers are used up, we are to the second tier now...
        assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.secondTierConnections.contains(failoverLogicBean.getConnectionName()));

        return failoverLogicBean.getConnectionName();
      }
    });

    //none of the results should equal each other
    assertTrue(secondConnectionResult + ", " + thirdConnectionResult, !StringUtils.equals(secondConnectionResult, thirdConnectionResult));
    assertTrue(thirdConnectionResult + ", " + firstConnectionResult, !StringUtils.equals(thirdConnectionResult, firstConnectionResult));
    assertTrue(thirdConnectionResult + ", " + thirdConnection[0], !StringUtils.equals(thirdConnectionResult, thirdConnection[0]));
    
    //now we have affinity
    for (int i=0;i<10;i++) {
      String currentConnectionName = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
        
        /**
         * @see FailoverLogic#logic
         */
        @Override
        public String logic(FailoverLogicBean failoverLogicBean) {
          
          assertTrue(failoverLogicBean.isRunningInNewThread());
          assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.secondTierConnections.contains(failoverLogicBean.getConnectionName()));

          return failoverLogicBean.getConnectionName();
        }
      });
      assertEquals(thirdConnectionResult, currentConnectionName);
    }
    
    //lets wait 1 minute, and see if we get back to the first tier
    System.out.println("Waiting one minute for errors to timeout...");
    
    GrouperUtil.sleep(62000);
    
    FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
      
      /**
       * @see FailoverLogic#logic
       */
      @Override
      public String logic(FailoverLogicBean failoverLogicBean) {
  
        assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
        assertTrue(failoverLogicBean.isRunningInNewThread());
  
        return failoverLogicBean.getConnectionName();
      }
    });

    
  }
  
  /**
   * 
   */
  public void testFailoverActiveStandbyLogicAffinity() {

    this.failoverConfig.setFailoverStrategy(FailoverStrategy.activeStandby);
    FailoverClient.initFailoverClient(this.failoverConfig);

    Set<String> connectionNames = new HashSet<String>();
    
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
            assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
            assertTrue(failoverLogicBean.isRunningInNewThread());
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

  /**
   * 
   */
  public void testFailoverLogicAffinityWithTimeout() {
  
    FailoverClient.initFailoverClient(this.failoverConfig);
  
    final String[] firstConnection = new String[1];
    final String[] secondConnection = new String[1];
    final String[] thirdConnection = new String[1];
  
    String firstConnectionResult = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
  
      /**
       * @see FailoverLogic#logic
       */
      @Override
      public String logic(FailoverLogicBean failoverLogicBean) {
  
        assertTrue(failoverLogicBean.getConnectionName(), 
            FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
        
        assertTrue(failoverLogicBean.isRunningInNewThread());
  
        if (firstConnection[0] == null) {
          firstConnection[0] = failoverLogicBean.getConnectionName();
          //5 seconds more than the 15 second timeout
          GrouperUtil.sleep(20000);
        }
        
        return failoverLogicBean.getConnectionName();
      }
    });
  
    assertTrue(!StringUtils.equals(firstConnection[0], firstConnectionResult));
    
    //try again
    
    String secondConnectionResult = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
      
      /**
       * @see FailoverLogic#logic
       */
      @Override
      public String logic(FailoverLogicBean failoverLogicBean) {
        
        assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
        assertTrue(failoverLogicBean.isRunningInNewThread());
  
        if (secondConnection[0] == null) {
          secondConnection[0] = failoverLogicBean.getConnectionName();
          //5 seconds more than the 15 second timeout
          GrouperUtil.sleep(20000);
        }
        
        return failoverLogicBean.getConnectionName();
      }
    });
    
    //none of the results should equal each other
    assertTrue(!StringUtils.equals(secondConnection[0], secondConnectionResult));
    assertTrue(firstConnectionResult + ", " + secondConnectionResult, !StringUtils.equals(firstConnectionResult, secondConnectionResult));
  
    String thirdConnectionResult = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
      
      /**
       * @see FailoverLogic#logic
       */
      @Override
      public String logic(FailoverLogicBean failoverLogicBean) {
        assertTrue(failoverLogicBean.isRunningInNewThread());
        
        if (thirdConnection[0] == null) {
          assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
  
          thirdConnection[0] = failoverLogicBean.getConnectionName();
          //5 seconds more than the 15 second timeout
          GrouperUtil.sleep(20000);
        }
  
        //after the first round, then all first tiers are used up, we are to the second tier now...
        assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.secondTierConnections.contains(failoverLogicBean.getConnectionName()));
  
        return failoverLogicBean.getConnectionName();
      }
    });
  
    //none of the results should equal each other
    assertTrue(secondConnectionResult + ", " + thirdConnectionResult, !StringUtils.equals(secondConnectionResult, thirdConnectionResult));
    assertTrue(thirdConnectionResult + ", " + firstConnectionResult, !StringUtils.equals(thirdConnectionResult, firstConnectionResult));
    assertTrue(thirdConnectionResult + ", " + thirdConnection[0], !StringUtils.equals(thirdConnectionResult, thirdConnection[0]));
    
    //now we have affinity
    for (int i=0;i<10;i++) {
      String currentConnectionName = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
        
        /**
         * @see FailoverLogic#logic
         */
        @Override
        public String logic(FailoverLogicBean failoverLogicBean) {
          
          assertTrue(failoverLogicBean.isRunningInNewThread());
          assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.secondTierConnections.contains(failoverLogicBean.getConnectionName()));
  
          return failoverLogicBean.getConnectionName();
        }
      });
      assertEquals(thirdConnectionResult, currentConnectionName);
    }
    
  }

  /**
   * 
   */
  public void testFailoverLogicAffinityWithTimeoutStartup() {
  
    //add 15 seconds more of startup
    this.failoverConfig.setSecondsForClassesToLoad(15);
    
    FailoverClient.initFailoverClient(this.failoverConfig);
  
    final String[] secondConnection = new String[1];
  
    String firstConnectionResult = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
  
      /**
       * @see FailoverLogic#logic
       */
      @Override
      public String logic(FailoverLogicBean failoverLogicBean) {
  
        assertTrue(failoverLogicBean.getConnectionName(), 
            FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
        
        assertTrue(failoverLogicBean.isRunningInNewThread());
  
        return failoverLogicBean.getConnectionName();
      }
    });
  
    //try again but sleep longer than the timeout, but less than the timeout plus the startup
    String secondConnectionResult = FailoverClient.failoverLogic(this.failoverConfig.getConnectionType(), new FailoverLogic<String>() {
      
      /**
       * @see FailoverLogic#logic
       */
      @Override
      public String logic(FailoverLogicBean failoverLogicBean) {
        
        assertTrue(failoverLogicBean.getConnectionName(), FailoverClientTest.this.firstTierConnections.contains(failoverLogicBean.getConnectionName()));
        assertTrue(failoverLogicBean.isRunningInNewThread());
  
        if (secondConnection[0] == null) {
          secondConnection[0] = failoverLogicBean.getConnectionName();
          //5 seconds more than the 15 second timeout
          GrouperUtil.sleep(20000);
        }
        
        return failoverLogicBean.getConnectionName();
      }
    });
    
    //none of the results should equal each other
    assertTrue(StringUtils.equals(secondConnection[0], secondConnectionResult));
    assertTrue(StringUtils.equals(firstConnectionResult, secondConnectionResult));
    
  }

}
