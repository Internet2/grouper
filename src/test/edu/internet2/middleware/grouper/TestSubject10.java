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
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.subj.GrouperSubject;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestSubject10.java,v 1.9 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestSubject10 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestSubject10.class);

  public TestSubject10(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGrouperSubjectEqual() {
    LOG.info("testGrouperSubjectEqual");
    try {
      R               r       = R.populateRegistry(1, 2, 1);
      Group           gA      = r.getGroup("a", "a");
      Group           gB      = r.getGroup("a", "b");
      Subject         subjA   = r.getSubject("a");
      GrouperSubject  subjGA  = new GrouperSubject( gA);
      Assert.assertTrue(
        "gA == gA"    , subjGA.equals(gA.toSubject())
      );
      Assert.assertFalse(
        "gA != gB"    , subjGA.equals(gB.toSubject())
      );
      Assert.assertFalse(
        "gA != subjA" , subjGA.equals(subjA)
      );
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGrouperSubjectEqual()

}

