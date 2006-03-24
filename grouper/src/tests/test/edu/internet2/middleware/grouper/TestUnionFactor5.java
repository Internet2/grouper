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
 * @version $Id: TestUnionFactor5.java,v 1.1 2006-03-24 19:38:12 blair Exp $
 */
public class TestUnionFactor5 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestUnionFactor5.class);

  public TestUnionFactor5(String name) {
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

  public void testAddUnionFactorWithUnionMember() {
    LOG.info("testAddUnionFactorWithUnionMember");
    try {
      R r = R.createOneStemAndEightGroups();
      try {
        r.ua.addMember(r.subj0);
        r.ub.addMember(r.subj1);
        r.uc.addMember(r.subj2);
        r.ud.addFactor( new UnionFactor(r.ua, r.ub) );
        r.rs.waitForTx();
        r.rs.flushCache("edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwner");
        Assert.assertTrue("populated feeder groups", true);

        T.getMembers(r.i2, 0);
        T.getMembers(r.ua, 1);
        T.getMembers(r.ub, 1);
        T.getMembers(r.uc, 1);
        T.getMembers(r.ud, 2);

        r.i2.addFactor( new UnionFactor(r.uc, r.ud) );
        r.rs.waitForTx();
        r.rs.flushCache("edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwner");
        Assert.assertTrue("added union factor", true);

        Assert.assertFalse( "i2 !isFactor"      , r.i2.isFactor());
        Assert.assertTrue(  "ua isFactor"       , r.ua.isFactor());
        Assert.assertTrue(  "ub isFactor"       , r.ub.isFactor());
        Assert.assertTrue(  "uc isFactor"       , r.uc.isFactor());
        Assert.assertTrue(  "ud isFactor"       , r.ud.isFactor());

        Assert.assertTrue(  "i2 hasFactor"      , r.i2.hasFactor());
        Assert.assertFalse( "ua !hasFactor"     , r.ua.hasFactor());
        Assert.assertFalse( "ub !hasFactor"     , r.ub.hasFactor());
        Assert.assertFalse( "uc !hasFactor"     , r.uc.hasFactor());
        Assert.assertTrue(  "ud hasFactor"      , r.ud.hasFactor());

        T.getMembers(r.i2, 3);
        T.getMembers(r.ua, 1);
        T.getMembers(r.ub, 1);
        T.getMembers(r.uc, 1);
        T.getMembers(r.ud, 2);
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
  } // public void testAddUnionFactorWithUnionMember()

}

