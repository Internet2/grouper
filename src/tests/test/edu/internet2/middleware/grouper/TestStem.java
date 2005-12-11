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
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStem.java,v 1.6 2005-12-11 04:16:31 blair Exp $
 */
public class TestStem extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestStem.class);


  public TestStem(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    Db.refreshDb();
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
      SessionHelper.getSession(SubjectHelper.SUBJ0_ID)
    );
    Stem  rootB = StemHelper.findRootStem(
      SessionHelper.getSession(SubjectHelper.SUBJ0_ID)
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

  public void testGetCreateAttrs() {
    LOG.info("testGetCreateAttrs");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Assert.assertTrue("create source", edu.getCreateSource().equals(""));
    try {
      Subject creator = edu.getCreateSubject();
      Assert.assertNotNull("creator !null", creator);
      Assert.assertTrue("creator", creator.equals(s.getSubject()));
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.fail("no create subject");
    }
    Date d = edu.getCreateTime();
    Assert.assertNotNull("create time !null", d);
    Assert.assertTrue("create time instanceof Date", d instanceof Date); 
    Assert.assertTrue("create time != epoch", !d.equals(new Date()));
  } // public void testGetCreateAttrs()

  public void testGetModifyAttrsNotModified() {
    LOG.info("testGetModifyAttrsNotModified");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Assert.assertTrue("modify source", edu.getModifySource().equals(""));
    // TODO Unfortunately, the modify* attrs currently get set due to
    //      the granting of STEM at stem creation.  Fuck.
    try {
      Subject modifier = edu.getModifySubject();
      Assert.assertNotNull("FIXME modifier !null", modifier);
      Assert.assertTrue(
        "FIXME modifier", SubjectHelper.eq(modifier, s.getSubject())
      );
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.fail("FIXME no modify subject");
    }
    Date d = edu.getModifyTime();
    Assert.assertNotNull("modify time !null", d);
    Assert.assertTrue("modify time instanceof Date", d instanceof Date); 
    Assert.assertTrue("FIXME modify time != epoch", !d.equals(new Date()));
  } // public void testGetModifyAttrsNotModified()

  public void testGetModifyAttrsModified() {
    LOG.info("testGetModifyAttrsModified");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Assert.assertTrue("modify source", edu.getModifySource().equals(""));
    try {
      Subject modifier = edu.getModifySubject();
      Assert.assertNotNull("modifier !null", modifier);
      Assert.assertTrue(
        "modifier", SubjectHelper.eq(modifier, s.getSubject())
      );
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.fail("no modify subject");
    }
    Date d = edu.getModifyTime();
    Assert.assertNotNull("modify time !null", d);
    Assert.assertTrue("modify time instanceof Date", d instanceof Date); 
    Assert.assertTrue("modify time != epoch", !d.equals(new Date()));
  } // public void testGetModifyAttrsModified()

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
    Assert.assertTrue("root displayExtn", root.getDisplayExtension().equals(exp));
    Assert.assertTrue("root displayName", root.getDisplayName().equals(exp));
    exp = root.getDisplayName() + "education";
    Assert.assertTrue("edu displayExtn" , edu.getDisplayExtension().equals(exp));
    Assert.assertTrue("edu displayName" , edu.getDisplayName().equals(exp));
    exp = edu.getDisplayName() + ":internet2";
    Assert.assertTrue("i2 displayName", i2.getDisplayName().equals(exp));
    exp = edu.getDisplayName() + ":uchicago";
    Assert.assertTrue("uofc displayName", uofc.getDisplayName().equals(exp));
    exp = uofc.getDisplayName() + ":biological sciences division";
    Assert.assertTrue("bsd displayName" , bsd.getDisplayName().equals(exp));
    exp = uofc.getDisplayName() + ":physical sciences division";
    Assert.assertTrue("bsd displayName" , psd.getDisplayName().equals(exp));
   
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
    Assert.assertTrue("bsd displayName" , psd.getDisplayName().equals(exp));
   
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
    PrivHelper.grantPriv(s, edu, SubjectHelper.SUBJ0, NamingPrivilege.STEM);
    // And revoke VIEW + READ from ALL on one of the child groups
    PrivHelper.revokePriv(s, psd, SubjectHelper.SUBJA, AccessPrivilege.VIEW);
    PrivHelper.revokePriv(s, psd, SubjectHelper.SUBJA, AccessPrivilege.READ);
   
    // Now rename as subj0
    GrouperSession  nrs   = SessionHelper.getSession(SubjectHelper.SUBJ0_ID);
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

}

