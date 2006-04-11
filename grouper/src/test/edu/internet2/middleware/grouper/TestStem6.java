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
 * @version $Id: TestStem6.java,v 1.1.2.1 2006-04-11 16:45:49 blair Exp $
 */
public class TestStem6 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestStem6.class);


  public TestStem6(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

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
    Date  d       = edu.getCreateTime();
    Assert.assertNotNull("create time !null", d);
    Assert.assertTrue("create time instanceof Date", d instanceof Date);
    long  create  = d.getTime();
    long  epoch   = new Date(0).getTime();
    Assert.assertFalse(
      "create[" + create + "] != epoch[" + epoch + "]",
      create == epoch
    );
  } // public void testGetCreateAttrs()

}

