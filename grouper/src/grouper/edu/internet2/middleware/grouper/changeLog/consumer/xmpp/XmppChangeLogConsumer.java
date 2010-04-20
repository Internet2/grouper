/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.changeLog.consumer.xmpp;

import java.io.StringWriter;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * send configured group member changes to xmpp
 */
public class XmppChangeLogConsumer extends ChangeLogConsumerBase {

  
  /**
   * xpp server to connect to
   * @return xmpp server
   */
  private static String xmppServer() {
    return GrouperLoaderConfig.getPropertyString("xmpp.server.host", true);
  }
  
  /**
   * port to connect to, or 1522 as default 
   * @return port
   */
  private static int xmppPort() {
    return GrouperLoaderConfig.getPropertyInt("xmpp.server.port", 1522);
  }

  /**
   * xmpp user
   * @return the user
   */
  private static String xmppUser() {
    return GrouperLoaderConfig.getPropertyString("xmpp.user", true);
  }
  
  /**
   * xmpp pass (decrypted if file)
   * @return the pass
   */
  private static String xmppPass() {
    String pass = GrouperLoaderConfig.getPropertyString("xmpp.pass", true);
    return Morph.decryptIfFile(pass);
  }
  
  /** keep a reference here so we dont have to keep logging in */
  private static XMPPConnection xmppConnection = null;

  /**
   * get a chat for a jabber id.  Note this doesnt listen on the channel
   * @param jabberId
   * @param payload of the message
   */
  private static synchronized void sendMessage(String jabberId, String payload) {
    
    try {
      if (xmppConnection == null || !xmppConnection.isAuthenticated() || !xmppConnection.isConnected()) {
        if (xmppConnection != null) {
          try {
            xmppConnection.disconnect();
          } catch (Exception e) {
            //this is ok
          }
        }
        ConnectionConfiguration config = new ConnectionConfiguration(xmppServer(), xmppPort());
  
        config.setDebuggerEnabled(false);
        config.setReconnectionAllowed(true);
        
        config.setSASLAuthenticationEnabled(true);
        SASLAuthentication.supportSASLMechanism("PLAIN");
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        xmppConnection = new XMPPConnection(config);
        xmppConnection.connect();
  
        xmppConnection.login(xmppUser(), xmppPass());
        
      }
      
      
      ChatManager chatManager = xmppConnection.getChatManager();
  
      Chat chat = chatManager.createChat(jabberId, null);
  
      chat.sendMessage(payload);
    } catch (XMPPException xmppException) {
      throw new RuntimeException("Problem with XMPP to " + jabberId, xmppException);
    }

  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(XmppChangeLogConsumer.class);
  
  /**
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase#processChangeLogEntries(java.util.List, edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata)
   */
  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {
    
    long currentId = -1;

    //try catch so we can track that we made some progress
    try {
      for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
        currentId = changeLogEntry.getSequenceNumber();

        //if this is a group type add action and category
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {

          String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);
          String sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId);
          String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId);
          processChangeLogEntry(groupName, sourceId, subjectId, "ADD_MEMBER");
        }
        
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
          
