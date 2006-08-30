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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup5.java,v 1.3 2006-08-30 18:35:38 blair Exp $
 */
public class TestGroup5 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroup5.class);


  public TestGroup5(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testGroupDeleteWhenHasMemberViaTwoPaths() {
    LOG.info("testGroupDeleteWhenHasMemberViaTwoPaths");
    try {
      R       r     = R.populateRegistry(1, 4, 1);
      Group   a     = r.getGroup("a", "a");
      Subject aSubj = a.toSubject();
      Group   b     = r.getGroup("a", "b");
      Subject bSubj = b.toSubject();
      Group   c     = r.getGroup("a", "c");
      Subject cSubj = c.toSubject();
      Group   d     = r.getGroup("a", "d");
      Subject subjA = r.getSubject("a");

      a.addMember(subjA);
      b.addMember(aSubj);
      c.addMember(aSubj);
      d.addMember(bSubj);
      d.addMember(cSubj);
      T.amount("a has 1 memberships", 1, a.getMemberships().size());
      T.amount("b has 2 memberships", 2, b.getMemberships().size());
      T.amount("c has 2 memberships", 2, c.getMemberships().size());
      T.amount("d has 6 memberships", 6, d.getMemberships().size());

      a.delete();
      Assert.assertTrue("group deleted", true);

      T.amount("b now has 0 memberships", 0, b.getMemberships().size());
      T.amount("c now has 0 memberships", 0, c.getMemberships().size());
      T.amount("d now has 2 memberships", 2, d.getMemberships().size());

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupDeleteWhenHasMemberViaTwoPaths()

}

