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
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStem8.java,v 1.4 2006-08-30 19:31:02 blair Exp $
 */
public class TestStem8 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestStem8.class);


  public TestStem8(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGetModifyAttrsModified() {
    LOG.info("testGetModifyAttrsModified");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    StemHelper.addChildGroup(edu, "i2", "internet2");
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
    Date  d       = edu.getModifyTime();
    Assert.assertNotNull("modify time !null", d);
    Assert.assertTrue("modify time instanceof Date", d instanceof Date);
    long  modify  = d.getTime();
    long  epoch   = new Date(0).getTime();
    Assert.assertFalse(
      "modify[" + modify + "] != epoch[" + epoch + "]",
      modify == epoch
    );
  } // public void testGetModifyAttrsModified()

}

