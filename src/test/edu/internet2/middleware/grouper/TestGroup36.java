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
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup36.java,v 1.8 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestGroup36 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestGroup36.class);

  public TestGroup36(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testCompositeIsDeletedWhenGroupIsDeleted() {
    LOG.info("testCompositeIsDeletedWhenGroupIsDeleted");
    try {
      R           r     = R.populateRegistry(1, 3, 1);
      Group       gA    = r.getGroup("a", "a");
      Group       gB    = r.getGroup("a", "b");
      Group       gC    = r.getGroup("a", "c");
      Subject     subjA = r.getSubject("a");
      gB.addMember(subjA);
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      Member      mA    = MemberFinder.findBySubject(r.rs, subjA);
      T.amount("subjA mships before deletion", 2, mA.getMemberships().size());
      MembershipFinder.findCompositeMembership(
        r.rs, gA, subjA
      );  
      gA.delete(); 
      mA    = MemberFinder.findBySubject(r.rs, subjA);
      T.amount("subjA mships after deletion", 1, mA.getMemberships().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCompositeIsDeletedWhenGroupIsDeleted()

} // public class TestGroup36 extends GrouperTest

