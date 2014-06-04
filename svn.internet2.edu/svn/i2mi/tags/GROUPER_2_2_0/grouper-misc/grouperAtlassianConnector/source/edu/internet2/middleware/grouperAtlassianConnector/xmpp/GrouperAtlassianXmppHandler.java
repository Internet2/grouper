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
package edu.internet2.middleware.grouperAtlassianConnector.xmpp;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.internet2.middleware.grouperAtlassianConnector.GrouperAccessProvider;
import edu.internet2.middleware.grouperAtlassianConnector.GrouperProfileProvider;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppMain;
import edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject;


/**
 * handle events that come in form xmpp
 * @author mchyzer
 *
 */
public class GrouperAtlassianXmppHandler implements GrouperClientXmppHandler {

  /**
   * thread for xmpp listener
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
  public static void registerXmppListenerIfNeeded() {
    
    boolean shouldDebugLog = LOG.isDebugEnabled() && shouldRegisterLog();
    
    if (!GrouperClientUtils.propertiesValueBoolean("atlassian.registerXmppListeners", false, false)) {
      
      if (shouldDebugLog) {
        LOG.debug("No need to start XMPP listener");
      }
      
      return;
    }
    
    if (!threadIsRunning) {
      synchronized(GrouperAtlassianXmppHandler.class) {
        if (!threadIsRunning) {
          
          LOG.debug("Starting XMPP listener");

          threadIsRunning = true;

          thread = new Thread(new Runnable() {

            @Override
            public void run() {
              try {
                GrouperClientXmppMain.main(new String[]{});
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
            LOG.debug("XMPP listener is already started");
          }
        }
      }
    } else {
      if (shouldDebugLog) {
        LOG.debug("XMPP listener is already started");
      }
    }
    
  }

  /**
   * logger
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GrouperAtlassianXmppHandler.class);
  
  /**
   * handle events from xmpp, right now just refresh the caches
   * @see edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler#handleAll(edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob, java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public void handleAll(GrouperClientXmppJob grouperClientXmppJob, String groupName,
      String groupExtension, List<GrouperClientXmppSubject> newSubjectList) {
    
    LOG.debug("handleAll flush cache");
    
    new GrouperAccessProvider().flushCaches();
    //kick it off
    new GrouperAccessProvider().list();

  }

  /**
   * @see edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler#handleIncremental(edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob, java.lang.String, java.lang.String, java.util.List, java.util.List, edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject, java.lang.String)
   */
  @Override
  public void handleIncremental(GrouperClientXmppJob grouperClientXmppJob,
      String groupName, String groupExtension,
      List<GrouperClientXmppSubject> newSubjectList,
      List<GrouperClientXmppSubject> previousSubjectList,
      GrouperClientXmppSubject changeSubject, String action) {

    int secondsInFuture = GrouperClientUtils.propertiesValueInt("atlassian.xmppIncrementalClearCacheSecondsInFuture", 60, true);
    
    if (secondsInFuture == 0) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Received XMPP message: " + grouperClientXmppJob.getEventAction() 
            + ", groups: " + GrouperClientUtils.setToString(grouperClientXmppJob.getGroupNames()) + ", subject: " 
                + (changeSubject == null ? "null" : changeSubject.getSubjectId() ) + ", clearing cache " + secondsInFuture + " in future" );
      }
      
      new GrouperAccessProvider().flushCaches();
      //kick it off
      new GrouperAccessProvider().list();
      
    } else {
      long flushCacheMillisSince1970 = System.currentTimeMillis() + (1000 * secondsInFuture);
      
      //give it an extra 100 millis
      if (GrouperAccessProvider.flushCaches(flushCacheMillisSince1970)) {

        if (LOG.isDebugEnabled()) {
          LOG.debug("Received XMPP message: " + grouperClientXmppJob.getEventAction() 
              + ", groups: " + GrouperClientUtils.setToString(grouperClientXmppJob.getGroupNames()) + ", subject: " 
                  + (changeSubject == null ? "null" : changeSubject.getSubjectId() ) + ", clearing cache " + secondsInFuture + " in future" );
        }
        
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
