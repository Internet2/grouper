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

package test.edu.internet2.middleware.grouper;


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test using the wrong field type in various operations.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestWrongFieldType.java,v 1.3 2006-02-21 17:11:33 blair Exp $
 */
public class TestWrongFieldType extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestWrongFieldType.class); 


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;
  private static Group          ub;
  private static Group          uw;
  


  public TestWrongFieldType(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    ub    = StemHelper.addChildGroup(edu, "ub", "ub");
    uw    = StemHelper.addChildGroup(edu, "uw", "uw");
    subj0 = SubjectHelper.SUBJ0;
    subj1 = SubjectHelper.SUBJ1;
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }


  // Tests

  public void testNaming() {
    LOG.info("testNaming");
    try {
      root.revokePriv(AccessPrivilege.ADMIN); 
      Assert.fail("revoked access privilege on a stem");
    }
    catch (SchemaException eS) {
      Assert.assertTrue(eS.getMessage(), true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testNaming()

  public void testAccess() {
    LOG.info("testAccess");
    try {
      i2.revokePriv(NamingPrivilege.STEM); 
      Assert.fail("revoked naming privilege on a group");
    }
    catch (SchemaException eS) {
      Assert.assertTrue(eS.getMessage(), true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testAccess()

  public void testGroupAttrs() {
    LOG.info("testGroupAttrs");
    try {
      i2.setAttribute("members", "members");
      Assert.fail("set list-as-attribute");
    }
    catch (AttributeNotFoundException eANF) {
      Assert.assertTrue(eANF.getMessage(), true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGroupAttrs()

  public void testGroupLists() {
    LOG.info("testGroupLists");
    try {
      Field f = FieldFinder.find("name");
      i2.addMember(subj0, f);
      Assert.fail("set attribute-as-list");
    }
    catch (SchemaException eS) {
      Assert.assertTrue(eS.getMessage(), true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGroupLists()

}

