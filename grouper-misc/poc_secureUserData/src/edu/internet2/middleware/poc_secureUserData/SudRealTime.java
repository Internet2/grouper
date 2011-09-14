/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.poc_secureUserData;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Message.Type;

import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.XppDriver;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;
import edu.internet2.middleware.poc_secureUserData.util.GcDbUtils;
import edu.internet2.middleware.poc_secureUserData.util.GcDbUtils.DbType;


/**
 *
 */
public class SudRealTime {

  // /**
  // *
  // * @return xstream
  // */
  // private static XStream xstream() {
  // if (xStream == null) {
  // xStream = new XStream(new XppDriver());
  //
  // //do javabean properties, not fields
  // xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()) {
  //
  // /**
  // * @see com.thoughtworks.xstream.converters.javabean.JavaBeanConverter#canConvert(java.lang.Class)
  // */
  // @SuppressWarnings("unchecked")
  // @Override
  // public boolean canConvert(Class type) {
  // //see if one of our beans
  // return type.getName().startsWith("edu.internet2");
  // }
  //
  // });
  //
  // xStream.alias("XmppMembershipChange", XmppMembershipChange.class);
  // xStream.alias("XmppSubject", XmppSubject.class);
  //
  // }
  // return xStream;
  // }
  
  /** allowed from jabber ids */
  private static Set<String> allowFromJabberIds = new HashSet<String>();

  /** keep a reference here so we dont have to keep logging in */
  private static XMPPConnection xmppConnection = null;

  /**
  * @param args
  */
  public static void main(String[] args) {
  
    String allowFroms = GrouperClientUtils.propertiesValue("grouperClient.xmpp.trustedMessagesFromJabberIds", true);
    allowFromJabberIds.addAll(GrouperClientUtils.splitTrimToList(allowFroms, ","));
  
    //note this doesnt return
    xmppLoop();
  
  }

  /**
  * connect to xmpp
  */
  private static void xmppConnect() {
    XMPPConnection theXmppConnection = xmppConnection();
    theXmppConnection.addPacketListener(new PacketListener() {
  
      // @Override
      public void processPacket(Packet packet) {
        Message message = null;
        try {
          message = (Message) packet;
          if (LOG.isDebugEnabled()) {
            LOG.debug(message == null ? null : message.toXML());
          }
          String body = message.getBody();
          
          XStream xStream = new XStream(new XppDriver());
          xStream.alias(StringUtils.uncapitalize(SudChangeLogMessage.class.getSimpleName()), SudChangeLogMessage.class);
          
          SudChangeLogMessage sudChangeLogMessage = (SudChangeLogMessage)xStream.fromXML(body);
          
          //handle it...
          if (GrouperClientUtils.equals("permissionRefresh", sudChangeLogMessage.getChangeType())) {
            SudFullSync.syncRowAndColumnPermissions();
          } else if (GrouperClientUtils.equals("rowGroupChange", sudChangeLogMessage.getChangeType())) {
            syncGroupAndUser(sudChangeLogMessage.getRowGroupExtension(), sudChangeLogMessage.getRowSubjectId());
          } else {
            throw new RuntimeException("Not expecting changeType: " + sudChangeLogMessage.getChangeType());
          }
          
          
        } catch (Throwable re) {
          String messageXml = message == null ? null : message.toXML();
          LOG.error("Problem with message: " + messageXml, re);
          throw new RuntimeException(re);
        }
  
      }
    }, new PacketFilter() {
  
      // @Override
      public boolean accept(Packet packet) {
        if (packet instanceof Message) {
          Message message = (Message) packet;
          Type type = message.getType();
          if (type == Type.chat && !GrouperClientUtils.isBlank(message.getBody())) {
            if (allowFromJabberIds.contains(message.getFrom())) {
              return true;
            }
            if (LOG.isDebugEnabled()) {
              LOG.debug("Not expecting message from: " + message.getFrom());
            }
          }
        }
        return false;
      }
    });
  }

  /**
  * get an xmpp connection
  * @return xmpp connection
  */
  private static synchronized XMPPConnection xmppConnection() {
    if (xmppConnection == null || !xmppConnection.isAuthenticated() || !xmppConnection.isConnected()) {
      String user = null;
      String pass = null;
      String resource = null;
      String server = null;
      int port = -1;
      try {
        if (xmppConnection != null) {
          try {
            xmppConnection.disconnect();
          } catch (Exception e) {
            //this is ok
          }
        }
        server = xmppServer();
        port = xmppPort();
        ConnectionConfiguration config = new ConnectionConfiguration(server, port);
  
        boolean debuggerEnabled = GrouperClientUtils.propertiesValueBoolean("grouperClient.xmpp.debuggerEnabled", false, false);
  
        config.setDebuggerEnabled(debuggerEnabled);
        config.setReconnectionAllowed(true);
  
        config.setSASLAuthenticationEnabled(true);
        SASLAuthentication.supportSASLMechanism("PLAIN");
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        xmppConnection = new XMPPConnection(config);
        xmppConnection.connect();
  
        user = xmppUser();
        pass = xmppPass();
        resource = xmppResource();
        xmppConnection.login(user, pass, resource);
      } catch (XMPPException xe) {
        throw new RuntimeException("Problem connecting: server: " + server + ", port: "
            + port + ", user: " + user + ", pass not included, "
            //+ GrouperClientUtils.repeat("*", GrouperClientUtils.defaultString(pass).length())
            + ", resource: " + resource, xe);
      }
    }
    return xmppConnection;
  }

