/*
 * @author mchyzer $Id: RestClientSettings.java,v 1.6 2008-10-21 05:27:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.util;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.io.IOUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
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
      try {
        RegistryReset.internal_resetRegistryAndAddTestSubjects();
        String userGroupName = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME);
        GrouperSession grouperSession = GrouperSession.startRootSession();
        Group wsUserGroup = Group.saveGroup(grouperSession, null, null, userGroupName, null, null, null, true);
        Subject userSubject = SubjectFinder.findByIdentifier(USER);
        wsUserGroup.addMember(userSubject);

        String actAsGroupName = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_ACT_AS_GROUP);
        Group actAsGroup = Group.saveGroup(grouperSession, null, null, actAsGroupName, null, null, null, true);
        actAsGroup.addMember(userSubject);
        
        String wheelGroupName = GrouperConfig.getProperty(GrouperConfig.PROP_WHEEL_GROUP);
        Group wheelGroup = Group.saveGroup(grouperSession, null, null, wheelGroupName, null, null, null, true);
        wheelGroup.addMember(userSubject);
        
        //add the member
        MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.0"));
        MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.2"));
        MemberFinder.findBySubject(grouperSession, SubjectFinder.findById("test.subject.3"));
        
        Stem.saveStem(grouperSession, null, null, "aStem", null, null, null, true);

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    
}
