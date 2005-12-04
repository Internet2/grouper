/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package test.edu.internet2.middleware.grouper;


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * {@link Group} helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: GroupHelper.java,v 1.8 2005-12-04 22:52:49 blair Exp $
 */
public class GroupHelper {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(GroupHelper.class);


  // Protected Class Methods

  // Add a group as a member to a group
  protected static void addMember(Group g, Group gm) {
    try {
      Member m = gm.toMember();
      g.addMember(gm.toSubject());
      Assert.assertTrue("added member", true);
      MembershipHelper.testImm(g, gm.toSubject(), m);
      MembershipHelper.testEff(g, gm, m);
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("not privileged to add member: " + eIP.getMessage());
    }
    catch (MemberAddException eMA) {
      Assert.fail("failed to add member: " + eMA.getMessage());
    }
  } // protected static void addMember(g, gm)

  // Add a member to a group
  protected static void addMember(Group g, Subject subj, Member m) {
    try {
      g.addMember(subj);
      Assert.assertTrue("added member", true);
      MembershipHelper.testImm(g, subj, m);
    }
    catch (InsufficientPrivilegeException e0) {
      Assert.fail("not privileged to add member: " + e0.getMessage());
    }
    catch (MemberAddException e1) {
      Assert.fail("failed to add member: " + e1.getMessage());
    }
  } // protected static void addMember(g, subj, m)

  // Delete a group as a member from a group
  protected static void deleteMember(Group g, Group gm) {
    try {
      Member m = gm.toMember();
      g.deleteMember(gm.toSubject());
      Assert.assertTrue("deleted member", true);
      Assert.assertFalse("g !hasMember m", g.hasMember(gm.toSubject()));
      Assert.assertFalse("m !isMember g", m.isMember(g));
      // TODO Assert immediate and effective in some manner or another
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.fail("not privileged to delete member: " + eIP.getMessage());
    }
    catch (MemberDeleteException eMA) {
      Assert.fail("failed to delete member: " + eMA.getMessage());
    }
  } // protected static void deleteMember(g, gm)

  // Delete a member from a group
  protected static void deleteMember(Group g, Subject subj, Member m) {
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
  } // protected static void deleteMember(g, subj, m)

  protected static Group findByName(GrouperSession s, String name) {
    LOG.debug("findByName.0 " + name);
    try {
      Group g = GroupFinder.findByName(s, name);
      LOG.debug("findByName.1 " + name);
      Assert.assertNotNull("found group by name !null", g);
      LOG.debug("findByName.2 " + name);
      Assert.assertTrue("group name", g.getName().equals(name));
      LOG.debug("findByName.3 " + name);
      return g;
    }
    catch (GroupNotFoundException eGNF) {
      LOG.debug("findByName.4 " + name);
      Assert.fail("failed to find group by name: " + eGNF.getMessage());
    }
    throw new RuntimeException("failed to find group by name");
  } // protected static Group findByName(s, name)

  protected static void findByNameFail(GrouperSession s, String name) {
    LOG.debug("findByNameFail.0 " + name);
    try {
      LOG.debug("findByNameFail.1 " + name);
      Group g = GroupFinder.findByName(s, name);
      LOG.debug("findByNameFail.2 " + name);
      Assert.fail("found group: " + name);
    }
    catch (GroupNotFoundException eGNF) {
      LOG.debug("findByNameFail.3 " + name);
      Assert.assertTrue("failed to find group: " + name, true);
    }
  } // protected static void findByNameFail(s, name)

