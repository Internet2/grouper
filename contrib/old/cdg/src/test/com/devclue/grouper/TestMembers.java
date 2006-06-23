/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper;

import  com.devclue.grouper.group.*;
import  com.devclue.grouper.member.*;
import  com.devclue.grouper.registry.*;
import  com.devclue.grouper.stem.*;
import  com.devclue.grouper.subject.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test <i>com.devclue.grouper.member.*</i> classes.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMembers.java,v 1.1 2006-06-23 17:30:12 blair Exp $
 */
public class TestMembers extends TestCase {

  public TestMembers(String name) {
    super(name);
  }

  protected void setUp() {
    GroupsRegistry gr = new GroupsRegistry();
    gr.reset();
  }

  protected void tearDown() {
    // Nothing
  }

  /*
   * TESTS
   */

  /* GENERAL */
  // Test instantiation
  public void testInstantiation() {
    try {
      MemberAdd ma  = new MemberAdd();
      Assert.assertNotNull("ma !null", ma);
      MemberQ   mq  = new MemberQ();
      Assert.assertNotNull("mq !null", mq);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  /* GENERAL */

  /* MEMBERADD */
  public void testMemberAddMemberInvalidPerson() {
    try {
      StemAdd     nsa   = new StemAdd();
      String      stem  = "";
      String      extn  = "com";
      String      name  = extn;
      Stem ns    = nsa.addRootStem(extn);

      GroupAdd      ga  = new GroupAdd();
      stem              = "com";
      extn              = "devclue";
      name              = stem + ":" + extn;
      Group  g   = ga.addGroup(stem, extn);

      MemberAdd     ma  = new MemberAdd();
      try {
        Member m   = ma.addMember(g.getName(), "id0", "person");
        Assert.fail("added invalid member");
      } 
      catch (SubjectNotFoundException e) {
        Assert.assertTrue("failed to add invalid member", true);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  public void testMemberAddMemberPerson() {
    try {
      StemAdd     nsa   = new StemAdd();
      String      stem  = "";
      String      extn  = "com";
      String      name  = extn;
      Stem ns    = nsa.addRootStem(extn);

      GroupAdd      ga  = new GroupAdd();
      stem              = "com";
      extn              = "devclue";
      name              = stem + ":" + extn;
      Group  g   = ga.addGroup(stem, extn);

      SubjectAdd    sa  = new SubjectAdd();
      sa.addSubject( 
        new MockSubject( "id0", "person", new MockSourceAdapter() )
      );

      MemberAdd     ma  = new MemberAdd();
      try {
        Member m   = ma.addMember(g.getName(), "id0", "person");
        Assert.assertTrue("added member", true);
        Assert.assertNotNull("m !null", m);
      } 
      catch (Exception e) {
        Assert.fail("failed to add member: " + e.getMessage());
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  public void testMemberAddMemberGroup() {
    try {
      StemAdd     nsa   = new StemAdd();
      String      stem  = "";
      String      extn  = "com";
      String      name  = extn;
      Stem ns    = nsa.addRootStem(extn);

      GroupAdd      ga  = new GroupAdd();
      stem              = "com";
      extn              = "devclue";
      name              = stem + ":" + extn;
      Group  g0  = ga.addGroup(stem, extn);

      stem              = "com";
      extn              = "example";
      name              = stem + ":" + extn;
      Group  g1  = ga.addGroup(stem, extn);

      MemberAdd     ma  = new MemberAdd();
      try {
        Member m   = ma.addMember(g0.getName(), g1.getUuid(), "group");
        Assert.assertTrue("added member", true);
        Assert.assertNotNull("m !null", m);
      }
      catch (Exception e) {
        Assert.fail("failed to add member: " + e.getMessage());
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testGroupAddGroupNoStem() {
    try {
      GroupAdd  ga    = new GroupAdd();
      String    stem  = "com";
      String    extn  = "devclue";
      String    name  = stem + ":" + extn;
      try {
        Group g  = ga.addGroup(stem, extn);
        Assert.fail("created group without stem");
      }
      catch (RuntimeException e) {
        Assert.assertTrue("failed to create group without stem", true);
      } 
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testGroupAddGroupInvalidGroup() {
    try {
      StemAdd     nsa   = new StemAdd();
      String      stem  = "";
      String      extn  = "com";
      String      name  = extn;
      Stem ns    = nsa.addRootStem(extn);
      Assert.assertNotNull("ns !null", ns);
      Assert.assertTrue("ns name", ns.getName().equals(name)                );
      Assert.assertTrue("ns stem", ns.getParentStem().getName().equals(stem));
      Assert.assertTrue("ns extn", ns.getExtension().equals(extn)           );

      GroupAdd  ga  = new GroupAdd();
      stem          = "com";
      extn          = ":devclue";
      name          = stem + ":" + extn;
      try {
        Group g = ga.addGroup(stem, extn);
        Assert.fail("created group with invalid extension");
      }
      catch (RuntimeException e) {
        Assert.assertTrue("failed to create group with invalid extension", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  // MEMBERADD //

  // MEMBERQ //
  public void testMemberQGetGroupsNoGroups() {
    try {
      SubjectAdd sa = new SubjectAdd();
      sa.addSubject( 
        new MockSubject( "id0", "person", new MockSourceAdapter() )
      );

      MemberQ mq = new MemberQ();
      try {
        Set groups  = mq.getGroups("id0", "person");
        Assert.assertTrue("groups == 0", groups.size() ==0);
      }
      catch (SubjectNotFoundException e) {
        Assert.fail("failed to find subject");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  public void testMemberQGetGroupsWithGroups() {
    try {
      SubjectAdd    sa  = new SubjectAdd();
      sa.addSubject( 
        new MockSubject( "id0", "person", new MockSourceAdapter() )
      );

      StemAdd       nsa = new StemAdd();
      GroupAdd      ga  = new GroupAdd();
      MemberAdd     ma  = new MemberAdd();
      Stem   ns  = nsa.addRootStem("com");
      Group  g0  = ga.addGroup("com", "devclue");
      Group  g1  = ga.addGroup("com", "example");
      try {
        Member m = ma.addMember(g0.getName(), "id0", "person");
        Assert.assertTrue("added member (id0)", true);
        Assert.assertNotNull("m (id0) !null", m);
      }
      catch (Exception e) {
        Assert.fail("failed to add member (g1): " + e.getMessage());
      }
      try {
        Member m = ma.addMember(g0.getName(), g1.getUuid(), "group");
        Assert.assertTrue("added member (g1)", true);
        Assert.assertNotNull("m (g1) !null", m);
      }
      catch (Exception e) {
        Assert.fail("failed to add member (g1): " + e.getMessage());
      }

      MemberQ mq      = new MemberQ();
      Set     groups  = new HashSet();
      // id0/person
      try {
        groups = mq.getGroups("id0", "person");
        Assert.assertTrue("id0 == 1", groups.size() == 1);
      }
      catch (SubjectNotFoundException e) {
        Assert.fail(e.getMessage());
      }
      // id1/person
      try {
        try {
          sa.addSubject(
            new MockSubject("id1", "id1", new MockSourceAdapter())
          );
          Assert.assertTrue("added subject", true);
        }
        catch (RuntimeException e) {
          Assert.fail("failed to add subject");
        }
        groups = mq.getGroups("id1", "person");
        Assert.assertTrue("id1 == 0", groups.size() == 0);
      }
      catch (SubjectNotFoundException e) {
        Assert.fail(e.getMessage());
      }
      // com:devclue/group
      try {
        groups = mq.getGroups("com:devclue", "group");
        Assert.assertTrue("com:devclue == 0", groups.size() == 0);
      }
      catch (SubjectNotFoundException e) {
        Assert.fail(e.getMessage());
      }
      // com:example/group
      try {
        groups = mq.getGroups("com:example", "group");
        Assert.assertTrue("com:example == 1", groups.size() == 1);
      }
      catch (SubjectNotFoundException e) {
        Assert.fail(e.getMessage());
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    // TODO ?
    // groups = mq.getGroups("com");
    // Assert.assertTrue("com == 2", groups.size() == 2);
    // groups = mq.getGroups("com:devclue");
    // Assert.assertTrue("com:devclue == 1", groups.size() == 1);
    // groups = mq.getGroups("devclue");
    // Assert.assertTrue("devclue == 1", groups.size() == 1);
  }
  // MEMBERQ //
 
}
 
