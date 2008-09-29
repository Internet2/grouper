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
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStem6.java,v 1.10 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestStem6 extends GrouperTest {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestStem6.class);

  public static void main(String[] args) {
    TestRunner.run(new TestStem6("testGetCreateAttrs"));
  }

  public TestStem6(String name) {
    super(name);
  }

  protected void setUp () {
    super.setUp();
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    super.tearDown();
    LOG.debug("tearDown");
  }

  public void testGetCreateAttrs() {
    LOG.info("testGetCreateAttrs");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemHelper.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "education");
      try {
        Subject creator = edu.getCreateSubject();
        Assert.assertNotNull("creator !null", creator);
        Assert.assertTrue("creator", creator.equals(s.getSubject()));
      }
      catch (SubjectNotFoundException eSNF) {
        Assert.fail("no create subject");
      }
      Date  d       = edu.getCreateTime();
      Assert.assertNotNull("create time !null", d);
      Assert.assertTrue("create time instanceof Date", d instanceof Date);
      long  create  = d.getTime();
      long  epoch   = new Date(0).getTime();
      Assert.assertFalse(
        "create[" + create + "] != epoch[" + epoch + "]",
        create == epoch
      );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetCreateAttrs()

}