  protected static Group findByUuid(GrouperSession s, String uuid) {
    LOG.debug("findByUuid.0 " + uuid);
    try {
      Group g = GroupFinder.findByUuid(s, uuid);
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
  } // protected static Gropu findByUuid(s, name)

  protected static void findByUuidFail(GrouperSession s, String uuid) {
    LOG.debug("findByUuidFail.0 " + uuid);
    try {
      Group g = GroupFinder.findByUuid(s, uuid);
      LOG.debug("findByUuidFail.1 " + uuid);
      Assert.fail("found group: " + uuid);
      LOG.debug("findByUuidFail.2 " + uuid);
    }
    catch (GroupNotFoundException eGNF) {
      LOG.debug("findByUuidFail.3 " + uuid);
      Assert.assertTrue("failed to find group: " + uuid, true);
    }
  } // protected static void findByUuidFail(s, uuid)

  protected static void testAttrs(Group exp, Group g) {
    try {
      LOG.debug("testAttrs.0");
      Assert.assertTrue("4 attrs", g.getAttributes().size() == 4);
      LOG.debug("testAttrs.1");
      Assert.assertTrue(
        "createSource", g.getCreateSource().equals("")
      );
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
      try {
        String desc = g.getAttribute("description");
      LOG.debug("testAttrs.5");
        Assert.fail("found description");
/* TODO Change once I'm setting description
        Assert.assertTrue(
          "[i] description", g.getAttribute("description").equals(exp.getDescription())
        );
*/
      }
      catch (AttributeNotFoundException eTODO) {
        Assert.assertTrue("no description found", true);
      LOG.debug("testAttrs.6");
      }
      Assert.assertTrue(
        "[d] description", g.getDescription().equals(exp.getDescription())
      );
      LOG.debug("testAttrs.7");
      Assert.assertTrue(
        "[i] displayName", g.getAttribute("displayName").equals(exp.getDisplayName())
      );
      LOG.debug("testAttrs.9");
      Assert.assertTrue(
        "[d] displayName", g.getDisplayName().equals(exp.getDisplayName())
      );
      LOG.debug("testAttrs.9");
      Assert.assertTrue(
        "[i] displayExtension", g.getAttribute("displayExtension").equals(exp.getDisplayExtension())
      );
      LOG.debug("testAttrs.10");
      Assert.assertTrue(
        "[d] displayExtension", g.getDisplayExtension().equals(exp.getDisplayExtension())
      );
      LOG.debug("testAttrs.11");
      Assert.assertTrue(
        "[i] extension", g.getAttribute("extension").equals(exp.getExtension())
      );
      LOG.debug("testAttrs.12");
      Assert.assertTrue(
        "[d] extension", g.getExtension().equals(exp.getExtension())
      );
      LOG.debug("testAttrs.13j");
      Assert.assertTrue(
        "modifySource", g.getModifySource().equals("")
      );
      LOG.debug("testAttrs.14");
/* TODO Change once I'm setting description */
      try {
        Subject modder = g.getModifySubject();
      LOG.debug("testAttrs.15");
        Assert.fail("group modified");
      }
      catch (SubjectNotFoundException esNF) {
        Assert.assertTrue("group not modified", true);
      LOG.debug("testAttrs.16");
      }
      Assert.assertTrue(
        "modifyTime", g.getModifyTime() instanceof Date
      );
      LOG.debug("testAttrs.17");
      Assert.assertTrue(
        "[i] name", g.getAttribute("name").equals(exp.getName())
      );
      LOG.debug("testAttrs.18");
      Assert.assertTrue(
        "[d] name", g.getName().equals(exp.getName())
      );
      LOG.debug("testAttrs.19");
    }
    catch (AttributeNotFoundException eANF) {
      Assert.fail(eANF.getMessage());
    }
  } // protected static void testAttrs(exp, g)

  protected static void testAttrsFail(Group exp, Group g) {
    // Naming attrs
    LOG.debug("testAttrsFail.0");
    Assert.assertTrue("4 attrs", g.getAttributes().size() == 4);
    LOG.debug("testAttrsFail.1");
    Assert.assertTrue(
      "createSource", g.getCreateSource().equals("")
    );
    LOG.debug("testAttrsFail.2");
    try {
      Assert.assertTrue(
        "createSubject", g.getCreateSubject() instanceof Subject
      );
    LOG.debug("testAttrsFail.3");
    }
    catch (SubjectNotFoundException eSNF) {
    LOG.debug("testAttrsFail.4");
      Assert.fail("create subject: " + eSNF.getMessage());
    }
    Assert.assertTrue(
      "createTime", g.getCreateTime() instanceof Date
    );
    LOG.debug("testAttrsFail.5");
    try {
      g.getAttribute("description");
    LOG.debug("testAttrsFail.6");
      Assert.fail("found description");
    }
    catch (AttributeNotFoundException eANF) {
      Assert.assertTrue("no description", true);
    LOG.debug("testAttrsFail.7");
    }
    try {
      Assert.assertTrue(
        "[i] displayName", g.getAttribute("displayName").equals(exp.getDisplayName())
      );
    LOG.debug("testAttrsFail.8");
      Assert.assertTrue(
        "[i] displayExtension", g.getAttribute("displayExtension").equals(exp.getDisplayExtension())
      );
    LOG.debug("testAttrsFail.9");
      Assert.assertTrue(
        "[i] extension", g.getAttribute("extension").equals(exp.getExtension())
      );
    LOG.debug("testAttrsFail.10");
      Assert.assertTrue(
        "[i] name", g.getAttribute("name").equals(exp.getName())
      );
    LOG.debug("testAttrsFail.11");
    }
    catch (AttributeNotFoundException eANF) {
    LOG.debug("testAttrsFail.12");
      Assert.fail(eANF.getMessage()); 
    }
    Assert.assertTrue(
      "[d] description", g.getDescription().equals("")
    );
    LOG.debug("testAttrsFail.13");
    Assert.assertTrue(
      "[d] displayName", g.getDisplayName().equals(exp.getDisplayName())
    );
    LOG.debug("testAttrsFail.14");
    Assert.assertTrue(
      "[d] displayExtension", g.getDisplayExtension().equals(exp.getDisplayExtension())
    );
    LOG.debug("testAttrsFail.15");
    Assert.assertTrue(
      "[d] extension", g.getExtension().equals(exp.getExtension())
    );
    LOG.debug("testAttrsFail.16");
    Assert.assertTrue(
      "modifySource", g.getModifySource().equals("")
    );
    LOG.debug("testAttrsFail.17");
// TODO Change once I'm setting description 
    try {
      Subject modder = g.getModifySubject();
    LOG.debug("testAttrsFail.18");
      Assert.fail("group modified");
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.assertTrue("group not modified", true);
    LOG.debug("testAttrsFail.19");
    }
    Assert.assertTrue(
      "modifyTime", g.getModifyTime() instanceof Date
    );
    LOG.debug("testAttrsFail.20");
    Assert.assertTrue(
      "[d] name", g.getName().equals(exp.getName())
    );
    LOG.debug("testAttrsFail.21");
  } // protected static void testAttrsFail(exp, g)

  // test converting a Group to a Member
  protected static Member toMember(Group g) {
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
        "m source", m.getSubjectSourceId().equals("grouper group adapter")
      );
      return m;
    }
    catch (RuntimeException e) {
      Assert.fail("failed to convert group to member");
    }
    throw new RuntimeException(Helper.ERROR); 
  } // protected static Member toMember(g)

}

