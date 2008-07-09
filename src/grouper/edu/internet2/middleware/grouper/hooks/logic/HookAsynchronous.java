/*
 * @author mchyzer
 * $Id: HookAsynchronous.java,v 1.2 2008-07-09 05:28:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.hooks.beans.HooksBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.subject.Subject;


/**
 * class to facilitate asynchronous hooks
 */
public class HookAsynchronous {

  /** logger */
  private static final Log LOG = LogFactory.getLog(HookAsynchronous.class);
  
  /**
   * use this to make an asynchronous hook implementation
   * @param hooksContext
   * @param hooksBean
   * @param hookAsynchronousHandler 
   */
  public static void callbackAsynchronous(HooksContext hooksContext, final HooksBean hooksBean, 
      final HookAsynchronousHandler hookAsynchronousHandler) {
    
    //take the threadlocal stuff out of the context, make a new one
    final HooksContext threadHooksContext = new HooksContext(true, hooksContext);
    Thread thread = new Thread(new Runnable() {

      //run in new thread
      public void run() {
        
        try {
          hookAsynchronousHandler.callback(threadHooksContext, hooksBean);
        } catch (HookVeto hv) {
          LOG.error("Cant veto an asynchronous hook! id: " + threadHooksContext.getHookId() + ", "  + hooksBean == null ? null : hooksBean.getClass(), hv);
        } catch (Exception e) {
          LOG.error("Problem in asynchronous hook: id: " + threadHooksContext.getHookId() + ", "  + hooksBean == null ? null : hooksBean.getClass(), e);
        } finally {
          //stop session if started and still seems like the same one
          if (threadHooksContext._internal_isAsynchronousGrouperSessionStarted()) {
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            if (grouperSession != null) {
              Subject subject = grouperSession.getSubject();
              if (subject != null) {
                if (StringUtils.equals(subject.getId(), threadHooksContext._internal_getAsynchronousGrouperSessionSubject().getId())) {
                  try {
                    grouperSession.stop();
                  } catch (SessionException se) {
                    LOG.error("Cant stop session: " + subject.getId(), se);
                  }
                }
              }
            }
          }
        }
        
        
      }
      
    });
    
    thread.start();
  }
  
}
