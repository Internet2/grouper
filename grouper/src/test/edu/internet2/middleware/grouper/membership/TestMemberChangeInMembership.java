/**
 * Copyright 2014 Internet2
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
 */
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

import junit.framework.TestCase;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 * @version $Id: TestMemberChangeInMembership.java,v 1.2 2009-12-07 07:31:08 mchyzer Exp $
 */
public class TestMemberChangeInMembership extends GrouperTest {

  R r = null;

  /**
   * @param name
   */
  public TestMemberChangeInMembership(String name) {
    super(name);
  }

  protected void tearDown () {
    super.tearDown();
    if (r != null) {
      r.rs.stop();
    }
  }
  
  /**
   * @throws Exception
   */
  public void testChangingMember() throws Exception {
    
    r = R.populateRegistry(0, 0, 4);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    Subject d = r.getSubject("d");
    
    Stem root = StemFinder.findRootStem(r.rs);

    Stem stem = root.addChildStem("stem", "stem");
    Group top1 = stem.addChildGroup("top1", "top1");
    Group top2 = stem.addChildGroup("top2", "top2");

    Group one = stem.addChildGroup("one", "one");
    Group two = stem.addChildGroup("two", "two");

    Group owner = stem.addChildGroup("owner", "owner");
    Group left = stem.addChildGroup("left", "left");
    Group right = stem.addChildGroup("right", "right");

    owner.addCompositeMember(CompositeType.COMPLEMENT, left, right);
    left.addMember(one.toSubject());
    left.addMember(a);
    one.addMember(two.toSubject());
    one.addMember(b);
    two.addMember(c);
    
    top1.addMember(owner.toSubject());
    top2.grantPriv(owner.toSubject(), AccessPrivilege.UPDATE);
    stem.grantPriv(owner.toSubject(), NamingPrivilege.CREATE);
    
    int numberOfMembershipsBefore = GrouperDAOFactory.getFactory().getMembership().findAll(false).size();
    assertTrue(two.hasMember(c));
    assertTrue(one.hasMember(c));
    assertTrue(left.hasMember(c));
    assertTrue(owner.hasMember(c));
    assertTrue(top1.hasMember(c));
    assertTrue(top2.hasUpdate(c));
    assertTrue(stem.hasCreate(c));
    
    // change member
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        two.getUuid(), MemberFinder.findBySubject(r.rs, c, true).getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setMemberUuid(MemberFinder.findBySubject(r.rs, d, true).getUuid());
    ms.setMember(MemberFinder.findBySubject(r.rs, d, true));
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();
    
    int numberOfMembershipsAfter = GrouperDAOFactory.getFactory().getMembership().findAll(false).size();
    assertEquals(numberOfMembershipsBefore, numberOfMembershipsAfter);
    
    assertTrue(two.hasMember(d));
    assertTrue(one.hasMember(d));
    assertTrue(left.hasMember(d));
    assertTrue(owner.hasMember(d));
    assertTrue(top1.hasMember(d));
    assertTrue(top2.hasUpdate(d));
    assertTrue(stem.hasCreate(d));
    
    assertFalse(two.hasMember(c));
    assertFalse(one.hasMember(c));
    assertFalse(left.hasMember(c));
    assertFalse(owner.hasMember(c));
    assertFalse(top1.hasMember(c));
    assertFalse(top2.hasUpdate(c));
    assertFalse(stem.hasCreate(c));
  }
}

