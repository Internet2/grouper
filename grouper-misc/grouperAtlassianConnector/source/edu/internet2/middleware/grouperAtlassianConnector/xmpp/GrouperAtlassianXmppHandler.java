/**
 * 
 */
package edu.internet2.middleware.grouperAtlassianConnector.xmpp;

import java.util.List;

import edu.internet2.middleware.grouperAtlassianConnector.GrouperAccessProvider;
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
   * 
   */
  public static void registerXmppListenerIfNeeded() {
    
    if (!GrouperClientUtils.propertiesValueBoolean("atlassian.registerXmppListeners", false, false)) {
      
      LOG.debug("No need to start XMPP listener");
      
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
          LOG.debug("XMPP listener is already started");
        
        }
      }
    } else {
      LOG.debug("XMPP listener is already started");
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
    
    LOG.error("Why is this being called????");
    
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppHandler#handleIncremental(edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppJob, java.lang.String, java.lang.String, java.util.List, java.util.List, edu.internet2.middleware.grouperClientExt.xmpp.GrouperClientXmppSubject, java.lang.String)
   */
  @Override
  public void handleIncremental(GrouperClientXmppJob grouperClientXmppJob,
      String groupName, String groupExtension,
      List<GrouperClientXmppSubject> newSubjectList,
      List<GrouperClientXmppSubject> previousSubjectList,
      GrouperClientXmppSubject changeSubject, String action) {

    if (LOG.isDebugEnabled()) {
      LOG.debug("Received XMPP message: " + grouperClientXmppJob.getEventAction() 
          + ", groups: " + GrouperClientUtils.setToString(grouperClientXmppJob.getGroupNames()) + ", subject: " 
              + (changeSubject == null ? "null" : changeSubject.getSubjectId() ) );
    }

    //clear the access cache
    new GrouperAccessProvider().flushCaches();
    
  }

}
