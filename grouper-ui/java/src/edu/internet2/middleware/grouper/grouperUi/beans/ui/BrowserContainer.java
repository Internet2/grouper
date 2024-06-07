/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 * This is the UI container for the programmatic browser
 * @author mchyzer
 *
 */
public class BrowserContainer {

  /** logger */
  protected static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(BrowserContainer.class);

  /**
   * The api version in maven
   * @return the version
   */
  public String getGrouperApiVersion() {
    return GrouperVersion.grouperVersion();
  }
  /**
   * Container build in Dockerhub. Should be the same as Api version but sometimes different.
   * @return
   */
  public String getGrouperContainerVersion() {
    return System.getenv("GROUPER_CONTAINER_VERSION");
  }
}
