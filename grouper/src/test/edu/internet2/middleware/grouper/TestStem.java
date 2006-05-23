/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

package edu.internet2.middleware.grouper;


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStem.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 */
public class TestStem extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestStem.class);


  public TestStem(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testRoot() {
    LOG.info("testRoot");
    Stem  rootA = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  rootB = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Assert.assertEquals("root == root", rootA, rootB);
  } // public void testRoot()

  public void testRootAsNonRoot() {
    LOG.info("testRootAsNonRoot");
    Stem  rootA = StemHelper.findRootStem(
      SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID)
    );
    Stem  rootB = StemHelper.findRootStem(
      SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID)
    );
    Assert.assertEquals("root == root", rootA, rootB);
  } // public void testRootAsNonRoot()

  public void testGetParentStemAtRoot() {
    LOG.info("testGetParentStemAtRoot");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    try {
      Stem parent = root.getParentStem();
      Assert.fail("root stem has parent");
    }
    catch (StemNotFoundException eSNF) {
      Assert.assertTrue("root stem has no parent", true);
    }
  } // public void testGetParentStemAtRoot()

  public void testGetParentStem() {
    LOG.info("testGetParentStem");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    try {
      Stem parent = edu.getParentStem();
      Assert.assertTrue("stem has parent", true);
      Assert.assertTrue("parent == root", parent.equals(root));
      Assert.assertTrue(
        "root has STEM on parent", parent.hasStem(s.getSubject())
      );
    }
    catch (StemNotFoundException eSNF) {
      Assert.fail("stem has no parent: " + eSNF.getMessage());
    }
  } // public void testGetParentStem()

  public void testGetChildStems() {
    LOG.info("testGetChildStems");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Stem            i2    = StemHelper.addChildStem(edu, "i2", "internet2");
    Stem            uofc  = StemHelper.addChildStem(edu, "uofc", "uchicago");
    Stem            net   = StemHelper.addChildStem(root, "net", "network");
    Stem            org   = StemHelper.addChildStem(root, "org", "organization");
    Set children = root.getChildStems();
    Assert.assertTrue("4 child stems", children.size() == 4);
    Iterator iter = children.iterator();
    while (iter.hasNext()) {
      Stem child = (Stem) iter.next();
      try {
        Stem parent = child.getParentStem();
        Assert.assertTrue("child stem has parent", true);
        Assert.assertTrue("parent == root", parent.equals(root));
        Assert.assertTrue(
          "root has STEM on parent", parent.hasStem(s.getSubject())
        );
      }
      catch (StemNotFoundException eSNF) {
        Assert.fail("child stem has no parent: " + eSNF.getMessage());
      }
    }
  } // public void testGetChildStems()

  public void testGetChildGroups() {
    LOG.info("testGetChildGroups");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Stem            uofc  = StemHelper.addChildStem(edu, "uofc", "uchicago");
    Group           bsd   = StemHelper.addChildGroup(uofc, "bsd", "bsd");
    Group           gsb   = StemHelper.addChildGroup(uofc, "gsb", "gsb");
    Group           hum   = StemHelper.addChildGroup(uofc, "hum", "hum");
    Group           law   = StemHelper.addChildGroup(uofc, "law", "law");
    Group           psd   = StemHelper.addChildGroup(uofc, "psd", "psd");
    Group           ssd   = StemHelper.addChildGroup(uofc, "ssd", "ssd");
    Set children = uofc.getChildGroups();
    Assert.assertTrue("6 child groups", children.size() == 6);
    Iterator iter = children.iterator();
    while (iter.hasNext()) {
      Group child = (Group) iter.next();
      Stem parent = child.getParentStem();
      Assert.assertNotNull("child group has parent", parent);
      Assert.assertTrue("parent == uofc", parent.equals(uofc));
      Assert.assertTrue(
        "root has STEM on parent", parent.hasStem(s.getSubject())
      );
    }
  } // public void testGetChildGroups()

  // testGetCreateAttrs             => TestStem6
  // testGetModifyAttrsNotModified  => TestStem7
  // testGetModifyAttrsModified     => TestStem8

  public void testPropagateDisplayExtensionChangeRootAsRoot() {
    LOG.info("testPropagateExtensionChangeRootAsRoot");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Stem            i2    = StemHelper.addChildStem(edu, "i2", "internet2");
    Stem            uofc  = StemHelper.addChildStem(edu, "uofc", "uchicago");
    Group           bsd   = StemHelper.addChildGroup(uofc, "bsd", "biological sciences division");
    Group           psd   = StemHelper.addChildGroup(uofc, "psd", "physical sciences division");

    String exp = "";
    Assert.assertTrue(
      "root displayExtn exp(" + exp + ") got(" + root.getDisplayExtension() + ")", 
      root.getDisplayExtension().equals(exp)
    );
    Assert.assertTrue(
      "root displayName exp(" + exp + ") got(" + root.getDisplayName() + ")", 
      root.getDisplayName().equals(exp));
    exp = "education";
    Assert.assertTrue(
      "edu displayExtn exp(" + exp + ") got(" + edu.getDisplayExtension() + ")",
      edu.getDisplayExtension().equals(exp)
    );
    Assert.assertTrue(
      "edu displayName exp(" + exp + ") got(" + edu.getDisplayName() + ")",
      edu.getDisplayName().equals(exp)
    );
    exp = edu.getDisplayName() + ":internet2";
    Assert.assertTrue("i2 displayName", i2.getDisplayName().equals(exp));
    exp = edu.getDisplayName() + ":uchicago";
    Assert.assertTrue("uofc displayName", uofc.getDisplayName().equals(exp));
    exp = uofc.getDisplayName() + ":biological sciences division";
    Assert.assertTrue("bsd displayName" , bsd.getDisplayName().equals(exp));
    exp = uofc.getDisplayName() + ":physical sciences division";
    Assert.assertTrue("psd displayName" , psd.getDisplayName().equals(exp));

    // Now rename
    exp = "root stem";
    try {
      root.setDisplayExtension(exp);
      Assert.assertTrue(
        "mod'd root displayExtension=(" + root.getDisplayExtension() + ") (" + exp + ")", 
        root.getDisplayExtension().equals(exp)
      );
      Assert.assertTrue(
        "mod'd root displayName (" + root.getDisplayName() + ") (" + exp + ")", 
        root.getDisplayName().equals(exp)
      );
    }
    catch (Exception e) {
      Assert.fail("unable to change stem displayName: " + e.getMessage());
    }
    
    // Now retrieve the children and check them 
    Stem eduR = StemHelper.findByName(s, edu.getName());
    exp = root.getDisplayName() + ":education";
    Assert.assertTrue(
      "mod'd edu displayName=(" + eduR.getDisplayName() + ") (" + exp + ")", 
      eduR.getDisplayName().equals(exp)
    );

    Stem i2R = StemHelper.findByName(s, i2.getName());
    exp = eduR.getDisplayName() + ":internet2";
    Assert.assertTrue(
      "mod'd i2 displayName=(" + i2R.getDisplayName() + ") (" + exp + ")", 
      i2R.getDisplayName().equals(exp)
    );

    exp = eduR.getDisplayName() + ":uchicago";
    Stem  uofcR = StemHelper.findByName(s, uofc.getName());
    Assert.assertTrue(
      "mod'd uofc displayName=(" + uofcR.getDisplayName() + ") (" + exp + ")", 
      uofcR.getDisplayName().equals(exp)
    );
    exp = uofcR.getDisplayName() + ":biological sciences division";
    Group bsdR  = GroupHelper.findByName(s, bsd.getName());
    Assert.assertTrue(
      "mod'd bsd edu displayName=(" + bsdR.getDisplayName() + ") (" + exp + ")", 
      bsdR.getDisplayName().equals(exp)
    );
    exp = uofcR.getDisplayName() + ":physical sciences division";
    Group psdR  = GroupHelper.findByName(s, psd.getName());
    Assert.assertTrue(
      "mod'd psd edu displayName=(" + psdR.getDisplayName() + ") (" + exp + ")", 
      psdR.getDisplayName().equals(exp)
    );

    // Now reset root's displayExtension
    try {
      exp = "";
      root.setDisplayExtension(exp);
      Assert.assertTrue(
        "re-mod'd root displayExtension=(" + root.getDisplayExtension() + ") (" + exp + ")", 
        root.getDisplayExtension().equals(exp)
      );
      Assert.assertTrue(
        "re-mod'd root displayName (" + root.getDisplayName() + ") (" + exp + ")", 
        root.getDisplayName().equals(exp)
      );
    }
    catch (Exception e) {
      Assert.fail("unable to change stem displayName: " + e.getMessage());
    }
    
  } // public void testPropagateExtensionChangeRootAsRoot()

  public void testPropagateDisplayExtensionChangeAsRoot() {
    LOG.info("testPropagateExtensionChangeAsRoot");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Stem            i2    = StemHelper.addChildStem(edu, "i2", "internet2");
    Stem            uofc  = StemHelper.addChildStem(edu, "uofc", "uchicago");
    Group           bsd   = StemHelper.addChildGroup(uofc, "bsd", "biological sciences division");
    Group           psd   = StemHelper.addChildGroup(uofc, "psd", "physical sciences division");

    String exp = "education";
    Assert.assertTrue("edu displayExtn" , edu.getDisplayExtension().equals(exp));
    Assert.assertTrue("edu displayName" , edu.getDisplayName().equals(exp));
    exp = edu.getDisplayName() + ":internet2";
    Assert.assertTrue("i2 displayName", i2.getDisplayName().equals(exp));
    exp = edu.getDisplayName() + ":uchicago";
    Assert.assertTrue("uofc displayName", uofc.getDisplayName().equals(exp));
    exp = uofc.getDisplayName() + ":biological sciences division";
    Assert.assertTrue("bsd displayName" , bsd.getDisplayName().equals(exp));
    exp = uofc.getDisplayName() + ":physical sciences division";
    Assert.assertTrue("psd displayName" , psd.getDisplayName().equals(exp));
   
    // Now rename
    exp = "higher ed";
    try {
      edu.setDisplayExtension(exp);
      Assert.assertTrue(
        "mod'd edu displayExtension (" + edu.getDisplayExtension() + ")", 
        edu.getDisplayExtension().equals(exp)
      );
      Assert.assertTrue(
        "mod'd edu displayName (" + edu.getDisplayName() + ")", 
        edu.getDisplayName().equals(exp)
      );
    }
    catch (Exception e) {
      Assert.fail("unable to change stem displayName: " + e.getMessage());
    }
    
    // Now retrieve the children and check them 
    Stem i2R = StemHelper.findByName(s, i2.getName());
    exp = edu.getDisplayName() + ":internet2";
    Assert.assertTrue(
      "mod'd i2 displayName=(" + i2R.getDisplayName() + ") (" + exp + ")", 
      i2R.getDisplayName().equals(exp)
    );

    exp = edu.getDisplayName() + ":uchicago";
    Stem  uofcR = StemHelper.findByName(s, uofc.getName());
    Assert.assertTrue(
      "mod'd uofc displayName=(" + uofcR.getDisplayName() + ") (" + exp + ")", 
      uofcR.getDisplayName().equals(exp)
    );
    exp = uofcR.getDisplayName() + ":biological sciences division";
    Group bsdR  = GroupHelper.findByName(s, bsd.getName());
    Assert.assertTrue(
      "mod'd bsd edu displayName=(" + bsdR.getDisplayName() + ") (" + exp + ")", 
      bsdR.getDisplayName().equals(exp)
    );
    exp = uofcR.getDisplayName() + ":physical sciences division";
    Group psdR  = GroupHelper.findByName(s, psd.getName());
    Assert.assertTrue(
      "mod'd psd edu displayName=(" + psdR.getDisplayName() + ") (" + exp + ")", 
      psdR.getDisplayName().equals(exp)
    );

  } // public void testPropagateExtensionChangeAsRoot()

  public void testPropagateDisplayExtensionChangeAsNonRoot() {
    LOG.info("testPropagateExtensionChangeAsNonRoot");
    // Create stems + groups as root
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Stem            i2    = StemHelper.addChildStem(edu, "i2", "internet2");
    Stem            uofc  = StemHelper.addChildStem(edu, "uofc", "uchicago");
    Group           bsd   = StemHelper.addChildGroup(uofc, "bsd", "biological sciences division");
    Group           psd   = StemHelper.addChildGroup(uofc, "psd", "physical sciences division");
    // Grant subj0 STEM on edu
    PrivHelper.grantPriv(s, edu, SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
    // And revoke VIEW + READ from ALL on one of the child groups
    PrivHelper.revokePriv(s, psd, SubjectTestHelper.SUBJA, AccessPrivilege.VIEW);
    PrivHelper.revokePriv(s, psd, SubjectTestHelper.SUBJA, AccessPrivilege.READ);
   
    // Now rename as subj0
    GrouperSession  nrs   = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    Stem            eduNR = StemHelper.findByName(nrs, edu.getName());
    
    // Now rename
    String exp = "higher ed";
    try {
      eduNR.setDisplayExtension(exp);
      Assert.assertTrue(
        "mod'd edu displayExtension (" + eduNR.getDisplayExtension() + ")", 
        eduNR.getDisplayExtension().equals(exp)
      );
      Assert.assertTrue(
        "mod'd edu displayName (" + edu.getDisplayName() + ")", 
        eduNR.getDisplayName().equals(exp)
      );
    }
    catch (Exception e) {
      Assert.fail("unable to change stem displayName: " + e.getMessage());
    }
    
    // Now retrieve the children and check them 
    Stem i2R = StemHelper.findByName(nrs, i2.getName());
    exp = eduNR.getDisplayName() + ":internet2";
    Assert.assertTrue(
      "mod'd i2 displayName=(" + i2R.getDisplayName() + ") (" + exp + ")", 
      i2R.getDisplayName().equals(exp)
    );

    exp = eduNR.getDisplayName() + ":uchicago";
    Stem  uofcR = StemHelper.findByName(nrs, uofc.getName());
    Assert.assertTrue(
      "mod'd uofc displayName=(" + uofcR.getDisplayName() + ") (" + exp + ")", 
      uofcR.getDisplayName().equals(exp)
    );
    exp = uofcR.getDisplayName() + ":biological sciences division";
    Group bsdR  = GroupHelper.findByName(nrs, bsd.getName());
    Assert.assertTrue(
      "mod'd bsd edu displayName=(" + bsdR.getDisplayName() + ") (" + exp + ")", 
      bsdR.getDisplayName().equals(exp)
    );

    // Check this one as root as subj0 doesn't have READ or VIEW
    exp = uofcR.getDisplayName() + ":physical sciences division";
    Group psdR  = GroupHelper.findByName(s, psd.getName());
    Assert.assertTrue(
      "mod'd psd edu displayName=(" + psdR.getDisplayName() + ") (" + exp + ")", 
      psdR.getDisplayName().equals(exp)
    );
  } // public void testPropagateExtensionChangeAsNonRoot()

  public void testChildStemsAndGroupsLazyInitialization() {
    LOG.info("testChildStemsAndGroupsLazyInitialization");
    try {
      String          edu   = "edu";
      String          uofc  = "uofc";
      String          bsd   = "bsd";

      Subject         subj  = SubjectFinder.findById("GrouperSystem");
      GrouperSession  s     = GrouperSession.start(subj);
      Stem            root  = StemFinder.findRootStem(s);
      Stem            ns0   = root.addChildStem(edu, edu);
      Stem            ns1   = ns0.addChildStem(uofc, uofc);
      Group           g0    = ns1.addChildGroup(bsd, bsd);
      s.stop();

      s = GrouperSession.start(subj);
      Stem  a         = StemFinder.findByName(s, edu);
      Set   children  = a.getChildStems();
      Assert.assertTrue("has child stems", children.size() > 0);
      Iterator iter = children.iterator();
      while (iter.hasNext()) {
        Stem child  = (Stem) iter.next();
        Set  stems  = child.getChildStems();
        Assert.assertTrue("child has no child stems", stems.size() == 0);
        Set  groups = child.getChildGroups();
        Assert.assertTrue("child has child groups", groups.size() == 1);
        Iterator gIter = groups.iterator();
        while (gIter.hasNext()) {
          Group g = (Group) gIter.next();
          Assert.assertNotNull("group name", g.getName());
        }
      }
      s.stop();

      Assert.assertTrue("no exceptions", true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testChildStemsAndGroupsLazyInitialization() 

  public void testParentChildStemsAndGroupsLazyInitialization() {
    LOG.info("testParentChildStemsAndGroupsLazyInitialization");
    try {
      String          edu   = "edu";
      String          uofc  = "uofc";
      String          bsd   = "bsd";

      Subject         subj  = SubjectFinder.findById("GrouperSystem");
      GrouperSession  s     = GrouperSession.start(subj);
      Stem            root  = StemFinder.findRootStem(s);
      Stem            ns0   = root.addChildStem(edu, edu);
      Stem            ns1   = ns0.addChildStem(uofc, uofc);
      Group           g0    = ns1.addChildGroup(bsd, bsd);
      s.stop();

      s = GrouperSession.start(subj);
      Stem  a         = StemFinder.findByName(s, edu);
      Stem  parent    = a.getParentStem();
      Set   children  = parent.getChildStems();
      Assert.assertTrue("parent has child stems", children.size() > 0);
      Iterator iter = children.iterator();
      while (iter.hasNext()) {
        Stem child  = (Stem) iter.next();
        Set  stems  = child.getChildStems();
        Assert.assertTrue(
          "child of parent has child stems", stems.size() == 1
        );
        Iterator childIter = stems.iterator();
        while (childIter.hasNext()) {
          Stem c = (Stem) childIter.next();
          Assert.assertTrue(
            "child of child of parent has no child stems",
            c.getChildStems().size() == 0
          );
          Assert.assertTrue(
            "child of child of parent has child groups",
            c.getChildGroups().size() == 1
          );
          Iterator gIter = c.getChildGroups().iterator();
          while (gIter.hasNext()) {
            Group g = (Group) gIter.next();
            Assert.assertNotNull("group name", g.getName());
          }
        }
        Set  groups = child.getChildGroups();
        Assert.assertTrue(
          "child of parent has no child groups", groups.size() == 0
        );
      }
      s.stop();

      Assert.assertTrue("no exceptions", true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testChildStemsAndGroupsLazyInitialization() 

  public void testAddChildStemWithBadExtnOrDisplayExtn() {
    LOG.info("testAddChildStemWithBadExtnOrDisplayExtn");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      try {
        Stem badE = root.addChildStem(null, "test");
        Assert.fail("added stem with null extn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("null extn", true);
      }
      try {
        Stem badE = root.addChildStem("", "test");
        Assert.fail("added stem with empty extn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("empty extn", true);
      }
      try {
        Stem badE = root.addChildStem("a:test", "test");
        Assert.fail("added stem with colon-containing extn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("colon-containing extn", true);
      }
      try {
        Stem badE = root.addChildStem("test", null);
        Assert.fail("added stem with null displayExtn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("null displayExtn", true);
      }
      try {
        Stem badE = root.addChildStem("test", "");
        Assert.fail("added stem with empty displayextn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("empty displayExtn", true);
      }
      try {
        Stem badE = root.addChildStem("test", "a:test");
        Assert.fail("added stem with colon-containing displayExtn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("colon-containing displayExtn", true);
      }
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testAddChildStemWithBadExtnOrDisplayExtn()

  public void testSetBadStemDisplayExtension() {
    LOG.info("testSetBadStemDisplayExtension");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "edu");
      try {
        edu.setDisplayExtension(null);
        Assert.fail("set null displayExtn");
      }
      catch (StemModifyException eSA) {
        Assert.assertTrue("null displayExtn", true);
      }
      try {
        edu.setDisplayExtension("");
        Assert.fail("set empty displayExtn");
      }
      catch (StemModifyException eSA) {
        Assert.assertTrue("empty displayExtn", true);
      }
      try {
        edu.setDisplayExtension("a:test");
        Assert.fail("set colon-containing displayExtn");
      }
      catch (StemModifyException eSA) {
        Assert.assertTrue("colon-containing displayExtn", true);
      }
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetBadStemDisplayExtension()

}

