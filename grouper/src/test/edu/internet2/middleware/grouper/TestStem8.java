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
import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStem8.java,v 1.11 2009-03-18 18:51:58 shilen Exp $
 */
public class TestStem8 extends TestCase {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestStem8.class);


  public TestStem8(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
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
    edu = StemFinder.findByName(s, edu.getName(), true);

    
    try {
      Subject modifier = edu.getModifySubject();
      Assert.fail("no exception thrown");
    }
    catch (SubjectNotFoundException eSNF) {
    }
    
    Date  d       = edu.getModifyTime();
    Assert.assertTrue("modify time null", d.getTime() == 0);
    
    d = edu.getLastMembershipChange();
    Assert.assertNotNull("last membership change !null", d);
    Assert.assertTrue("last membership change instanceof Date", d instanceof Date);
    
    long  modify  = d.getTime();
    long  epoch   = new Date(0).getTime();
    Assert.assertFalse(
      "modify[" + modify + "] != epoch[" + epoch + "]",
      modify == epoch
    );
  } // public void testGetModifyAttrsModified()

}

