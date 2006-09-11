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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestMembership2.java,v 1.1 2006-09-11 16:58:02 blair Exp $
 */
public class TestMembership2 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestMembership2.class);

  public TestMembership2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testCreationTimesAndCreators() {
    LOG.info("testCreationTimesAndCreators");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");

      Date    before  = DateHelper.getPastDate();
      gA.addMember(subjA);
      gB.addMember( gA.toSubject() );
      gC.grantPriv( subjA, AccessPrivilege.ADMIN );
      gC.setSession( GrouperSession.start(subjA) );
      gC.addCompositeMember(CompositeType.INTERSECTION, gA, gB);
      Date    future  = DateHelper.getFutureDate();

      List imms   = new ArrayList( gA.getImmediateMemberships() );
      T.amount("immediate mships", 1, imms.size());
      Membership i = (Membership) imms.get(0); 
      Assert.assertTrue("i ctime after $BEFORE"   , i.getCreateTime().getTime() > before.getTime());
      Assert.assertTrue("i ctime before $FUTURE"  , i.getCreateTime().getTime() < future.getTime());
      Assert.assertTrue("i creator" , i.getCreator().equals(r.rs.getMember()));

      List effs   = new ArrayList( gB.getEffectiveMemberships() );
      T.amount("effective mships", 1, effs.size());
      Membership e = (Membership) effs.get(0); 
      Assert.assertTrue("e ctime after $BEFORE"   , e.getCreateTime().getTime() > before.getTime());
      Assert.assertTrue("e ctime before $FUTURE"  , e.getCreateTime().getTime() < future.getTime());
      Assert.assertTrue("e creator" , e.getCreator().equals(r.rs.getMember()));

      List comps  = new ArrayList( gC.getCompositeMemberships() );
      T.amount("composite mships", 1, comps.size());
      Membership c = (Membership) comps.get(0); 
      Assert.assertTrue("c ctime after $BEFORE"   , c.getCreateTime().getTime() > before.getTime());
      Assert.assertTrue("c ctime before $FUTURE"  , c.getCreateTime().getTime() < future.getTime());
      Assert.assertTrue("c creator" , c.getCreator().equals(gC.getSession().getMember()));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCreationTimesAndCreators()

} // public class TestMembership2

