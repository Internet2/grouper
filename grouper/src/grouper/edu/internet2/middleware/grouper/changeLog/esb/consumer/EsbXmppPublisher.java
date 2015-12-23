/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xmpp.XmppConnectionBean;
import org.apache.commons.logging.Log;

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

    String recipient = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
        + consumerName + ".publisher.recipient", "");

    String xmppServer = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
        + consumerName + ".publisher.server");
    int port = GrouperLoaderConfig.retrieveConfig().propertyValueInt("changeLog.consumer." + consumerName
        + ".publisher.port", -1);
    String username = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
        + consumerName + ".publisher.username", "");
    String password = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
        + consumerName + ".publisher.password", "");
    String resource = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
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
