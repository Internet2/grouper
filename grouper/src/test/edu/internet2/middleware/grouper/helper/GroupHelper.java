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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.helper;
import java.util.Date;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * {@link Group} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupHelper.java,v 1.3 2009-03-24 17:12:08 mchyzer Exp $
 */
public class GroupHelper {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = GrouperUtil.getLog(GroupHelper.class);


  // public CLASS METHODS //

  // Add a member to a group
  public static void addMember(Group g, Subject subj, String list) {
    try {
      Field f = FieldFinder.find(list, true); 
      g.addMember(subj, f);
      Assert.assertTrue("added member", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public static void addMember(g, subj, list)


  // 'Tis more ugly below

  // Add a group as a member to a group
  public static void addMember(Group g, Group gm) {
    try {
      Member m = gm.toMember();
      g.addMember(gm.toSubject());
      Assert.assertTrue("added member", true);
      MembershipTestHelper.testImm(g, gm.toSubject(), m);
      MembershipTestHelper.testEff(g, gm, m);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  } // s static void addMember(g, gm)

  // Add a member to a group
  public static void addMember(Group g, Subject subj, Member m) {
    try {
      g.addMember(subj);
      Assert.assertTrue("added member", true);
      MembershipTestHelper.testImm(g, subj, m);
    }
    catch (InsufficientPrivilegeException e0) {
      throw new RuntimeException("not privileged to add member: " + e0.getMessage(), e0);
    }
    catch (MemberAddException e1) {
      Assert.fail("failed to add member: " + e1.getMessage());
    }
  } // public static void addMember(g, subj, m)

  /**
   * TODO 20070813 deprecate
   * @since  1.2.1
   */
  public static void addMemberUpdate(Group g, Subject subj) {
    LOG.debug("addMemberUpdate.0");
    try {
      g.addMember(subj);
      Assert.assertTrue("added member", true);
      LOG.debug("addMemberUpdate.1");
    }
    catch (InsufficientPrivilegeException e0) {
      LOG.debug("addMemberUpdate.2");
      Assert.fail("not privileged to add member: " + e0.getMessage());
    }
    catch (MemberAddException e1) {
      LOG.debug("addMemberUpdate.3");
      Assert.fail("failed to add member: " + e1.getMessage());
    }
  }

  /**
   * TODO 20070813 deprecate
   * @since  1.2.1
   */
  public static void addMemberUpdateFail(GrouperSession grouperSession, 
      final Group g, final Subject subj) {
    GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          g.addMember(subj);
          Assert.fail("added member");
        }
        catch (InsufficientPrivilegeException e0) {
          Assert.assertTrue("failed to add member", true);
        }
        catch (MemberAddException e1) {
          Assert.assertTrue("failed to add member", true);
        }
        return null;
      }
      
    });
  }

  // fail to delete a group attribute
  public static void delAttrFail(Group g, String attr) {
    LOG.debug("delAttrFail.0");
    String  err = "delete attribute '" + attr + "'";
    String  msg = "did not " + err + ": ";
    try {
      g.deleteAttribute(attr);
      LOG.debug("delAttrFail.1");
      Assert.fail(err);
    }
    catch (AttributeNotFoundException eANF) {
      LOG.debug("delAttrFail.2");
      Assert.assertTrue(msg, true);
    }
    catch (GroupModifyException eGM) {
      LOG.debug("delAttrFail.3");
      Assert.assertTrue(msg, true);
    }
    catch (InsufficientPrivilegeException eIP) {
      LOG.debug("delAttrFail.4");
      Assert.assertTrue(msg, true);
    }
  } // public static void delAttrFail(g, attr)

  // Delete a group
  public static void delete(GrouperSession s, Group g, String name) {
    LOG.debug("delete.0");
    try {
      g.delete();
      LOG.debug("delete.1");
      findByNameFail(s, name);
      LOG.debug("delete.2");
    }
    catch (GroupDeleteException eGD) {
      LOG.debug("delete.3");
      Assert.fail(eGD.getMessage());
    }
    catch (InsufficientPrivilegeException eIP) {
      LOG.debug("delete.4: " + eIP.getMessage());
      Assert.fail(eIP.getMessage());
    }
  } // public static void delete(s, g, name)


  /**
   * TODO 20070813 deprecate
   * @since  1.2.1
   */
  public static void deleteFail(Group g) {
    try {
      g.delete();
      Assert.fail("deleted group");
    }
    catch (GroupDeleteException eGD) {
      Assert.assertTrue("failed to delete group: " + eGD.getMessage(), true);
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("failed to delete group: " + eIP.getMessage(), true);
    }
  } 

