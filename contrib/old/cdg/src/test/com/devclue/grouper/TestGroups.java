/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper;

import  com.devclue.grouper.registry.*;
import  com.devclue.grouper.group.*;
import  com.devclue.grouper.stem.*;
import  edu.internet2.middleware.grouper.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test <i>com.devclue.grouper.group.*</i> classes.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroups.java,v 1.1 2006-06-23 17:30:12 blair Exp $
 */
public class TestGroups extends TestCase {

  public TestGroups(String name) {
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
    GroupAdd  ga  = new GroupAdd();
    Assert.assertNotNull("ga !null", ga);
    GroupQ    gq  = new GroupQ();
    Assert.assertNotNull("gq !null", gq);
  }
  /* GENERAL */

  /* GROUPADD */
  public void testGroupAddGroup() {
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

      GroupAdd      ga  = new GroupAdd();
      stem              = "com";
      extn              = "devclue";
      name              = stem + ":" + extn;
      Group  g   = ga.addGroup(stem, extn);
      Assert.assertNotNull("g !null", g);
      Assert.assertTrue("g name", g.getName().equals(name)                );
      Assert.assertTrue("g stem", g.getParentStem().getName().equals(stem));
      Assert.assertTrue("g extn", g.getExtension().equals(extn)           );
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
  /* GROUPADD */

  /* GROUPQ */
  public void testGroupQGetGroupsNoGroups() {
    try {
      GroupQ  gq      = new GroupQ();
      Set     groups  = gq.getGroups("nothing");
      Assert.assertTrue("groups == 0", groups.size() ==0);
    }
    catch (Exception e) {   
      Assert.fail(e.getMessage());
    }
  }

  public void testGroupQGetGroupsWithGroups() {
    try {
      StemAdd       nsa = new StemAdd();
      GroupAdd      ga  = new GroupAdd();
      Stem   ns  = nsa.addRootStem("com");
      Group  g0  = ga.addGroup("com", "devclue");
      Group  g1  = ga.addGroup("com", "example");

      GroupQ  gq      = new GroupQ();
      Set     groups  = new HashSet();
      groups = gq.getGroups("nothing");
      Assert.assertTrue("nothing == 0", groups.size() ==0);
      groups = gq.getGroups("com");
      Assert.assertTrue("com == 2", groups.size() == 2);
      groups = gq.getGroups("com:devclue");
      Assert.assertTrue("com:devclue == 1", groups.size() == 1);
      groups = gq.getGroups("devclue");
      Assert.assertTrue("devclue == 1", groups.size() == 1);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  /* GROUPQ */
 
}
 
