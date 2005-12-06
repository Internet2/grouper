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
 * @version $Id: TestStem.java,v 1.4 2005-12-06 19:42:19 blair Exp $
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
      Assert.assertTrue("FIXME modifier", modifier.equals(s.getSubject()));
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
      Assert.assertTrue("modifier", modifier.equals(s.getSubject()));
    }
    catch (SubjectNotFoundException eSNF) {
      Assert.fail("no modify subject");
    }
    Date d = edu.getModifyTime();
    Assert.assertNotNull("modify time !null", d);
    Assert.assertTrue("modify time instanceof Date", d instanceof Date); 
    Assert.assertTrue("modify time != epoch", !d.equals(new Date()));
  } // public void testGetModifyAttrsModified()
}