  // Delete a member from a group
  public static void deleteMember(Group g, Subject subj, Member m) {
    try {
      g.deleteMember(subj);
      Assert.assertTrue("deleted member", true);
      Assert.assertFalse("g !hasMember m", g.hasMember(subj));
      Assert.assertFalse("m !isMember g", m.isMember(g));
    }
    catch (InsufficientPrivilegeException e0) {
      Assert.fail("not privileged to delete member: " + e0.getMessage());
    }
    catch (MemberDeleteException e1) {
      Assert.fail("failed to delete member: " + e1.getMessage());
    }
  } // public static void deleteMember(g, subj, m)

  /**
   * TODO 20070813 deprecate
   * @since  1.2.1
   */
  public static void delMemberUpdate(Group g, Subject subj) {
    try {
      g.deleteMember(subj);
      Assert.assertTrue("deleted member", true);
    }
    catch (InsufficientPrivilegeException e0) {
      Assert.fail("not privileged to delete member: " + e0.getMessage());
    }
    catch (MemberDeleteException e1) {
      Assert.fail("failed to delete member: " + e1.getMessage());
    }
  }

  /**
   * TODO 20070813 deprecate
   * @since  1.2.1
   */
  public static void delMemberUpdateFail(GrouperSession grouperSession, 
      final Group g, final Subject subj) {
    GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          g.deleteMember(subj);
          Assert.fail("deleted member");
        }
        catch (InsufficientPrivilegeException e0) {
          Assert.assertTrue("did not delete member (IP)", true);
        }
        catch (MemberDeleteException e1) {
          Assert.assertTrue("did not delete member (MD)", true);
        }
        return null;
      }
      
    });
  }

  public static Group findByName(GrouperSession s, String name) {
    LOG.debug("findByName.0 " + name);
    try {
      Group g = GroupFinder.findByName(s, name, true);
      LOG.debug("findByName.1 " + name);
      Assert.assertNotNull("found group by name !null", g);
      LOG.debug("findByName.2 " + name);
      Assert.assertTrue("group name", g.getName().equals(name));
      LOG.debug("findByName.3 " + name);
      return g;
    }
    catch (GroupNotFoundException eGNF) {
      throw new RuntimeException(eGNF);
    }
  } // public static Group findByName(s, name)

  public static void findByNameFail(GrouperSession s, String name) {
    LOG.debug("findByNameFail.0 " + name);
    try {
      LOG.debug("findByNameFail.1 " + name);
      GroupFinder.findByName(s, name, true);
      LOG.debug("findByNameFail.2 " + name);
      Assert.fail("found group: " + name);
    }
    catch (GroupNotFoundException eGNF) {
      LOG.debug("findByNameFail.3 " + name);
      Assert.assertTrue("failed to find group: " + name, true);
    }
  } // public static void findByNameFail(s, name)

  public static Group findByUuid(GrouperSession s, String uuid) {
    LOG.debug("findByUuid.0 " + uuid);
    try {
      Group g = GroupFinder.findByUuid(s, uuid, true);
      LOG.debug("findByUuid.1 " + uuid);
      Assert.assertNotNull("found group by uuid !null", g);
      LOG.debug("findByUuid.2 " + uuid);
      Assert.assertTrue("group uuid", g.getUuid().equals(uuid));
      LOG.debug("findByUuid.3 " + uuid);
      return g;
    }
    catch (GroupNotFoundException eGNF) {
      LOG.debug("findByUuid.4");
      Assert.fail("failed to find group by uuid: " + eGNF.getMessage());
    }
    throw new RuntimeException("failed to find group by uuid");
  } // public static Gropu findByUuid(s, name)

  public static void findByUuidFail(GrouperSession s, String uuid) {
    LOG.debug("findByUuidFail.0 " + uuid);
    try {
      GroupFinder.findByUuid(s, uuid, true);
      LOG.debug("findByUuidFail.1 " + uuid);
      Assert.fail("found group: " + uuid);
      LOG.debug("findByUuidFail.2 " + uuid);
    }
    catch (GroupNotFoundException eGNF) {
      LOG.debug("findByUuidFail.3 " + uuid);
      Assert.assertTrue("failed to find group: " + uuid, true);
    }
  } // public static void findByUuidFail(s, uuid)

  public static void setAttr(Group g, String attr, String val) {
    LOG.debug("setAttr.0");
    String  msg = "set attribute '" + attr + "'='" + val + "'";
    String  err = "did not " + msg + ": ";
    try {
      g.setAttribute(attr, val);

      LOG.debug("setAttr.1");
      Assert.assertTrue(msg, true);
      LOG.debug("setAttr.2");
      Assert.assertTrue("right val", g.getAttributeValue(attr, false, true).equals(val));
      LOG.debug("setAttr.3");
    }
    catch (AttributeNotFoundException eANF) {
      LOG.debug("setAttr.4");
      Assert.fail(err + eANF.getMessage());
    }
    catch (GroupModifyException eGM) {
      LOG.debug("setAttr.5");
      Assert.fail(err + eGM.getMessage());
    }
    catch (InsufficientPrivilegeException eIP) {
      LOG.debug("setAttr.6");
      Assert.fail(err + eIP.getMessage());
    }
  } // public static void setAttr(g, attr, val)

  public static void setAttrFail(Group g, String attr, String val) {
    LOG.debug("setAttrFail.0");
    String  err = "set attribute '" + attr + "'='" + val + "'";
    String  msg = "did not " + err + ": ";
    try {
      g.setAttribute(attr, val);

      LOG.debug("setAttrFail.1");
      Assert.fail(err);
    }
    catch (AttributeNotFoundException eANF) {
      LOG.debug("setAttrFail.2");
      Assert.assertTrue(msg, true);
    }
    catch (GroupModifyException eGM) {
      LOG.debug("setAttrFail.3");
      Assert.assertTrue(msg, true);
    }
    catch (InsufficientPrivilegeException eIP) {
      LOG.debug("setAttrFail.4");
      Assert.assertTrue(msg, true);
    }
  } // public static void setAttr(g, attr, val)

  /**
   * 
   * @param exp
   * @param g
   */
  public static void testAttrs(Group exp, Group g) {
    LOG.debug("testAttrs.0");
    Map attrs = g.getAttributesMap(true);
    Assert.assertEquals( attrs.size() + " attributes (exp 0)", 0, attrs.size() );
    LOG.debug("testAttrs.2");
    try {
      Assert.assertTrue(
        "createSubject", g.getCreateSubject() instanceof Subject
      );
    LOG.debug("testAttrs.3");
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.fail("no create subject: " + eSNF.getMessage());
    }
    Assert.assertTrue(
      "createTime", g.getCreateTime() instanceof Date
    );
    LOG.debug("testAttrs.4");
    Assert.assertTrue(
      "[d] description", g.getDescription().equals(exp.getDescription())
    );
    LOG.debug("testAttrs.9");
    Assert.assertTrue(
      "[d] displayName", g.getDisplayName().equals(exp.getDisplayName())
    );
    LOG.debug("testAttrs.10");
    Assert.assertTrue(
      "[d] displayExtension", g.getDisplayExtension().equals(exp.getDisplayExtension())
    );
    LOG.debug("testAttrs.12");
    Assert.assertTrue(
      "[d] extension", g.getExtension().equals(exp.getExtension())
    );
    LOG.debug("testAttrs.14");
    try {
      g.getModifySubject();
      LOG.debug("testAttrs.15");
      Assert.assertTrue("group modified", true);
    }
    catch (SubjectNotFoundException esNF) {
      Assert.fail("group not modified");
      LOG.debug("testAttrs.16");
    }
    Assert.assertTrue(
      "modifyTime", g.getModifyTime() instanceof Date
    );
    LOG.debug("testAttrs.18");
    Assert.assertTrue(
      "[d] name", g.getName().equals(exp.getName())
    );
    LOG.debug("testAttrs.19");
  } // public static void testAttrs(exp, g)

  // test converting a Group to a Member
  public static Member toMember(Group g) {
    try {
      Member m = g.toMember();
      Assert.assertTrue("converted group to member", true);
      Assert.assertNotNull("m !null", m);
      Assert.assertTrue(
        "m subj id", m.getSubjectId().equals(g.getUuid())
      );
      Assert.assertTrue(
        "m type == group", m.getSubjectTypeId().equals("group")
      );
      Assert.assertTrue(
        "m source",
        m.getSubjectSourceId().equals(SubjectFinder.internal_getGSA().getId())
      );
      return m;
    }
    catch (Exception e) {
      T.e(e);
    }
    throw new GrouperException();
  } // public static Member toMember(g)

} // class GroupHelper

