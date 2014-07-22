/**
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
 */
package edu.internet2.middleware.grouper.xmpp;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;

/**
 * represents a connection to an XMPP server.  There is one connection per server/port/user/resource
 * @author mchyzer
 */
public class XmppConnectionBean {

  /** chat map, holds the jabberid chatting with and the chat object */
  private Map<String, Chat> chatMap = Collections.synchronizedMap(new HashMap<String, Chat>());

  /** keep a reference here so we dont have to keep logging in.  The multikey is server, port, user */
  private static Map<XmppConnectionBean, XMPPConnection> xmppConnectionMap = Collections.synchronizedMap(new HashMap<XmppConnectionBean, XMPPConnection>());

  /**
   * default constructor
   */
  public XmppConnectionBean() {
    
  }
  
  /**
   * constructor with default everything and different resource
   * @param theResource 
   */
  public XmppConnectionBean(String theResource) {
    this.resource = theResource;
  }
  
  /**
   * constructor with different stuff than the default
   * @param theServer 
   * @param thePort 
   * @param theUser 
   * @param theResource 
   * @param thePass 
   */
  public XmppConnectionBean(String theServer, int thePort, String theUser, String theResource, String thePass) {
    this.resource = theResource;
    this.port = thePort;
    this.pass = thePass;
    this.user = theUser;
    this.server = theServer;
    
  }
  
  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof XmppConnectionBean)) {
      return false;
    }
    
    XmppConnectionBean otherBean = (XmppConnectionBean)other;
    
    return new EqualsBuilder()
      .append( this.xmppPort(), otherBean.xmppPort() )
      .append( this.xmppUser(), otherBean.xmppUser() )
      .append( this.xmppResource(), otherBean.xmppResource() )
      .append( this.xmppServer(), otherBean.xmppServer() )
      .isEquals();
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.xmppPort())
      .append(this.xmppUser())
      .append(this.xmppResource())
      .append(this.xmppServer())
      .toHashCode();
  }

  /**
   * get a chat for a jabber id.  Note this doesnt listen on the channel
   * @param jabberIds
   * @param payload of the message
   */
  public synchronized void sendMessage(String jabberIds, String payload) {
    
    for (String jabberId : GrouperUtil.splitTrim(jabberIds, ",")) {
      
      try {
        
        Chat chat = this.chat(jabberId, null);
        try {
          chat.sendMessage(payload);
        } catch (XMPPException xe) {
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Problem in first pass to " + jabberId, xe);
          }
          //after first try, clear out the chat and try again
          try {
            chatMap.clear();
            this.xmppConnection().disconnect();
          } catch (Exception e) {
            //this is ok
          }
          //try again
          chat = this.chat(jabberId, null);
          chat.sendMessage(payload);
        }
      } catch (XMPPException xmppException) {
        //lets chill out for a while, there might be big problems
        GrouperUtil.sleep(30000);
        
        throw new RuntimeException("Problem with XMPP to " + jabberId, xmppException);
      }
    }  
  }

  /**
   * get a connection object
   * @return the connection object
   */
  public XMPPConnection xmppConnection() {
    synchronized(XmppConnectionBean.class) {
      
      XMPPConnection xmppConnection = xmppConnectionMap.get(this);
      
      if (xmppConnection == null || !xmppConnection.isAuthenticated() || !xmppConnection.isConnected()) {
        try {
          if (xmppConnection != null) {
            try {
              xmppConnection.disconnect();
            } catch (Exception e) {
              //this is ok
            }
          }
          ConnectionConfiguration config = new ConnectionConfiguration(this.xmppServer(), this.xmppPort());
    
          config.setDebuggerEnabled(false);
          config.setReconnectionAllowed(true);
          
          config.setSASLAuthenticationEnabled(true);
          SASLAuthentication.supportSASLMechanism("PLAIN");
          config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
          xmppConnection = new XMPPConnection(config);
          xmppConnection.connect();
    
          xmppConnection.login(this.xmppUser(), this.xmppPass(), this.xmppResource());
          
          //clear this out so we dont try to use the old connections
          this.chatMap.clear();
  
        } catch (XMPPException xe) {
          throw new RuntimeException(xe);
        }
      }
      
      xmppConnectionMap.put(this, xmppConnection);
      
      return xmppConnection;
    }
  }
  
  /**
   * xmpp pass (decrypted if file)
   * @return the pass
   */
  public String xmppPass() {
    String pass = StringUtils.isBlank(this.pass) ? GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("xmpp.pass") : this.pass;
    return Morph.decryptIfFile(pass);
  }

  /**
   * port to connect to, or 1522 as default 
   * @return port
   */
  public int xmppPort() {
    return this.port == -1 ? GrouperLoaderConfig.retrieveConfig().propertyValueInt("xmpp.server.port", 1522) : this.port;
  }

  /**
   * xmpp resource
   * @return the resource
   */
  public String xmppResource() {
    return StringUtils.isBlank(this.resource) ? GrouperLoaderConfig.retrieveConfig().propertyValueString("xmpp.resource") : this.resource;
  }

  /**
   * xpp server to connect to
   * @return xmpp server
   */
  public String xmppServer() {
    return StringUtils.isBlank(this.server) ? GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("xmpp.server.host") : this.server;
  }

  /**
   * xmpp user
   * @return the user
   */
  public String xmppUser() {
    return StringUtils.isBlank(this.user) ? GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("xmpp.user") : this.user;
  }

  /**
   * get or make a chat
   * @param jabberId to chat with
   * @param messageListener to get callbacks on messages or null to just send.  Note if there is already one registered, this
   * one wont be used
   * @return the chat
   */
  public synchronized Chat chat(String jabberId, MessageListener messageListener) {
    
    Chat chat = this.chatMap.get(jabberId);
    if (chat == null) {
      
      ChatManager chatManager = xmppConnection().getChatManager();
      chat = chatManager.createChat(jabberId, null);
      chatMap.put(jabberId, chat);
    }
    return chat;
    
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(XmppConnectionBean.class);

  /** pass or encrypted pass for xmpp */
  private String pass;

  /** port for xmpp server */
  private int port = -1;

  /** server dns name or ip address for xmpp */
  private String server;

  /** username for xmpp server */
  private String user;

  /** resource in jabber id for xmpp */
  private String resource;
  
}
