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

package edu.internet2.middleware.grouper;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: Test_Integration_Stem_setExtension.java,v 1.1 2007-02-19 20:43:29 blair Exp $
 * @since   1.2.0
 */
public class Test_Integration_Stem_setExtension extends GrouperTest {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Test_Integration_Stem_setExtension.class);


  // TESTS //  

  // TODO 20070219 add mock priv adapter and add version of this to unit tests?
  public void testSetExtension_NotPrivileged() {
    try {
      LOG.info("testSetExtension_NotPrivileged");
      R     r     = new R();
      r.startAllSession();
      Stem  root  = r.findRootStem();
      root.setExtension("i should not be privileged to set this");
      fail("should have thrown InsufficientPrivilegeException");
    }
    catch (InsufficientPrivilegeException eIP) {
      assertTrue(true);
      assertEquals( E.CANNOT_STEM, eIP.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_NotPrivileged()

/*
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
    // TODO 20070219 hack! hack! hack!
    root.getDTO().setDisplayExtension(Stem.ROOT_INT);
    root.getDTO().setDisplayName(Stem.ROOT_INT);
    HibernateDAO.update( root.getDTO() );
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
    try {
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
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPropagateExtensionChangeAsNonRoot()
*/

} // public class Test_Integration_Stem_setExtension extends GrouperTest

