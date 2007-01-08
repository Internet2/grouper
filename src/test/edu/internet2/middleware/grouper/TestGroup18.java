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
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup18.java,v 1.4 2007-01-08 16:43:56 blair Exp $
 */
public class TestGroup18 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroup18.class);


  public TestGroup18(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailCanReadFieldValidSubjectValidFieldNotRoot() {
    LOG.info("testFailCanReadFieldValidSubjectValidFieldNotRoot");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");

      GrouperSession s = GrouperSession.start(subjA);
      a.internal_setSession(s);
      Assert.assertFalse(
        "cannot read", 
        a.canReadField(subjA, FieldFinder.find("admins"))
      );
      s.stop();

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldValidSubjectValidFieldNotRoot()

}