          String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
          String sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId);
          String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);
          processChangeLogEntry(groupName, sourceId, subjectId, "DELETE_MEMBER");
        }
        
      }
    } catch (Exception e) {
      changeLogProcessorMetadata.registerProblem(e, "Error processing record", currentId);
      //we made it to this -1
      return currentId-1;
    }
    if (currentId == -1) {
      throw new RuntimeException("Couldnt process any records");
    }
    
    return currentId;
  }

  /**
   * based on a change log entry, send a notification if configured
   * @param groupName
   * @param sourceId 
   * @param subjectId 
   * @param action
   */
  public void processChangeLogEntry(String groupName, String sourceId, String subjectId, String action) {
    //first lets see if the group name matches:
    List<XmppJob> xmppJobs = XmppJob.retrieveXmppJobs();
    for (XmppJob xmppJob : xmppJobs) {
      boolean matches = false;
      for (String currentGroupName : GrouperUtil.nonNull(xmppJob.getGroupNames())) {
        if (StringUtils.equals(currentGroupName, groupName)) {
          matches = true;
          break;
        }
      }
      Pattern pattern = xmppJob.retrievePattern();
      if (!matches && pattern != null) {
        if (pattern.matcher(groupName).matches()) {
          matches = true;
        }
      }
      if (matches) {
        String[] subjectAttributeNames = xmppJob.getSubjectAttributeNames();
        String[] subjectAttributeValues = null;
        if (GrouperUtil.length(subjectAttributeNames) > 0) {
          Subject subject = SourceManager.getInstance().getSource(sourceId).getSubject(subjectId, false);
          if (subject != null) {
            subjectAttributeValues = new String[GrouperUtil.length(subjectAttributeNames.length)];
            for (int i=0;i<subjectAttributeNames.length;i++) {
              String attributeName = subjectAttributeNames[i];
              String value = null;
              if (StringUtils.equals("name", attributeName)) {
                value = subject.getName();
              } else if (StringUtils.equals("description", attributeName)) {
                value = subject.getDescription();
              } else {
                value = subject.getAttributeValueOrCommaSeparated(attributeName);
              }
              subjectAttributeValues[i] = value;
            }
          }
        } else {
          subjectAttributeNames = null;
        }
        XmppMembershipChange xmppMembershipChange = new XmppMembershipChange();
        xmppMembershipChange.setAction(action);
        xmppMembershipChange.setGroupName(groupName);
        XmppSubject xmppSubject = new XmppSubject();
        xmppSubject.setId(subjectId);
        xmppSubject.setSourceId(sourceId);
        xmppSubject.setAttributeValues(subjectAttributeValues);
        xmppMembershipChange.setXmppSubject(xmppSubject);
        xmppMembershipChange.setSubjectAttributeNames(subjectAttributeNames);
        
        XStream xStream = xstream();
        StringWriter stringWriter = new StringWriter();
        xStream.marshal(xmppMembershipChange, new CompactWriter(stringWriter));
        String xml = stringWriter.toString();
        for (String jabberId : xmppJob.getSendToXmppJabberIds()) {
          sendMessage(jabberId, xml);
        }
      }
    }
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    xstreamPoc();
  }
  
  /** xstream object */
  private static XStream xStream = null;
  
  /**
   * 
   * @return xstream
   */
  private static XStream xstream() {
    if (xStream == null) {
      xStream = new XStream(new XppDriver());
      
      //do javabean properties, not fields
      xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()) {

        /**
         * @see com.thoughtworks.xstream.converters.javabean.JavaBeanConverter#canConvert(java.lang.Class)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean canConvert(Class type) {
          //see if one of our beans
          return type.getName().startsWith("edu.internet2");
        }
        
      }); 
      
      xStream.alias("XmppMembershipChange", XmppMembershipChange.class);
      xStream.alias("XmppSubject", XmppSubject.class);

    }
    return xStream;
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static void xstreamPoc() {
    XmppMembershipChange xmppMembershipChange = new XmppMembershipChange();
    xmppMembershipChange.setGroupName("a:b");
    
    XmppSubject xmppSubject = new XmppSubject();
    xmppSubject.setId("id");
    xmppSubject.setAttributeValues(new String[]{"a", "b"});
    xmppMembershipChange.setXmppSubject(xmppSubject);
    
    xmppMembershipChange.setSubjectAttributeNames(new String[]{"x", "y", "z"});
    
    xmppMembershipChange.setAction("ADD_MEMBER");
    
    
    XStream theXstream = new XStream(new XppDriver());
    
    //do javabean properties, not fields
    theXstream.registerConverter(new JavaBeanConverter(theXstream.getMapper()) {

      /**
       * @see com.thoughtworks.xstream.converters.javabean.JavaBeanConverter#canConvert(java.lang.Class)
       */
      @SuppressWarnings("unchecked")
      @Override
      public boolean canConvert(Class type) {
        //see if one of our beans
        return type.getName().startsWith("edu.internet2");
      }
      
    }); 
    
    theXstream.alias("XmppMembershipChange", XmppMembershipChange.class);
    theXstream.alias("XmppSubject", XmppSubject.class);
    StringWriter stringWriter = new StringWriter();
    theXstream.marshal(xmppMembershipChange, new CompactWriter(stringWriter));
    String xml = stringWriter.toString();
    System.out.println(GrouperUtil.indent(xml, true));

  }
  
}
