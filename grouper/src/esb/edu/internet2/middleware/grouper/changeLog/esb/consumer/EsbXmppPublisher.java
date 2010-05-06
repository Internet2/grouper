/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import org.apache.commons.logging.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * Class to send Grouper events to XMPP server, formatted as JSON strings
 *
 */

public class EsbXmppPublisher extends EsbListenerBase {

  private static final Log LOG = GrouperUtil.getLog(EsbXmppPublisher.class);

  private XMPPConnection connection;

  private Chat thisChat;

  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {

    if (LOG.isDebugEnabled()) {
      LOG.debug("Consumer " + consumerName + " publishing "
          + GrouperUtil.indent(eventJsonString, false));
    }

    String recipient = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.recipient", "");
    if (this.connection == null || !this.connection.isConnected()
        || !this.connection.isAuthenticated()) {
      this.connect(consumerName);
    }
    try {
      if (this.thisChat == null) {
        ChatManager chatmanager = connection.getChatManager();
        thisChat = chatmanager.createChat(recipient, null);
      }
      thisChat.sendMessage(eventJsonString);

      if (LOG.isDebugEnabled()) {
        LOG.debug("ESB XMMP client " + consumerName + " sent message");
      }
      return true;
    } catch (XMPPException e) {
      throw new RuntimeException(consumerName + " failed to send message", e);
    }
  }

  public void connect(String consumerName) {
    String xmppServer = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.server");
    int port = GrouperLoaderConfig.getPropertyInt("changeLog.consumer." + consumerName
        + ".publisher.port", 5222);
    String username = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.username", "");
    String password = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
        + consumerName + ".publisher.password", "");
    ConnectionConfiguration config = new ConnectionConfiguration(xmppServer, port);
    config.setReconnectionAllowed(true);
    this.connection = new XMPPConnection(config);
    // Connect to the server
    if (LOG.isDebugEnabled()) {
      LOG.debug("ESB XMMP client " + consumerName + " connecting");
    }
    try {
      connection.connect();
      // Log into the server
      connection.login(username, password, "GrouperEsbClient");
    } catch (XMPPException e) {
      throw new RuntimeException(consumerName + " failed to connect", e);
    }
  }

  public void disconnect() {
    if (this.connection != null && this.connection.isConnected())
      this.connection.disconnect();
  }
}