  /**
  * note, this doesnt return
  */
  private static void xmppLoop() {
    while (true) {
      try {
        if (xmppConnection == null || !xmppConnection.isConnected() || !xmppConnection.isAuthenticated()) {
  
          //if not starting, this is a problem
          if (xmppConnection != null) {
            LOG.error("xmpp connection is not connected");
            try {
              xmppConnection.disconnect();
            } catch (Exception e) {
              LOG.error("error", e);
            }
            xmppConnection = null;
          }
          xmppConnect();
        }
      } catch (Exception e) {
        LOG.error("Problem with xmpp", e);
      }
      GrouperClientUtils.sleep(60000);
    }
  }

  /**
  * xmpp pass (decrypted if file)
  * @return the pass
  */
  private static String xmppPass() {
  
    boolean disableExternalFileLookup = GrouperClientUtils.propertiesValueBoolean(
        "encrypt.disableExternalFileLookup", false, true);
  
    //lets lookup if file
    String pass = GrouperClientUtils.propertiesValue("grouperClient.xmpp.pass", true);
    String passFromFile = GrouperClientUtils.readFromFileIfFile(pass, disableExternalFileLookup);
    
    if (GrouperClientUtils.propertiesValueBoolean("encrypt.encryptLikeServer", false, false)) {
      if (!GrouperClientUtils.equals(pass, passFromFile)) {
  
        String encryptKey = GrouperClientUtils.encryptKey();
        pass = new Crypto(encryptKey).decrypt(passFromFile);
        
      }
  
      return pass;
    }
    if (!GrouperClientUtils.equals(pass, passFromFile)) {
  
      String encryptKey = GrouperClientUtils.propertiesValue("encrypt.key", true);
      encryptKey = GrouperClientUtils.readFromFileIfFile(encryptKey, disableExternalFileLookup);
      passFromFile = new Crypto(encryptKey).decrypt(passFromFile);
  
    }
  
    return passFromFile;
  }

  /**
  * port to connect to, or 1522 as default
  * @return port
  */
  private static int xmppPort() {
    return GrouperClientUtils.propertiesValueInt("grouperClient.xmpp.server.port", 1522, false);
  }

  /**
  * xmpp resource
  * @return the resource
  */
  private static String xmppResource() {
    return GrouperClientUtils.propertiesValue("grouperClient.xmpp.resource", false);
  }

  /**
  * xpp server to connect to
  * @return xmpp server
  */
  private static String xmppServer() {
    return GrouperClientUtils.propertiesValue("grouperClient.xmpp.server.host", true);
  }

  /**
  * xmpp user
  * @return the user
  */
  private static String xmppUser() {
    return GrouperClientUtils.propertiesValue("grouperClient.xmpp.user", true);
  }

  /**
   * sync a group and user in incremental fashion
   * @param groupExtension
   * @param subjectId
   */
  public static void syncGroupAndUser(String groupExtension, String subjectId) {
    //get the memberships from Grouper
    String rowGroupsFolder = GrouperClientUtils.propertiesValue("sud.rootFolder", true) + ":rowGroups";
    
    //is there a member?
    WsHasMemberResults wsHasMemberResults = new GcHasMember().addSubjectLookup(new WsSubjectLookup(subjectId, "jdbc", null))
      .assignGroupName(rowGroupsFolder + ":" + groupExtension).execute();
    
    boolean hasGrouperMembership = StringUtils.equals("IS_MEMBER", 
        wsHasMemberResults.getResults()[0].getResultMetadata().getResultCode());
    
    //add all from DB
    Set<String> groupExtensionsFromDb = new TreeSet<String>(GcDbUtils.listSelect(String.class, 
        "select distinct group_extension from secureuserdata_memberships where personid = ? and group_extension = ?", 
        GrouperClientUtils.toList(DbType.STRING), GrouperClientUtils.toList((Object)subjectId, groupExtension)));

    boolean hasDbMembership = groupExtensionsFromDb.contains(groupExtension);
    
    if (hasGrouperMembership && !hasDbMembership) {
      
      SudMembership sudMembership = new SudMembership();
      sudMembership.setGroupExtension(groupExtension);
      sudMembership.setPersonid(subjectId);
      sudMembership.store();
      
      if (LOG.isInfoEnabled()) {
        LOG.info("Add mship for group: " 
            + groupExtension + ", personid: " + subjectId);
      }

    } else if (!hasGrouperMembership && hasDbMembership) {
      
      //delete by query in case multiple
      int rows = GcDbUtils.executeUpdate("delete from secureuserdata_memberships where group_extension = ? and personid = ?", 
          GrouperClientUtils.toList((Object)groupExtension, subjectId));
      
      if (LOG.isInfoEnabled()) {
        LOG.info("Del " + rows + " mships of group: " 
            + groupExtension + ", personid: " + subjectId);
      }

    }
  }

  /**
   * 
   */
  private static Logger LOG = Logger.getLogger(SudFullSync.class);
  
}
