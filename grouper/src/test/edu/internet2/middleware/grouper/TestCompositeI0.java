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
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeI0.java,v 1.1 2006-06-02 17:35:07 blair Exp $
 */
public class TestCompositeI0 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeI0.class);

  public TestCompositeI0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailNotPrivilegedToAddCompositeMember() {
    LOG.info("testFailNotPrivilegedToAddCompositeMember");
    try {
      R               r   = R.populateRegistry(1, 3, 1);
      GrouperSession  nrs = GrouperSession.start( r.getSubject("a") );
      Group           a   = r.getGroup("a", "a");
      a.setSession(nrs);
      a.addCompositeMember(
        CompositeType.INTERSECTION, r.getGroup("a", "b"), r.getGroup("a", "c")
      );
      r.rs.stop();
      nrs.stop();
      Assert.fail("added composite without privilege to add composite");
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("OK: cannot add union without privileges", true);
      String exp = "a does not have update on 'i2:a:a'"; // TODO fragile
      T.string("error message", exp, eIP.getMessage());
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailNotPrivilegedToAddCompositeMember()

}

