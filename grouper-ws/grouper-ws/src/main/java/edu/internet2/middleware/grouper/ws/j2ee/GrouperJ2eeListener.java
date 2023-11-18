/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.j2ee;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.internet2.middleware.grouper.j2ee.ServletContextUtils;


/**
 * J2ee listener for whatever
 */
public class GrouperJ2eeListener implements ServletContextListener {

  /**
   * 
   */
  public GrouperJ2eeListener() {
  }

  /**
   * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
   */
  public void contextDestroyed(ServletContextEvent arg0) {
    
    ServletContextUtils.contextDestroyed();
    
  }

  /**
   * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
   */
  public void contextInitialized(ServletContextEvent arg0) {
    //nothing yet
  }

}
