/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

package edu.internet2.middleware.grouper.membership;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.helper.DateHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestMembership2.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestMembership2 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMembership2.class);

  public TestMembership2(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembership2("testCreationTimesAndCreators"));
  }
  
  public void testCreationTimesAndCreators() {
    LOG.info("testCreationTimesAndCreators");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");

      GrouperUtil.sleep(100);
      Date before  = new Date();
      GrouperUtil.sleep(100);
      gA.addMember(subjA);
      gB.addMember( gA.toSubject() );
      gC.grantPriv( subjA, AccessPrivilege.ADMIN );
      GrouperSession.start(subjA);
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
      Assert.assertTrue("c creator" , c.getCreator().equals(GrouperSession.staticGrouperSession().getMember()));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCreationTimesAndCreators()

} // public class TestMembership2

