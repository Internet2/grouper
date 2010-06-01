/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xmpp.XmppConnectionBean;

/**
 * 
 * Class to send Grouper events to XMPP server, formatted as JSON strings
 *
 */
public class EsbXmppPublisher extends EsbListenerBase {

  /** */
  private static final Log LOG = GrouperUtil.getLog(EsbXmppPublisher.class);

  /**
   * @see EsbListenerBase#dispatchEvent(String, String)
   */
  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {

    if (LOG.isDebugEnabled()) {
      LOG.debug("Consumer " + consumerName + " publishing "
          + GrouperUtil.indent(eventJsonString, false));
    }

    String recipient = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.recipient", "");

    String xmppServer = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.server");
    int port = GrouperLoaderConfig.getPropertyInt("changeLog.consumer." + consumerName
        + ".publisher.port", -1);
    String username = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.username", "");
    String password = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.password", "");
    String resource = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.resource", "");
    
    XmppConnectionBean xmppConnectionBean = new XmppConnectionBean(xmppServer, port, username, resource, password);

    xmppConnectionBean.sendMessage(recipient, eventJsonString);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("ESB XMMP client " + consumerName + " sent message");
    }
    return true;
  }

  /**
   * 
   */
  @Override
  public void disconnect() {
    //do nothing, keep xmpp connections open a little longer
  }
}
