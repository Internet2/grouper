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
/**
 * 
 */
package edu.internet2.middleware.grouperAtlassianConnector.aws;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import edu.internet2.middleware.grouperAtlassianConnector.GrouperAccessProvider;
import edu.internet2.middleware.grouperAtlassianConnector.GrouperProfileProvider;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * handle events that come in from aws
 * @author mchyzer
 *
 */
public class GrouperAtlassianAwsHandler {

  /**
   * thread for aws listener
   */
  private static Thread thread = null;
  
  /**
   * see if thread is running
   */
  private static boolean threadIsRunning = false;
  
  /**
   * dont log more than once per minute
   */
  private static long lastRegisterLogMessageMillis = -1;

  /**
   * dont log register more than once per minute
   * @return if should log
   */
  private static boolean shouldRegisterLog() {
    if (System.currentTimeMillis() - lastRegisterLogMessageMillis > 60 * 1000) {
      lastRegisterLogMessageMillis = System.currentTimeMillis();
      return true;
    }
    return false;
  }
  
  /**
   * 
   */
  public static void registerAwsListenerIfNeeded() {
    
    boolean shouldDebugLog = LOG.isDebugEnabled() && shouldRegisterLog();
        
    if (!threadIsRunning) {
      synchronized(GrouperAtlassianAwsHandler.class) {
        if (!threadIsRunning) {
          
          LOG.debug("Starting AWS listener");

          threadIsRunning = true;

          thread = new Thread(new Runnable() {

            @Override
            public void run() {
              try {
                //TODO list for events
                LOG.warn("Atlassian XMPP listener is exiting");
              } catch (RuntimeException re) {
                LOG.error("Atlassian XMPP listener errored out: ", re);
                throw re;
              } finally {
                threadIsRunning = false;
              }
              
            }
            
          });
          thread.setDaemon(true);
          thread.start();
        } else {
          if (shouldDebugLog) {
            LOG.debug("AWS listener is already started");
          }
        }
      }
    } else {
      if (shouldDebugLog) {
        LOG.debug("AWS listener is already started");
      }
    }
    
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperAtlassianAwsHandler.class);
  
  /**
   * handle events from aws, right now just refresh the caches and rebuild the DB
   */
  public void fullRefresh() {
    
    LOG.debug("handleAll flush cache");
    
    new GrouperAccessProvider().flushCaches();
    //kick it off
    new GrouperAccessProvider().list();

  }

  /**
   * 
   */
  public void handleIncremental() {

    int secondsInFuture = GrouperClientUtils.propertiesValueInt("atlassian.awsIncrementalClearCacheSecondsInFuture", 60, true);
    
    if (secondsInFuture == 0) {
      
      new GrouperAccessProvider().flushCaches();
      //kick it off
      new GrouperAccessProvider().list();
      
    } else {
      long flushCacheMillisSince1970 = System.currentTimeMillis() + (1000 * secondsInFuture);
      
      //give it an extra 100 millis
      if (GrouperAccessProvider.flushCaches(flushCacheMillisSince1970)) {

        //lets make a timer
        new Timer().schedule(new TimerTask() {
          
          @Override
          public void run() {
            
            boolean cacheWillBeClearedInFuture = GrouperAccessProvider.cacheWillBeClearedInFuture();
            
            boolean cacheShouldBeClearedNow = GrouperAccessProvider.cacheShouldBeClearedNow();
            
            new GrouperAccessProvider().list();
            new GrouperProfileProvider().flushCaches();
            new GrouperProfileProvider().list();
            
            if (LOG.isDebugEnabled()) {
              LOG.debug("Refreshing cache in separate thread from xmpp incremental message, " +
              		"oldCacheWillBeClearedInFuture: " + cacheWillBeClearedInFuture 
              		+ ", oldCacheShouldBeClearedNow: " + cacheShouldBeClearedNow
              		+ ", cacheWillBeClearedInFuture: " + GrouperAccessProvider.cacheWillBeClearedInFuture()
              		+ ", cacheShouldBeClearedNow: " + GrouperAccessProvider.cacheShouldBeClearedNow());
            }
          }
        }, new Date(flushCacheMillisSince1970 + 1000));
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Not refreshing cache in separate thread from xmpp incremental message since already scheduled"
              + ", cacheWillBeClearedInFuture: " + GrouperAccessProvider.cacheWillBeClearedInFuture()
              + ", cacheShouldBeClearedNow: " + GrouperAccessProvider.cacheShouldBeClearedNow());
        }
      }
    }
  }

}
