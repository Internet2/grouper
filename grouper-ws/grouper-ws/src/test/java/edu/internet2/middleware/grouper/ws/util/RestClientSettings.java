/*******************************************************************************
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
 ******************************************************************************/
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
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefScopeType;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
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

        GrouperSession grouperSession = GrouperSession.startRootSession();

        //make sure to create with the same id
        
        String groupTypeId = null;
        String groupType2Id = null;
        String groupType3Id = null;
        String groupTypeAttribute1Id = null;
        String groupTypeAttribute2Id = null;
        String groupType2Attribute1Id = null;
        String groupType2Attribute2Id = null;
        String groupType3Attribute1Id = null;
        String groupType3Attribute2Id = null;
        String groupTypeAssignId = null;
        String groupType2AssignId = null;
        String groupType3AssignId = null;

        {
          String attributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");
          String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");

          GroupType groupType = GroupTypeFinder.find("aType", false);
          if (groupType != null) {
            groupTypeId = groupType.getUuid();
            
            AttributeDefName attribute = AttributeDefNameFinder.findById(groupTypeId, false);
            AttributeAssignAction attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction().findByUuidOrKey(null, attribute.getAttributeDefId(), "assign", false);
            groupTypeAssignId = attributeAssignAction == null ? null : attributeAssignAction.getId();
            
            // see if the attribute already exists.
            attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr_1", false);
            if (attribute != null) {
              groupTypeAttribute1Id = attribute.getId();
            }

            attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr_2", false);
            if (attribute != null) {
              groupTypeAttribute2Id = attribute.getId();
            }

          }
          
          groupType = GroupTypeFinder.find("aType2", false);
          if (groupType != null) {
            groupType2Id = groupType.getUuid();
            
            AttributeDefName attribute = AttributeDefNameFinder.findById(groupTypeId, false);
            AttributeAssignAction attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction().findByUuidOrKey(null, attribute.getAttributeDefId(), "assign", false);
            groupType2AssignId = attributeAssignAction == null ? null : attributeAssignAction.getId();

            // see if the attribute already exists.
            attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr2_1", false);
            if (attribute != null) {
              groupType2Attribute1Id = attribute.getId();
            }

            attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr2_2", false);
            if (attribute != null) {
              groupType2Attribute2Id = attribute.getId();
            }

          }
          
          groupType = GroupTypeFinder.find("aType3", false);
          if (groupType != null) {
            groupType3Id = groupType.getUuid();
            
            AttributeDefName attribute = AttributeDefNameFinder.findById(groupTypeId, false);
            AttributeAssignAction attributeAssignAction = GrouperDAOFactory.getFactory().getAttributeAssignAction().findByUuidOrKey(null, attribute.getAttributeDefId(), "assign", false);
            groupType3AssignId = attributeAssignAction == null ? null : attributeAssignAction.getId();

            // see if the attribute already exists.
            attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr3_1", false);
            if (attribute != null) {
              groupType3Attribute1Id = attribute.getId();
            }

            attribute = AttributeDefNameFinder.findByName(stemName + ":" + attributePrefix + "attr3_2", false);
            if (attribute != null) {
              groupType3Attribute2Id = attribute.getId();
            }

          }
          
        }
                
        
        RegistryReset.internal_resetRegistryAndAddTestSubjects(false);
        
        GrouperCheckConfig.checkObjects();

        String userGroupName = GrouperWsConfig.getPropertyString(GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME);
        
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
        GroupType groupType = GroupType.createType(grouperSession, "aType", false, groupTypeId);
        GroupType groupType2 = GroupType.createType(grouperSession, "aType2", false, groupType2Id);
        GroupType groupType3 = GroupType.createType(grouperSession, "aType3", false, groupType3Id);
        groupType.addAttribute(grouperSession, "attr_1", false, groupTypeAttribute1Id);
        groupType.addAttribute(grouperSession, "attr_2", false, groupTypeAttribute2Id);
        groupType2.addAttribute(grouperSession, "attr2_1", false, groupType2Attribute1Id);
        groupType2.addAttribute(grouperSession, "attr2_2", false, groupType2Attribute2Id);
        groupType3.addAttribute(grouperSession, "attr3_1", false, groupType3Attribute1Id);
        groupType3.addAttribute(grouperSession, "attr3_2", false, groupType3Attribute2Id);
        
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    
}
