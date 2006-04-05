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
 * @version $Id: TestUnionFactor7.java,v 1.1 2006-04-05 19:20:19 blair Exp $
 */
public class TestUnionFactor7 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestUnionFactor7.class);

  public TestUnionFactor7(String name) {
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

  public void testAddMemberToUnionMember() {
    LOG.info("testAddMemberToUnionMember");
    try {
      R r = R.createOneStemAndEightGroups();
      try {
        r.ua.addMember(r.subj0);
        r.uc.addFactor( new UnionFactor(r.ua, r.ub) );
        r.rs.waitForTx();
        r.rs.flushCache("edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwner");
        Assert.assertTrue("populated feeder groups", true);

        T.getMembers(r.ua, 1);
        T.getMembers(r.ub, 0);
        T.getMembers(r.uc, 1);

        Assert.assertTrue(  "ua isFactor"       , r.ua.isFactor());
        Assert.assertTrue(  "ub isFactor"       , r.ub.isFactor());
        Assert.assertFalse( "uc !isFactor"      , r.uc.isFactor());

        Assert.assertFalse( "ua !hasFactor"     , r.ua.hasFactor());
        Assert.assertFalse( "ub !hasFactor"     , r.ub.hasFactor());
        Assert.assertTrue(  "uc hasFactor"      , r.uc.hasFactor());

        r.ub.addMember(r.subj1);
        r.rs.waitForTx();
        r.rs.flushCache("edu.internet2.middleware.grouper.MembershipFinder.FindMembershipsOwner");
        Assert.assertTrue("added member", true);

        T.getMembers(r.ua, 1);
        T.getMembers(r.ub, 1);
        T.getMembers(r.uc, 2);

        Assert.assertTrue(  "ua isFactor"       , r.ua.isFactor());
        Assert.assertTrue(  "ub isFactor"       , r.ub.isFactor());
        Assert.assertFalse( "uc !isFactor"      , r.uc.isFactor());

        Assert.assertFalse( "ua !hasFactor"     , r.ua.hasFactor());
        Assert.assertFalse( "ub !hasFactor"     , r.ub.hasFactor());
        Assert.assertTrue(  "uc hasFactor"      , r.uc.hasFactor());
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
  } // public void testAddMemberToUnionMember()

}

