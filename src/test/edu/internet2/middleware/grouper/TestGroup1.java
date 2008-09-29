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
import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup1.java,v 1.6 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestGroup1 extends TestCase {

  private static final Log LOG = GrouperUtil.getLog(TestGroup1.class);

  public TestGroup1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testDeleteGroupMemberWithNonGroupMember() {
    LOG.info("testDeleteGroupMemberWithNonGroupMember");
    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   a     = r.getGroup("a", "a");
      Subject aSubj = a.toSubject();
      Group   b     = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");

      Assert.assertFalse( "[0] a !has subjA"  , a.hasMember(subjA)  );
      Assert.assertFalse( "[0] b !has a"      , b.hasMember(aSubj)  );
      Assert.assertFalse( "[0] b !has subjA"  , b.hasMember(subjA)  );

      a.addMember(subjA);
      Assert.assertTrue(  "[1] a has subjA"   , a.hasMember(subjA)  );
      Assert.assertFalse( "[1] b !has a"      , b.hasMember(aSubj)  );
      Assert.assertFalse( "[0] b !has subjA"  , b.hasMember(subjA)  );

      b.addMember(aSubj);
      Assert.assertTrue(  "[2] a has subjA"   , a.hasMember(subjA)  );
      Assert.assertTrue(  "[2] b has a"       , b.hasMember(aSubj)  );
      Assert.assertTrue(  "[2] b has subjA"   , b.hasMember(subjA)  );

      b.deleteMember(aSubj);
      Assert.assertTrue(  "[3] a has subjA"   , a.hasMember(subjA)  );
      Assert.assertFalse( "[3] b !has a"      , b.hasMember(aSubj)  );
      Assert.assertFalse( "[3] b !has subjA"  , b.hasMember(subjA)  );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteGroupMemberWithNonGroupMember()

}

