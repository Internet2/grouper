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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * @author  blair christensen.
 * @version $Id: TestUnionFactor4.java,v 1.1 2006-03-23 18:36:31 blair Exp $
 */
public class TestUnionFactor4 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestUnionFactor4.class);

  public TestUnionFactor4(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  public void testAddUnionFactorWithMembers() {
    LOG.info("testAddUnionFactorWithMembers");
    try {
      R r = R.createOneStemAndFourGroups();
      try {
        r.ub.addMember(r.subj0);
        r.uc.addMember(r.subj1);

        T.getMembers(r.i2, 0);
        T.getMembers(r.ub, 1);
        T.getMembers(r.uc, 1);

        r.i2.addFactor( new UnionFactor(r.ub, r.uc) );
        r.rs.waitForTx();
        r.rs.flushCache("edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwner");
        Assert.assertTrue(  "added union factor", true);

        Assert.assertFalse( "i2 !isFactor"      , r.i2.isFactor());
        Assert.assertTrue(  "ub isFactor"       , r.ub.isFactor());
        Assert.assertTrue(  "uc isFactor"       , r.uc.isFactor());

        Assert.assertTrue(  "i2 hasFactor"      , r.i2.hasFactor());
        Assert.assertFalse( "ub !hasFactor"     , r.ub.hasFactor());
        Assert.assertFalse( "uc !hasFactor"     , r.uc.hasFactor());

        T.getMembers(r.i2, 2);
        T.getMembers(r.ub, 1);
        T.getMembers(r.uc, 1);
      }
      catch (Exception e) {
        Assert.fail(e.getMessage());
      } 
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testAddUnionFactorWithMembers()

}

