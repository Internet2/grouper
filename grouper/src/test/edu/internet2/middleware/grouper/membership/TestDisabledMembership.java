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

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 * @version $Id: TestDisabledMembership.java,v 1.1 2009-08-18 23:11:39 shilen Exp $
 */
public class TestDisabledMembership extends TestCase {

  R r = null;

  /**
   * @param name
   */
  public TestDisabledMembership(String name) {
    super(name);
  }

  protected void setUp () throws Exception {
    RegistryReset.reset();
  }

  protected void tearDown () {
    
    if (r != null) {
      r.rs.stop();
    }
  }
  
  /**
   * @throws Exception
   */
  public void testDisablingMembership() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    
    Timestamp disabledTime = new Timestamp(new Date().getTime() - 100000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 100000);
    
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

    verifyMemberships(20, 6, 6, true, true, true, true, true, true);
    
    // disable owner -> top1
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        top1.getUuid(), owner.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();

    verifyMemberships(14, 6, 6, false, true, true, true, true, true);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        top1.getUuid(), owner.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, false);
    ms.setEnabled(true);
    ms.setEnabledTime(null);
    ms.setDisabledTime(null);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();
    
    verifyMemberships(20, 6, 6, true, true, true, true, true, true);
    
    // disable owner -> top2 (update priv)
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        top2.getUuid(), owner.toMember().getUuid(), FieldFinder.find("updaters", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();

    verifyMemberships(20, 6, 0, true, false, true, true, true, true);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        top2.getUuid(), owner.toMember().getUuid(), FieldFinder.find("updaters", true), Membership.IMMEDIATE, true, false);
    ms.setEnabled(true);
    ms.setEnabledTime(null);
    ms.setDisabledTime(null);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();
    
    verifyMemberships(20, 6, 6, true, true, true, true, true, true);
    
    // disable owner -> stem (create priv)
    ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        stem.getUuid(), owner.toMember().getUuid(), FieldFinder.find("creators", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();

    verifyMemberships(20, 0, 6, true, true, false, true, true, true);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        stem.getUuid(), owner.toMember().getUuid(), FieldFinder.find("creators", true), Membership.IMMEDIATE, true, false);
    ms.setEnabled(true);
    ms.setEnabledTime(null);
    ms.setDisabledTime(null);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();
    
    verifyMemberships(20, 6, 6, true, true, true, true, true, true);
    
    // disable one -> left
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        left.getUuid(), one.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();

    verifyMemberships(8, 2, 2, true, true, true, false, true, true);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        left.getUuid(), one.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, false);
    ms.setEnabled(true);
    ms.setEnabledTime(null);
    ms.setDisabledTime(null);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();
    
    verifyMemberships(20, 6, 6, true, true, true, true, true, true);

    
    // disable two -> one
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        one.getUuid(), two.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();

    verifyMemberships(12, 4, 4, true, true, true, true, false, true);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        one.getUuid(), two.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, false);
    ms.setEnabled(true);
    ms.setEnabledTime(null);
    ms.setDisabledTime(null);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();
    
    verifyMemberships(20, 6, 6, true, true, true, true, true, true);
    
    
    // disable SC -> two
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        two.getUuid(), MemberFinder.findBySubject(r.rs, c, true).getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();

    verifyMemberships(15, 5, 5, true, true, true, true, true, false);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        two.getUuid(), MemberFinder.findBySubject(r.rs, c, true).getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, false);
    ms.setEnabled(true);
    ms.setEnabledTime(null);
    ms.setDisabledTime(null);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    GrouperCacheUtils.clearAllCaches();
    
    verifyMemberships(20, 6, 6, true, true, true, true, true, true);
  }
  
  private void verifyMemberships(int members, int creators, int updaters, boolean top1ToOwnerEnabled,
      boolean top2ToOwnerEnabled, boolean stemToOwnerEnabled, boolean leftToOneEnabled, boolean oneToTwoEnabled,
      boolean twoToSCEnabled) throws Exception {
    int actualCreators = 0;
    int actualMembers = 0;
    int actualUpdaters = 0;
    
    Stem stem = StemFinder.findByName(r.rs, "stem", true);
    Group top1 = GroupFinder.findByName(r.rs, "stem:top1", true);
    Group top2 = GroupFinder.findByName(r.rs, "stem:top2", true);
    Group one = GroupFinder.findByName(r.rs, "stem:one", true);
    Group two = GroupFinder.findByName(r.rs, "stem:two", true);
    Group owner = GroupFinder.findByName(r.rs, "stem:owner", true);
    Group left = GroupFinder.findByName(r.rs, "stem:left", true);
    GroupFinder.findByName(r.rs, "stem:right", true);    
    Member a = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
    Member b = MemberFinder.findBySubject(r.rs, r.getSubject("b"), true);
    Member c = MemberFinder.findBySubject(r.rs, r.getSubject("c"), true);
    
    Field updatersField = FieldFinder.find("updaters", true);
    Field creatorsField = FieldFinder.find("creators", true);
    Field membersField = Group.getDefaultList();
    
    Set<Membership> allEnabledMemberships = GrouperDAOFactory.getFactory().getMembership().findAll(true);
    Iterator<Membership> iter = allEnabledMemberships.iterator();
    while (iter.hasNext()) {
      Membership membership = iter.next();
      if (membership.getFieldId().equals(creatorsField.getUuid())) {
        actualCreators++;
      } else if (membership.getFieldId().equals(updatersField.getUuid())) {
        actualUpdaters++;
      } else if (membership.getFieldId().equals(membersField.getUuid())) {
        actualMembers++;
      }
    }
    
    assertEquals(members, actualMembers);
    assertEquals(creators, actualCreators);
    assertEquals(updaters, actualUpdaters);
    
    
    if (top1ToOwnerEnabled) {
      Membership ms1 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top1.getUuid(), owner.toMember().getUuid(), membersField, Membership.IMMEDIATE, true, true);
      assertEquals(0, ms1.getDepth());
      
      Membership ms2 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top1.getUuid(), a.getUuid(), membersField, Membership.EFFECTIVE, true, true);
      assertEquals(1, ms2.getDepth());
      
      if (leftToOneEnabled) {
        Membership ms3 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top1.getUuid(), one.toMember().getUuid(), membersField, Membership.EFFECTIVE, true, true);
        assertEquals(1, ms3.getDepth());
        
        Membership ms4 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top1.getUuid(), b.getUuid(), membersField, Membership.EFFECTIVE, true, true);
        assertEquals(1, ms4.getDepth());
       
        if (oneToTwoEnabled) {
          Membership ms5 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top1.getUuid(), two.toMember().getUuid(), membersField, Membership.EFFECTIVE, true, true);
          assertEquals(1, ms5.getDepth());
          
          if (twoToSCEnabled) {
            Membership ms6 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top1.getUuid(), c.getUuid(), membersField, Membership.EFFECTIVE, true, true);
            assertEquals(1, ms6.getDepth());
          }
        }
      }
    }
    
    Membership ms7 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(left.getUuid(), a.getUuid(), membersField, Membership.IMMEDIATE, true, true);
    assertEquals(0, ms7.getDepth());
    
    if (leftToOneEnabled) {
      Membership ms8 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(left.getUuid(), one.toMember().getUuid(), membersField, Membership.IMMEDIATE, true, true);
      assertEquals(0, ms8.getDepth());
      
      Membership ms9 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(left.getUuid(), b.getUuid(), membersField, Membership.EFFECTIVE, true, true);
      assertEquals(1, ms9.getDepth());
      
      if (oneToTwoEnabled) {
        Membership ms10 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(left.getUuid(), two.toMember().getUuid(), membersField, Membership.EFFECTIVE, true, true);
        assertEquals(1, ms10.getDepth());
        
        if (twoToSCEnabled) {
          Membership ms11 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(left.getUuid(), c.getUuid(), membersField, Membership.EFFECTIVE, true, true);
          assertEquals(2, ms11.getDepth());
        }
      }
    }
    
    Membership ms12 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(one.getUuid(), b.getUuid(), membersField, Membership.IMMEDIATE, true, true);
    assertEquals(0, ms12.getDepth());
    
    if (oneToTwoEnabled) {
      Membership ms13 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(one.getUuid(), two.toMember().getUuid(), membersField, Membership.IMMEDIATE, true, true);
      assertEquals(0, ms13.getDepth());
      
      if (twoToSCEnabled) {
        Membership ms14 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(one.getUuid(), c.getUuid(), membersField, Membership.EFFECTIVE, true, true);
        assertEquals(1, ms14.getDepth());
      }
    }
    
    if (twoToSCEnabled) {
      Membership ms15 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(two.getUuid(), c.getUuid(), membersField, Membership.IMMEDIATE, true, true);
      assertEquals(0, ms15.getDepth());
    }
    
    Membership ms16 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(owner.getUuid(), a.getUuid(), membersField, Membership.COMPOSITE, true, true);
    assertEquals(0, ms16.getDepth());
    
    if (leftToOneEnabled) {
      Membership ms17 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(owner.getUuid(), one.toMember().getUuid(), membersField, Membership.COMPOSITE, true, true);
      assertEquals(0, ms17.getDepth());
      
      Membership ms18 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(owner.getUuid(), b.getUuid(), membersField, Membership.COMPOSITE, true, true);
      assertEquals(0, ms18.getDepth());
      
      if (oneToTwoEnabled) {
        Membership ms19 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(owner.getUuid(), two.toMember().getUuid(), membersField, Membership.COMPOSITE, true, true);
        assertEquals(0, ms19.getDepth());
        
        if (twoToSCEnabled) {
          Membership ms20 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(owner.getUuid(), c.getUuid(), membersField, Membership.COMPOSITE, true, true);
          assertEquals(0, ms20.getDepth());
        }
      }
    }
    
    if (top2ToOwnerEnabled) {
      Membership ms21 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top2.getUuid(), owner.toMember().getUuid(), updatersField, Membership.IMMEDIATE, true, true);
      assertEquals(0, ms21.getDepth());
      
      Membership ms22 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top2.getUuid(), a.getUuid(), updatersField, Membership.EFFECTIVE, true, true);
      assertEquals(1, ms22.getDepth());
      
      if (leftToOneEnabled) {
        Membership ms23 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top2.getUuid(), one.toMember().getUuid(), updatersField, Membership.EFFECTIVE, true, true);
        assertEquals(1, ms23.getDepth());
  
        Membership ms24 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top2.getUuid(), b.getUuid(), updatersField, Membership.EFFECTIVE, true, true);
        assertEquals(1, ms24.getDepth());
        
        if (oneToTwoEnabled) {
          Membership ms25 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top2.getUuid(), two.toMember().getUuid(), updatersField, Membership.EFFECTIVE, true, true);
          assertEquals(1, ms25.getDepth());
         
          if (twoToSCEnabled) {
            Membership ms26 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(top2.getUuid(), c.getUuid(), updatersField, Membership.EFFECTIVE, true, true);
            assertEquals(1, ms26.getDepth());
          }
        }
      }
    }
    
    if (stemToOwnerEnabled) {
      Membership ms27 = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(stem.getUuid(), owner.toMember().getUuid(), creatorsField, Membership.IMMEDIATE, true, true);
      assertEquals(0, ms27.getDepth());
      
      Membership ms28 = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(stem.getUuid(), a.getUuid(), creatorsField, Membership.EFFECTIVE, true, true);
      assertEquals(1, ms28.getDepth());
      
      if (leftToOneEnabled) {
        Membership ms29 = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(stem.getUuid(), one.toMember().getUuid(), creatorsField, Membership.EFFECTIVE, true, true);
        assertEquals(1, ms29.getDepth());
        
        Membership ms30 = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(stem.getUuid(), b.getUuid(), creatorsField, Membership.EFFECTIVE, true, true);
        assertEquals(1, ms30.getDepth());
        
        if (oneToTwoEnabled) {
          Membership ms31 = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(stem.getUuid(), two.toMember().getUuid(), creatorsField, Membership.EFFECTIVE, true, true);
          assertEquals(1, ms31.getDepth());
          
          if (twoToSCEnabled) {
            Membership ms32 = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(stem.getUuid(), c.getUuid(), creatorsField, Membership.EFFECTIVE, true, true);
            assertEquals(1, ms32.getDepth());
          }
        }
      }
    }
  }

  /**
   * @throws Exception 
   */
  public void testAddGroupMemberWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");
    
    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    group.addMember(a);
    
    // now disable SA -> group
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    assertFalse(group.hasMember(a));
    
    // now add SA -> group
    group.addMember(a);
    assertTrue(group.hasMember(a));
  }
  
  /**
   * @throws Exception 
   */
  public void testDeleteGroupMemberWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");
    
    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    group.addMember(a);
    
    // now disable SA -> group
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    assertFalse(group.hasMember(a));
    
    // now delete SA -> group
    group.deleteMember(a);
    assertFalse(group.hasMember(a));
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, false, false);
    
    assertNull(ms);
  }
  
  /**
   * @throws Exception 
   */
  public void testAddStemMemberWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    
    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    top.grantPriv(group.toSubject(), NamingPrivilege.CREATE);

    // now disable group -> top (create priv)
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        top.getUuid(), group.toMember().getUuid(), FieldFinder.find("creators", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertFalse(top.hasCreate(group.toSubject()));
    
    // now add group -> top
    top.grantPriv(group.toSubject(), NamingPrivilege.CREATE);
    assertTrue(top.hasCreate(group.toSubject()));
  }
  
  /**
   * @throws Exception 
   */
  public void testDeleteStemMemberWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    
    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    top.grantPriv(group.toSubject(), NamingPrivilege.CREATE);

    // now disable group -> top (create priv)
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        top.getUuid(), group.toMember().getUuid(), FieldFinder.find("creators", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertFalse(top.hasCreate(group.toSubject()));
    
    // now delete group -> top
    top.revokePriv(group.toSubject(), NamingPrivilege.CREATE);
    assertFalse(top.hasCreate(group.toSubject()));
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        top.getUuid(), group.toMember().getUuid(), FieldFinder.find("creators", true), Membership.IMMEDIATE, false, false);
    
    assertNull(ms);
  }
  
  /**
   * @throws Exception
   */
  public void testGroupCanReadFieldWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");

    assertTrue(group.canReadField(a, Group.getDefaultList()));
    
    // now disable grouperAll -> group
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, SubjectFinder.findAllSubject(), true).getUuid(), 
        FieldFinder.find("readers", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertFalse(group.canReadField(a, Group.getDefaultList()));
  }
  
  /**
   * @throws Exception
   */
  public void testGroupCanWriteFieldWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    group.grantPriv(a, AccessPrivilege.ADMIN);

    assertTrue(group.canWriteField(a, Group.getDefaultList()));
    
    // now disable SA -> group
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("admins", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertFalse(group.canWriteField(a, Group.getDefaultList()));
  }
  

  /**
   * @throws Exception
   */
  public void testGroupDeleteWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group1 = top.addChildGroup("group", "group");
    Group group2 = top.addChildGroup("group2", "group2");
    group1.grantPriv(a, AccessPrivilege.ADMIN);
    group2.grantPriv(a, AccessPrivilege.ADMIN);
    
    // now disable SA -> group1
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group1.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("admins", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    GrouperSession.start(a);
    
    // should be able to delete group2
    group2.delete();
    
    // should not be able to delete group1
    try {
      group1.delete();
      fail ("should throw exception");
    } catch (InsufficientPrivilegeException e) {
      // this is good
    }
  }
  
  /**
   * @throws Exception
   */
  public void testGroupGetAdminsWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    group.grantPriv(a, AccessPrivilege.ADMIN);
    
    assertEquals(2, group.getAdmins().size());
    
    // now disable SA -> group
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("admins", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertEquals(1, group.getAdmins().size());
  }
  
  /**
   * @throws Exception
   */
  public void testGroupGetEffectiveMembersWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");
    Member member = MemberFinder.findBySubject(r.rs, a, true);

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group1 = top.addChildGroup("group1", "group1");
    Group group2 = top.addChildGroup("group2", "group2");
    group1.addMember(group2.toSubject());
    group1.grantPriv(group2.toSubject(), AccessPrivilege.ADMIN);
    group2.addMember(a);
    
    assertEquals(1, group1.getEffectiveMembers().size());
    assertEquals(1, group1.getEffectiveMemberships().size());
    assertEquals(1, group1.getEffectiveMembers(FieldFinder.find("admins", true)).size());
    assertEquals(1, group1.getEffectiveMemberships(FieldFinder.find("admins", true)).size());
    assertTrue(group1.hasEffectiveMember(a));
    assertTrue(group1.hasEffectiveMember(a, FieldFinder.find("admins", true)));
    assertTrue(member.isEffectiveMember(group1));
    
    // now disable SA -> group2
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group2.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertEquals(0, group1.getEffectiveMembers().size());
    assertEquals(0, group1.getEffectiveMemberships().size());
    assertEquals(0, group1.getEffectiveMembers(FieldFinder.find("admins", true)).size());
    assertEquals(0, group1.getEffectiveMemberships(FieldFinder.find("admins", true)).size());
    assertFalse(group1.hasEffectiveMember(a));
    assertFalse(group1.hasEffectiveMember(a, FieldFinder.find("admins", true)));
    assertFalse(member.isEffectiveMember(group1));
  }
  
  /**
   * @throws Exception
   */
  public void testGroupGetImmediateMembersWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");
    Member member = MemberFinder.findBySubject(r.rs, a, true);

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    group.addMember(a);
    group.grantPriv(a, AccessPrivilege.ADMIN);
    
    assertEquals(1, group.getImmediateMembers().size());
    assertEquals(1, group.getImmediateMemberships().size());
    assertEquals(2, group.getImmediateMembers(FieldFinder.find("admins", true)).size());
    assertEquals(2, group.getImmediateMemberships(FieldFinder.find("admins", true)).size());
    assertTrue(group.hasImmediateMember(a));
    assertTrue(group.hasImmediateMember(a, FieldFinder.find("admins", true)));
    assertTrue(member.isImmediateMember(group));
    
    // now disable SA -> group2
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("admins", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertEquals(0, group.getImmediateMembers().size());
    assertEquals(0, group.getImmediateMemberships().size());
    assertEquals(1, group.getImmediateMembers(FieldFinder.find("admins", true)).size());
    assertEquals(1, group.getImmediateMemberships(FieldFinder.find("admins", true)).size());
    assertFalse(group.hasImmediateMember(a));
    assertFalse(group.hasImmediateMember(a, FieldFinder.find("admins", true)));
    assertFalse(member.isImmediateMember(group));
  }
  

  /**
   * @throws Exception
   */
  public void testGroupGetMembersWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");
    Member member = MemberFinder.findBySubject(r.rs, a, true);

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    group.addMember(a);
    group.grantPriv(a, AccessPrivilege.ADMIN);
    
    assertEquals(1, group.getMembers().size());
    assertEquals(1, group.getMemberships().size());
    assertEquals(2, group.getMembers(FieldFinder.find("admins", true)).size());
    assertEquals(2, group.getMemberships(FieldFinder.find("admins", true)).size());
    assertTrue(group.hasMember(a));
    assertTrue(group.hasMember(a, FieldFinder.find("admins", true)));
    assertTrue(member.isMember(group));
    
    // now disable SA -> group2
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("admins", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertEquals(0, group.getMembers().size());
    assertEquals(0, group.getMemberships().size());
    assertEquals(1, group.getMembers(FieldFinder.find("admins", true)).size());
    assertEquals(1, group.getMemberships(FieldFinder.find("admins", true)).size());
    assertFalse(group.hasMember(a));
    assertFalse(group.hasMember(a, FieldFinder.find("admins", true)));
    assertFalse(member.isMember(group));
  }
  
  /**
   * @throws Exception
   */
  public void testGroupGetPrivsWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    group.grantPriv(a, AccessPrivilege.ADMIN);
    
    assertEquals(3, group.getPrivs(a).size());
    
    // now disable SA -> group
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("admins", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertEquals(2, group.getPrivs(a).size());
  }

  /**
   * @throws Exception
   */
  public void testGroupHasAdminWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    group.grantPriv(a, AccessPrivilege.ADMIN);
    
    assertTrue(group.hasAdmin(a));
    
    // now disable SA -> group
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("admins", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertFalse(group.hasAdmin(a));
  }

  /**
   * @throws Exception
   */
  public void testStemDeleteWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top1 = root.addChildStem("top1", "top1");
    Stem top2 = root.addChildStem("top2", "top2");
    top1.grantPriv(a, NamingPrivilege.STEM);
    top2.grantPriv(a, NamingPrivilege.STEM);
    
    // now disable SA -> top1
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        top1.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("stemmers", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    GrouperSession.start(a);
    
    // should be able to delete top2
    top2.delete();
    
    // should not be able to delete top1
    try {
      top1.delete();
      fail ("should throw exception");
    } catch (InsufficientPrivilegeException e) {
      // this is good
    }
  }

  /**
   * @throws Exception
   */
  public void testStemGetChildMembershipGroupsWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");
    GrouperSession nrs;

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Set<Privilege> privileges = new LinkedHashSet<Privilege>();
    privileges.add(AccessPrivilege.ADMIN);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    Group group = top.addChildGroup("group", "group");
    group.addMember(a);
    group.grantPriv(a, AccessPrivilege.ADMIN);
    
    nrs = GrouperSession.start(a);
    assertEquals(1, top.getChildMembershipGroups(Scope.ONE, privileges, null).size());
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    
    // now disable SA -> group (default list)
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        Group.getDefaultList(), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    nrs = GrouperSession.start(a);
    assertEquals(0, top.getChildMembershipGroups(Scope.ONE, privileges, null).size());
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    
    // enable it again
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        Group.getDefaultList(), Membership.IMMEDIATE, true, false);
    ms.setEnabled(true);
    ms.setEnabledTime(null);
    ms.setDisabledTime(null);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    nrs = GrouperSession.start(a);
    assertEquals(1, top.getChildMembershipGroups(Scope.ONE, privileges, null).size());
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    
    // now disable SA -> group (admin priv)
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("admins", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);    
    
    nrs = GrouperSession.start(a);
    assertEquals(0, top.getChildMembershipGroups(Scope.ONE, privileges, null).size());
    nrs.stop();
    nrs = GrouperSession.startRootSession();
  }
  
  /**
   * @throws Exception
   */
  public void testStemGetPrivsWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    top.grantPriv(a, NamingPrivilege.CREATE);
    
    assertEquals(1, top.getPrivs(a).size());
    
    // now disable SA -> top
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        top.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("creators", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertEquals(0, top.getPrivs(a).size());
  }
  
  /**
   * @throws Exception
   */
  public void testStemGetCreatorsWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    top.grantPriv(a, NamingPrivilege.CREATE);
    
    assertEquals(1, top.getCreators().size());
    
    // now disable SA -> top
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        top.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("creators", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertEquals(0, top.getCreators().size());
  }
  
  /**
   * @throws Exception
   */
  public void testStemHasCreateWhenDisabled() throws Exception {
    
    r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");

    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Stem root = StemFinder.findRootStem(r.rs);
    Stem top = root.addChildStem("top", "top");
    top.grantPriv(a, NamingPrivilege.CREATE);
    
    assertTrue(top.hasCreate(a));
    
    // now disable SA -> top
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
        top.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), 
        FieldFinder.find("creators", true), Membership.IMMEDIATE, true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperCacheUtils.clearAllCaches();
    
    assertFalse(top.hasCreate(a));
  }
}

