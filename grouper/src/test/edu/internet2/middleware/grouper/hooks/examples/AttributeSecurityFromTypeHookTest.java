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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks.examples;

import junit.textui.TestRunner;

import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class AttributeSecurityFromTypeHookTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeSecurityFromTypeHookTest("testAttributeSecurityByGroup"));
  }
  
  /**
   * @param name
   */
  public AttributeSecurityFromTypeHookTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testAttributeSecurity() {
    //set some grouper properties
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.posixGroup.wheelOnly", "true");

    GroupTypeSecurityHook.resetCacheSettings();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    final GroupType groupType = GroupType.createType(grouperSession, "posixGroup", true);
    groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    groupType.addAttribute(grouperSession, "gidNumber", 
        true);
    
    Stem stem = new StemSave(grouperSession).assignName("aStem").assignCreateParentStemsIfNotExist(true).save();
    
    Subject subject = SubjectTestHelper.SUBJ0;
    
    stem.grantPriv(subject, NamingPrivilege.CREATE, true);
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(subject);
    final Group group = stem.addChildGroup("aGroup", "aGroup");
    
    try {
      group.addType(groupType);
      fail("Shouldnt get here");
    } catch (Exception e) {
      //ok
    }
    
    assertTrue(!group.getTypes().contains(groupType));
    
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        try {
          //should work here
          group.addType(groupType);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

        try {
          group.setAttribute("gidNumber", "2");
        } catch (Exception e) {
          fail("Shouldnt get here: " + ExceptionUtils.getFullStackTrace(e));
        }

        return null;
      }
    });
    
    
  }
  
  /**
   * 
   */
  public void testAttributeSecurityByGroup() {
    //set some grouper properties
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.types.testType.allowOnlyGroup", "etc:adminGroup");

    Group adminGroup = GrouperDAOFactory.getFactory().getStem().findByName("etc").addChildGroup("adminGroup", "adminGroup");
    
    GroupTypeSecurityHook.resetCacheSettings();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    final GroupType groupType = GroupType.createType(grouperSession, "testType", true);
    groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);

    groupType.addAttribute(grouperSession, "gidNumber", 
        true);
    
    groupType.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    groupType.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    Stem stem = new StemSave(grouperSession).assignName("aStem").assignCreateParentStemsIfNotExist(true).save();
    
    Subject subject = SubjectTestHelper.SUBJ0;
    
    stem.grantPriv(subject, NamingPrivilege.CREATE, true);
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(subject);
    final Group group = stem.addChildGroup("aGroup", "aGroup");
    
    try {
      group.addType(groupType);
      fail("Shouldnt get here");
    } catch (Exception e) {
      //ok
    }
    
    assertTrue(!group.getTypes().contains(groupType));
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.startRootSession();
    adminGroup.addMember(subject);
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(subject);
    
    try {
      //should work here
      group.addType(groupType);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    // test attributes now
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.startRootSession();
    adminGroup.deleteMember(subject);
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(subject);

    try {
      group.setAttribute("gidNumber", "2");
      fail("Shouldnt get here");
    } catch (Exception e) {
      // ok
    }
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.startRootSession();
    adminGroup.addMember(subject);
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(subject);

    try {
      group.setAttribute("gidNumber", "2");
      assertEquals("2", GrouperDAOFactory.getFactory().getAttributeAssignValue().findLegacyAttributesByGroupId(group.getId()).get("gidNumber").getValueString());
      
      group.setAttribute("gidNumber", "3");
      assertEquals("3", GrouperDAOFactory.getFactory().getAttributeAssignValue().findLegacyAttributesByGroupId(group.getId()).get("gidNumber").getValueString());
 
      group.deleteAttribute("gidNumber");
      assertEquals(null, GrouperDAOFactory.getFactory().getAttributeAssignValue().findLegacyAttributesByGroupId(group.getId()).get("gidNumber"));

      group.setAttribute("gidNumber", "4");
      assertEquals("4", GrouperDAOFactory.getFactory().getAttributeAssignValue().findLegacyAttributesByGroupId(group.getId()).get("gidNumber").getValueString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.startRootSession();
    adminGroup.deleteMember(subject);
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(subject);
    
    
    try {
      group.setAttribute("gidNumber", "5");
      fail("Shouldnt get here");
    } catch (Exception e) {
      // ok
      assertEquals("4", GrouperDAOFactory.getFactory().getAttributeAssignValue().findLegacyAttributesByGroupId(group.getId()).get("gidNumber").getValueString());
    }
    
    try {
      group.deleteAttribute("gidNumber");
      fail("Shouldnt get here");
    } catch (Exception e) {
      // ok
    }
  }
  
}
