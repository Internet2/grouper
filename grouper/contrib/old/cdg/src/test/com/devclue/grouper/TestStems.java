/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper;

import  com.devclue.grouper.registry.*;
import  com.devclue.grouper.stem.*;
import  edu.internet2.middleware.grouper.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test <i>com.devclue.grouper.stem.*</i> classes.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStems.java,v 1.1 2006-06-23 17:30:12 blair Exp $
 */
public class TestStems extends TestCase {

  public TestStems(String name) {
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
    StemAdd nsa = new StemAdd();
    Assert.assertNotNull("nsa !null", nsa);
    StemQ   nsq = new StemQ();
    Assert.assertNotNull("nsq !null", nsq);
  }
  /* GENERAL */

  /* STEMADD */
  public void testStemAddRootStem() {
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
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testStemAddRootAndNonRoot() {
    try {
      StemAdd     nsa   = new StemAdd();
      String      stem  = "";
      String      extn  = "com";
      String      name  = extn;
      Stem ns    = nsa.addRootStem(extn);
    
      stem  = extn;
      extn  = "devclue";
      name  = stem + ":" + extn;
      ns    = nsa.addStem(stem, extn);
      Assert.assertNotNull("ns !null", ns);
      Assert.assertTrue("ns name", ns.getName().equals(name)                );
      Assert.assertTrue("ns stem", ns.getParentStem().getName().equals(stem));
      Assert.assertTrue("ns extn", ns.getExtension().equals(extn)           );
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testStemAddRootInvalid() {
    try {
      StemAdd nsa   = new StemAdd();
      String  stem  = "";
      String  extn  = ":com";
      String  name  = extn;
      try {
        Stem ns = nsa.addRootStem(extn);
        Assert.fail("created invalid stem");
      } 
      catch (RuntimeException e) {
        Assert.assertTrue("could not create stem", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testStemAddRootAndNonRootInvalid() {
    try {
      StemAdd     nsa   = new StemAdd();
      String      stem  = "";
      String      extn  = "com";
      String      name  = extn;
      Stem ns    = nsa.addRootStem(extn);
    
      stem  = extn;
      extn  = ":devclue";
      name  = stem + ":" + extn;
      try {
        ns = nsa.addStem(stem, extn);
        Assert.fail("created invalid stem");
      } 
      catch (RuntimeException e) {
        Assert.assertTrue("could not create stem", true);
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  /* STEMADD */

  /* STEMQ */
  public void testStemQGetStemsNoStems() {
    try {
      StemQ nsq   = new StemQ();
      Set   stems = nsq.getStems("nothing");
      Assert.assertTrue("stems == 0", stems.size() ==0);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  public void testStemQGetStemsWithStems() {
    try {
      StemAdd     nsa = new StemAdd();
      Stem ns;
      ns = nsa.addRootStem("com");
      Assert.assertNotNull("com !null", ns);
      ns = nsa.addStem("com", "devclue");
      Assert.assertNotNull("com:devclue !null", ns);

      StemQ nsq   = new StemQ();
      Set   stems = new HashSet();
      stems = nsq.getStems("nothing");
      Assert.assertTrue("nothing == 0", stems.size() ==0);
      stems = nsq.getStems("com");
      Assert.assertTrue("com == 2", stems.size() == 2);
      stems = nsq.getStems("com:devclue");
      Assert.assertTrue("com:devclue == 1", stems.size() == 1);
      stems = nsq.getStems("devclue");
      Assert.assertTrue("devclue == 1", stems.size() == 1);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  /* STEMQ */
 
}
 
