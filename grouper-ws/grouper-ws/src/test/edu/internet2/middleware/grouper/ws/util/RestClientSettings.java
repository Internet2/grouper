/*
 * @author mchyzer $Id: RestClientSettings.java,v 1.14 2009-11-15 18:54:00 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.util;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.subject.Subject;


/**
 * rest client settings
 */
public class RestClientSettings {
    /** client version.  keep this updated as the version changes */
    public static final String VERSION = GrouperWsConfig.getPropertyString("ws.testing.version");

    /** user to login as */
    public static final String USER = GrouperWsConfig.getPropertyString("ws.testing.user");

    /** user to login as */
    public static final String PASS = GrouperWsConfig.getPropertyString("ws.testing.pass");

    /** port for auth settings */
    public static final int PORT = Integer.parseInt(GrouperWsConfig.getPropertyString("ws.testing.port"));
    
    /** host for auth settings */
    public static final String HOST = GrouperWsConfig.getPropertyString("ws.testing.host");
    
    /** url prefix */
    public static final String URL = GrouperWsConfig.getPropertyString("ws.testing.httpPrefix") + 
      "://" + GrouperWsConfig.getPropertyString("ws.testing.host") + ":" 
      + GrouperWsConfig.getPropertyString("ws.testing.port") + "/" 
      + GrouperWsConfig.getPropertyString("ws.testing.appName") 
      + "/servicesRest";
    
    /**
     * for testing, get the response body as a string
     * @param method
     * @return the string of response body
     */
    public static String responseBodyAsString(HttpMethodBase method) {
      InputStream inputStream = null;
      try {
        
        StringWriter writer = new StringWriter();
        inputStream = method.getResponseBodyAsStream();
        IOUtils.copy(inputStream, writer);
        return writer.toString();
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
      
    }

    /**
     * reset data
     * @param args
     */
    public static void main(String[] args) {
      resetData();
    }
    
    /**
     * reset data, and insert groups, and add root user etc 
     */
    public static void resetData() {
      resetData(RestClientSettings.USER, true);
    }
    /**
     * reset data, and insert groups, and add root user etc 
     * @param loginUserString 
     * @param loginIsWheel
     */
    public static void resetData(String loginUserString, boolean loginIsWheel) {
      try {
        RegistryReset.internal_resetRegistryAndAddTestSubjects(false);
        
        GrouperCheckConfig.checkAttributes();

        GrouperCheckConfig.checkGroups();

        String userGroupName = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME);
        GrouperSession grouperSession = GrouperSession.startRootSession();
        
        Subject userSubject = SubjectFinder.findByIdOrIdentifier(loginUserString, true);

        if (StringUtils.isBlank(userGroupName)) {
          throw new RuntimeException("Set a " + GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME + " in grouper-ws.properties");
        }
        Group wsUserGroup = Group.saveGroup(grouperSession, userGroupName, null, userGroupName, null, null, null, true);
        wsUserGroup.addMember(userSubject, false);

        String actAsGroupName = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_ACT_AS_GROUP);
        
        if (StringUtils.isBlank(actAsGroupName)) {
          throw new RuntimeException("Set a " + GrouperWsConfig.WS_ACT_AS_GROUP + " in grouper-ws.properties");
        }
        
        if (StringUtils.contains(actAsGroupName, ",") || StringUtils.contains(actAsGroupName, "::")) {
          throw new RuntimeException(GrouperWsConfig.WS_ACT_AS_GROUP + " in grouper-ws.properties cannot contain comma or two colons for tests");
        }
        
        Group actAsGroup = Group.saveGroup(grouperSession, actAsGroupName, null, actAsGroupName, null, null, null, true);
        actAsGroup.addMember(userSubject, false);
        
        String wheelGroupName = GrouperConfig.getProperty(GrouperConfig.PROP_WHEEL_GROUP);
        if (StringUtils.isBlank(wheelGroupName)) {
          throw new RuntimeException("Set a " + GrouperWsConfig.WS_ACT_AS_GROUP + " in grouper-ws.properties");
        }
        Group wheelGroup = Group.saveGroup(grouperSession, wheelGroupName, null, wheelGroupName, null, null, null, true);
        if (loginIsWheel) {
          wheelGroup.addMember(userSubject, false);
        }
        
        //add the member
        Subject subject0 = SubjectFinder.findById("test.subject.0", true);
        MemberFinder.findBySubject(grouperSession, subject0, true);
        MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.2", true), true);
        MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.3", true), true);
        
        Stem aStem = Stem.saveStem(grouperSession, "aStem", null, "aStem", null, null, null, true);

        aStem.grantPriv(subject0, NamingPrivilege.CREATE, false);
        aStem.grantPriv(userSubject, NamingPrivilege.CREATE, false);
        aStem.grantPriv(subject0, NamingPrivilege.STEM, false);
        aStem.grantPriv(userSubject, NamingPrivilege.STEM, false);

        Stem aStem0 = Stem.saveStem(grouperSession, "aStem:aStem0", null, "aStem:aStem0", null, null, null, true);

        aStem0.grantPriv(subject0, NamingPrivilege.CREATE, false);
        aStem0.grantPriv(userSubject, NamingPrivilege.CREATE, false);
        aStem0.grantPriv(subject0, NamingPrivilege.STEM, false);
        aStem0.grantPriv(userSubject, NamingPrivilege.STEM, false);
        
        Group aGroup = Group.saveGroup(grouperSession, "aStem:aGroup", null, "aStem:aGroup", null, null, null, true);
        
        //grant a priv
        aGroup.grantPriv(subject0, AccessPrivilege.ADMIN, false);
        aGroup.grantPriv(userSubject, AccessPrivilege.ADMIN, false);
        aGroup.grantPriv(subject0, AccessPrivilege.READ, false);
        aGroup.grantPriv(userSubject, AccessPrivilege.READ, false);
        aGroup.grantPriv(subject0, AccessPrivilege.VIEW, false);
        aGroup.grantPriv(userSubject, AccessPrivilege.VIEW, false);
        
        //add some types and attributes
        GroupType groupType = GroupType.createType(grouperSession, "aType", false);
        GroupType groupType2 = GroupType.createType(grouperSession, "aType2", false);
        GroupType groupType3 = GroupType.createType(grouperSession, "aType3", false);
        groupType.addAttribute(grouperSession, "attr_1", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
        groupType.addAttribute(grouperSession, "attr_2", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
        groupType2.addAttribute(grouperSession, "attr2_1", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
        groupType2.addAttribute(grouperSession, "attr2_2", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
        groupType3.addAttribute(grouperSession, "attr3_1", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
        groupType3.addAttribute(grouperSession, "attr3_2", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
        
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    
}
