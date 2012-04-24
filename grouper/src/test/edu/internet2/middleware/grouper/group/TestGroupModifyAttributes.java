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

package edu.internet2.middleware.grouper.group;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupModifyAttributes.java,v 1.7 2009-12-07 07:31:09 mchyzer Exp $
 * @since   1.2.0
 */
public class TestGroupModifyAttributes extends GrouperTest {

  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new TestGroupModifyAttributes("testGroupModifyAttributesAfterDisablingMembership"));
    TestRunner.run(TestGroupModifyAttributes.class);
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestGroupModifyAttributes.class);

  public TestGroupModifyAttributes(String name) {
    super(name);
  }

  // MEMBERSHIPS //

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterAddingImmediateMember() {

    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    LOG.info("testGroupModifyAttributesUpdatedAfterAddingImmediateMember");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");

      GrouperUtil.sleep(50);
      
      long    orig  = gA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(50);

      gA.addMember(subjA);
      long    post  = new java.util.Date().getTime();
      gA = GroupFinder.findByUuid(r.rs, gA.getUuid(), true,new QueryOptions().secondLevelCache(false));
      assertTrue( "gA modify time not updated: " + gA.getModifyTime().getTime() 
          + ", " + orig, gA.getModifyTime().getTime() == orig );
      assertTrue( "gA last membership time >= pre",    gA.getLastMembershipChange().getTime() >= pre );
      assertTrue( "gA last membership time <= post",   gA.getLastMembershipChange().getTime() <= post );

      assertTrue( "gA last immediate membership time >= pre",    gA.getLastImmediateMembershipChange().getTime() >= pre );
      assertTrue( "gA last immediate membership time <= post",   gA.getLastImmediateMembershipChange().getTime() <= post );
      
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupModifyAttributesUpdatedAfterAddingImmediateMember()

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterDeletingImmediateMember() {
    LOG.info("testGroupModifyAttributesUpdatedAfterDeletingImmediateMember");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      gA.addMember(subjA);

      long    orig  = gA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      Thread.sleep(100); // TODO 20070430 hack
      gA.deleteMember(subjA);
      Thread.sleep(100); // TODO 20070430 hack
      long    post  = new java.util.Date().getTime();
      gA = GroupFinder.findByUuid(r.rs, gA.getUuid(), true,new QueryOptions().secondLevelCache(false));
      long    mtime = gA.getModifyTime().getTime();
      long    mtime_mem = gA.getLastMembershipChange().getTime();
      long    mtime_imm_mem = gA.getLastImmediateMembershipChange().getTime();

      assertTrue( "gA modify time not updated (" + mtime + " == " + orig + ")", mtime == orig );
      assertTrue( "gA last membership time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "gA last membership time <= post (" + mtime_mem + " <= " + post + ")", mtime_mem <= post );

      assertTrue( "gA last immediate membership time >= pre (" + mtime_imm_mem + " >= " + pre + ")", mtime_imm_mem >= pre );
      assertTrue( "gA last immediate membership time <= post (" + mtime_imm_mem + " <= " + post + ")", mtime_imm_mem <= post );
      
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterAddingEffectiveMember() {
    LOG.info("testGroupModifyAttributesUpdatedAfterAddingEffectiveMember");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");

      gA.addMember( gB.toSubject() );
      GrouperUtil.sleep(100);

      long  orig  = gA.getModifyTime().getTime();
      long  pre   = new java.util.Date().getTime();
      
      GrouperUtil.sleep(100);

      gB.addMember(subjA);
      long  post  = new java.util.Date().getTime();

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g = GroupFinder.findByUuid( s, gA.getUuid(), true,new QueryOptions().secondLevelCache(false) );
      assertTrue( "gA modifyTime == orig",  g.getModifyTime().getTime() == orig );
      assertTrue( "gA getLastMembershipChange >= pre", g.getLastMembershipChange().getTime() >= pre );
      assertTrue( "gA getLastMembershipChange <= post", g.getLastMembershipChange().getTime() <= post );
      assertTrue( "gA getLastImmediateMembershipChange <= pre", g.getLastImmediateMembershipChange().getTime() <= pre );

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupModifyAttributesUpdatedAfterAddingEffectiveMember()

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterDeletingEffectiveMember() {
    LOG.info("testGroupModifyAttributesUpdatedAfterDeletingEffectiveMember");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
      gA.addMember( gB.toSubject() );
      gB.addMember(subjA);
      GrouperUtil.sleep(100);

      long  orig  = gA.getModifyTime().getTime();
      long  pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);

      gB.deleteMember(subjA);
      long  post  = new java.util.Date().getTime();

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g = GroupFinder.findByUuid( s, gA.getUuid(), true,new QueryOptions().secondLevelCache(false) );
      assertTrue( "gA modifyTime == orig",  g.getModifyTime().getTime() == orig );
      assertTrue( "gA getLastMembershipChange >= pre", g.getLastMembershipChange().getTime() >= pre );
      assertTrue( "gA getLastMembershipChange <= post", g.getLastMembershipChange().getTime() <= post );
      assertTrue( "gA getLastImmediateMembershipChange <= pre", g.getLastImmediateMembershipChange().getTime() <= pre );

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupModifyAttributesUpdatedAfterDeletingEffectiveMember()


  // COMPOSITES //

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterUpdatingComplement() {
    LOG.info("testGroupModifyAttributesUpdatedAfterAddingImmediateMember");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");

      GrouperUtil.sleep(50);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(50);
      
      gA.addMember(subjA);
      gB.addMember(subjB);
      gC.addCompositeMember(CompositeType.COMPLEMENT, gA, gB);

      long    pre2   = new java.util.Date().getTime();
      GrouperUtil.sleep(50);

      gA.addMember(subjC);
      long    post  = new java.util.Date().getTime();

      // load group in new session so we don't (potentially) get stale data
      GrouperSession  s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group           g     = GroupFinder.findByUuid( s, gC.getUuid(), true,new QueryOptions().secondLevelCache(false) );

      assertTrue( "gC modifyTime <= pre2",  g.getModifyTime().getTime() <= pre2 );
      assertTrue( "gC getLastMembershipChange >= pre2", g.getLastMembershipChange().getTime() >= pre2 );
      assertTrue( "gC getLastMembershipChange <= post", g.getLastMembershipChange().getTime() <= post );
      assertTrue( "gC getLastImmediateMembershipChange <= pre", g.getLastImmediateMembershipChange().getTime() <= pre );

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 

  // ACCESS PRIVS //

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterGrantingImmediatePriv() {
    LOG.info("testGroupModifyAttributesUpdatedAfterGrantingImmediatePriv");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");

      long    orig  = gA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(50);

      gA.grantPriv(subjA, AccessPrivilege.ADMIN);
      long    post  = new java.util.Date().getTime();
      gA = GroupFinder.findByUuid(r.rs, gA.getUuid(), true,new QueryOptions().secondLevelCache(false));

      long    mtime = gA.getModifyTime().getTime();
      long    mtime_mem = gA.getLastMembershipChange().getTime();
      long    mtime_imm_mem = gA.getLastImmediateMembershipChange().getTime();

      assertTrue( "gA modify time not updated (" + mtime + " == " + orig + ")", mtime == orig );
      assertTrue( "gA last membership time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "gA last membership time <= post (" + mtime_mem + " <= " + post + ")", mtime_mem <= post );

      assertTrue( "gA last immediate membership time >= pre (" + mtime_imm_mem + " >= " + pre + ")", mtime_imm_mem >= pre );
      assertTrue( "gA last immediate membership time <= post (" + mtime_imm_mem + " <= " + post + ")", mtime_imm_mem <= post );
      
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterRevokingImmediatePriv() {
    LOG.info("testGroupModifyAttributesUpdatedAfterRevokingImmediatePriv");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      gA.grantPriv(subjA, AccessPrivilege.ADMIN);

      long    orig  = gA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(50);

      gA.revokePriv(subjA, AccessPrivilege.ADMIN);
      long    post  = new java.util.Date().getTime();
      gA = GroupFinder.findByUuid(r.rs, gA.getUuid(), true,new QueryOptions().secondLevelCache(false));

      long    mtime = gA.getModifyTime().getTime();
      long    mtime_mem = gA.getLastMembershipChange().getTime();
      long    mtime_imm_mem = gA.getLastImmediateMembershipChange().getTime();

      assertTrue( "gA modify time not updated (" + mtime + " == " + orig + ")", mtime == orig );
      assertTrue( "gA last membership time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "gA last membership time <= post (" + mtime_mem + " <= " + post + ")", mtime_mem <= post );
      
      assertTrue( "gA last immediate membership time >= pre (" + mtime_imm_mem + " >= " + pre + ")", mtime_imm_mem >= pre );
      assertTrue( "gA last immediate membership time <= post (" + mtime_imm_mem + " <= " + post + ")", mtime_imm_mem <= post );
      
      r.rs.stop();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  } // public void testGroupModifyAttributesUpdatedAfterRevokingImmediatePriv()

  // @since   1.2.0
  public void testGroupModifyAttributesNotUpdatedAfterGrantingEffectivePriv() {
    LOG.info("testGroupModifyAttributesNotUpdatedAfterGrantingEffectivePriv");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
      gA.addMember( gB.toSubject() );
      GrouperUtil.sleep(100);

      long pre = new java.util.Date().getTime();
      

      gB.grantPriv(subjA, AccessPrivilege.ADMIN);
      GrouperUtil.sleep(100);

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g     = GroupFinder.findByUuid( s, gA.getUuid(), true,new QueryOptions().secondLevelCache(false) );
      long  mtime = g.getModifyTime().getTime();
      long  mtime_mem = g.getLastMembershipChange().getTime();
      long  mtime_imm_mem = g.getLastImmediateMembershipChange().getTime();

      assertTrue( "gA modifyTime <= pre (" + mtime + " <= " + pre + ")",  mtime <= pre );
      assertTrue( "gA last membership time < pre (" + mtime_mem + " < " + pre + ")", mtime_mem < pre );
      assertTrue( "gA last immediate membership time < pre (" + mtime_imm_mem + " < " + pre + ")", mtime_imm_mem < pre );
      
      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupModifyAttributesNotUpdatedAfterAddingGrantingEffectivePriv()

  // @since   1.2.0
  public void testGroupModifyAttributesNotUpdatedAfterRevokingEffectivePriv() {
    LOG.info("testGroupModifyAttributesNotUpdatedAfterRevokingEffectivePriv");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
      gA.addMember( gB.toSubject() );
      gB.grantPriv(subjA, AccessPrivilege.ADMIN);

      GrouperUtil.sleep(100);

      long pre = new java.util.Date().getTime();

      
      gB.revokePriv(subjA, AccessPrivilege.ADMIN);
      GrouperUtil.sleep(100);

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g = GroupFinder.findByUuid( s, gA.getUuid(), true ,new QueryOptions().secondLevelCache(false));
      long  mtime_mem = g.getLastMembershipChange().getTime();
      long  mtime_imm_mem = g.getLastImmediateMembershipChange().getTime();

      assertTrue( "gA modifyTime < pre",  g.getModifyTime().getTime() < pre );
      assertTrue( "gA last membership time < pre (" + mtime_mem + " < " + pre + ")", mtime_mem < pre );
      assertTrue( "gA last immediate membership time < pre (" + mtime_imm_mem + " < " + pre + ")", mtime_imm_mem < pre );
      
      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupModifyAttributesNotUpdatedAfterRevokingEffectivePriv()

  public void testGroupModifyAttributesAfterGrantingEffectivePriv() {
    LOG.info("testGroupModifyAttributesAfterGrantingEffectivePriv");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
      gA.grantPriv(gB.toSubject(), AccessPrivilege.ADMIN);
      GrouperUtil.sleep(100);

      long pre = new java.util.Date().getTime();
      GrouperUtil.sleep(100);

      gB.addMember(subjA);
      GrouperUtil.sleep(100);

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g     = GroupFinder.findByUuid( s, gA.getUuid(), true,new QueryOptions().secondLevelCache(false) );
      long  mtime = g.getModifyTime().getTime();
      long  mtime_mem = g.getLastMembershipChange().getTime();
      long  mtime_imm_mem = g.getLastImmediateMembershipChange().getTime();

      assertTrue( "gA modifyTime <= pre (" + mtime + " <= " + pre + ")",  mtime <= pre );
      assertTrue( "gA last membership time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "gA last immediate membership time <= pre (" + mtime_imm_mem + " <= " + pre + ")", mtime_imm_mem <= pre );
      
      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }
  
  public void testGroupModifyAttributesAfterRevokingEffectivePriv() {
    LOG.info("testGroupModifyAttributesAfterRevokingEffectivePriv");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
      gA.grantPriv(gB.toSubject(), AccessPrivilege.ADMIN);
      gB.addMember(subjA);

      GrouperUtil.sleep(100);

      long pre = new java.util.Date().getTime();
      GrouperUtil.sleep(100);
      
      gB.deleteMember(subjA);
      GrouperUtil.sleep(100);

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g = GroupFinder.findByUuid( s, gA.getUuid(), true,new QueryOptions().secondLevelCache(false) );
      long  mtime_mem = g.getLastMembershipChange().getTime();
      long  mtime_imm_mem = g.getLastImmediateMembershipChange().getTime();

      assertTrue( "gA modifyTime < pre",  g.getModifyTime().getTime() < pre );
      assertTrue( "gA last membership time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "gA last immediate membership time <= pre (" + mtime_imm_mem + " <= " + pre + ")", mtime_imm_mem <= pre );
      
      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }
  
  public void testGroupModifyAttributesAfterUpdatingAttributes() {
    LOG.info("testGroupModifyAttributesAfterUpdatingAttributes");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");

      GrouperUtil.sleep(100);
      long pre = new java.util.Date().getTime();
      GrouperUtil.sleep(100);

      gA.setDescription("test");
      gA.store();

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g = GroupFinder.findByUuid( s, gA.getUuid(), true,new QueryOptions().secondLevelCache(false) );
      long  mtime_mem = g.getLastMembershipChange().getTime();
      long  mtime_imm_mem = g.getLastImmediateMembershipChange().getTime();

      assertTrue( "gA modifyTime > pre",  g.getModifyTime().getTime() > pre );
      assertTrue( "gA last membership time < pre (" + mtime_mem + " < " + pre + ")", mtime_mem < pre );
      assertTrue( "gA last immediate membership time < pre (" + mtime_imm_mem + " < " + pre + ")", mtime_imm_mem < pre );
      
      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }
  
  public void testGroupModifyAttributesAfterAddingComplementWithNoMembers() {
    LOG.info("testGroupModifyAttributesAfterAddingComplementWithNoMembers");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");

      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);
      
      gC.addCompositeMember(CompositeType.COMPLEMENT, gA, gB);


      // load group in new session so we don't (potentially) get stale data
      GrouperSession  s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group           g     = GroupFinder.findByUuid( s, gC.getUuid(), true,new QueryOptions().secondLevelCache(false) );

      assertTrue( "gC modifyTime < pre",  g.getModifyTime().getTime() < pre );
      assertTrue( "gC getLastMembershipChange < pre", g.getLastMembershipChange().getTime() < pre );
      assertTrue( "gC getLastImmediateMembershipChange < pre", g.getLastImmediateMembershipChange().getTime() < pre );

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testGroupModifyAttributesAfterAddingComplementWithMembers() {
    LOG.info("testGroupModifyAttributesAfterAddingComplementWithMembers");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");

      gA.addMember(subjA);

      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);
      
      gC.addCompositeMember(CompositeType.COMPLEMENT, gA, gB);


      // load group in new session so we don't (potentially) get stale data
      GrouperSession  s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group           g     = GroupFinder.findByUuid( s, gC.getUuid(), true,new QueryOptions().secondLevelCache(false) );

      assertTrue( "gC modifyTime < pre",  g.getModifyTime().getTime() < pre );
      assertTrue( "gC getLastMembershipChange > pre", g.getLastMembershipChange().getTime() > pre );
      assertTrue( "gC getLastImmediateMembershipChange < pre", g.getLastImmediateMembershipChange().getTime() < pre );

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testGroupModifyAttributesAfterDeletingComplementWithNoMembers() {
    LOG.info("testGroupModifyAttributesAfterDeletingComplementWithNoMembers");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      gC.addCompositeMember(CompositeType.COMPLEMENT, gA, gB);


      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);
      
      gC.deleteCompositeMember();

      // load group in new session so we don't (potentially) get stale data
      GrouperSession  s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group           g     = GroupFinder.findByUuid( s, gC.getUuid(), true,new QueryOptions().secondLevelCache(false) );

      assertTrue( "gC modifyTime < pre",  g.getModifyTime().getTime() < pre );
      assertTrue( "gC getLastMembershipChange < pre", g.getLastMembershipChange().getTime() < pre );
      assertTrue( "gC getLastImmediateMembershipChange < pre", g.getLastImmediateMembershipChange().getTime() < pre );

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testGroupModifyAttributesAfterDeletingComplementWithMembers() {
    LOG.info("testGroupModifyAttributesAfterDeletingComplementWithMembers");
    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      gC.addCompositeMember(CompositeType.COMPLEMENT, gA, gB);
      gA.addMember(subjA);

      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);
      
      gC.deleteCompositeMember();

      // load group in new session so we don't (potentially) get stale data
      GrouperSession  s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group           g     = GroupFinder.findByUuid( s, gC.getUuid(), true,new QueryOptions().secondLevelCache(false) );

      assertTrue( "gC modifyTime < pre",  g.getModifyTime().getTime() < pre );
      assertTrue( "gC getLastMembershipChange > pre", g.getLastMembershipChange().getTime() > pre );
      assertTrue( "gC getLastImmediateMembershipChange < pre", g.getLastImmediateMembershipChange().getTime() < pre );

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 

  public void testGroupModifyAttributesAfterDisablingMembership() {    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 5, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Group   gD    = r.getGroup("a", "d");
      Group   gE    = r.getGroup("a", "e");

      gC.addMember(gD.toSubject());
      gD.addMember(gE.toSubject());
      gA.addMember(gC.toSubject());
      gB.grantPriv(gC.toSubject(), AccessPrivilege.ADMIN);

      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);
      
      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
          gC.getUuid(), gD.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
      ms.setEnabled(false);
      GrouperDAOFactory.getFactory().getMembership().update(ms);

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      gA = GroupFinder.findByUuid(s, gA.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gB = GroupFinder.findByUuid(s, gB.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gC = GroupFinder.findByUuid(s, gC.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gD = GroupFinder.findByUuid(s, gD.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gE = GroupFinder.findByUuid(s, gE.getUuid(), true, new QueryOptions().secondLevelCache(false));

      assertTrue("gA getLastMembershipChange > pre", gA.getLastMembershipChange().getTime() > pre);
      assertTrue("gB getLastMembershipChange > pre", gB.getLastMembershipChange().getTime() > pre);
      assertTrue("gC getLastMembershipChange > pre", gC.getLastMembershipChange().getTime() > pre);
      assertTrue("gD getLastMembershipChange < pre", gD.getLastMembershipChange().getTime() < pre);
      assertTrue("gE getLastMembershipChange < pre", gE.getLastMembershipChange().getTime() < pre);
      
      assertTrue("gA getLastImmediateMembershipChange < pre", gA.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gB getLastImmediateMembershipChange < pre", gB.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gC getLastImmediateMembershipChange > pre", gC.getLastImmediateMembershipChange().getTime() > pre);
      assertTrue("gD getLastImmediateMembershipChange < pre", gD.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gE getLastImmediateMembershipChange < pre", gE.getLastImmediateMembershipChange().getTime() < pre);

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testGroupModifyAttributesAfterDisablingMembership2() {    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 5, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Group   gD    = r.getGroup("a", "d");
      Group   gE    = r.getGroup("a", "e");

      gC.addMember(gD.toSubject());
      gC.grantPriv(gD.toSubject(), AccessPrivilege.ADMIN);
      gD.addMember(gE.toSubject());
      gA.addMember(gC.toSubject());
      gB.grantPriv(gC.toSubject(), AccessPrivilege.ADMIN);

      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);
      
      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
          gC.getUuid(), gD.toMember().getUuid(), FieldFinder.find(Field.FIELD_NAME_ADMINS, true), MembershipType.IMMEDIATE.getTypeString(), true, true);
      ms.setEnabled(false);
      GrouperDAOFactory.getFactory().getMembership().update(ms);

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      gA = GroupFinder.findByUuid(s, gA.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gB = GroupFinder.findByUuid(s, gB.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gC = GroupFinder.findByUuid(s, gC.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gD = GroupFinder.findByUuid(s, gD.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gE = GroupFinder.findByUuid(s, gE.getUuid(), true, new QueryOptions().secondLevelCache(false));

      assertTrue("gA getLastMembershipChange < pre", gA.getLastMembershipChange().getTime() < pre);
      assertTrue("gB getLastMembershipChange < pre", gB.getLastMembershipChange().getTime() < pre);
      assertTrue("gC getLastMembershipChange > pre", gC.getLastMembershipChange().getTime() > pre);
      assertTrue("gD getLastMembershipChange < pre", gD.getLastMembershipChange().getTime() < pre);
      assertTrue("gE getLastMembershipChange < pre", gE.getLastMembershipChange().getTime() < pre);
      
      assertTrue("gA getLastImmediateMembershipChange < pre", gA.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gB getLastImmediateMembershipChange < pre", gB.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gC getLastImmediateMembershipChange > pre", gC.getLastImmediateMembershipChange().getTime() > pre);
      assertTrue("gD getLastImmediateMembershipChange < pre", gD.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gE getLastImmediateMembershipChange < pre", gE.getLastImmediateMembershipChange().getTime() < pre);

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testGroupModifyAttributesAfterEnablingMembership() {    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 5, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Group   gD    = r.getGroup("a", "d");
      Group   gE    = r.getGroup("a", "e");

      gC.addMember(gD.toSubject());
      gD.addMember(gE.toSubject());
      gA.addMember(gC.toSubject());
      gB.grantPriv(gC.toSubject(), AccessPrivilege.ADMIN);
      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
          gC.getUuid(), gD.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
      ms.setEnabled(false);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);

      ms.setEnabled(true);
      GrouperDAOFactory.getFactory().getMembership().update(ms);

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      gA = GroupFinder.findByUuid(s, gA.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gB = GroupFinder.findByUuid(s, gB.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gC = GroupFinder.findByUuid(s, gC.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gD = GroupFinder.findByUuid(s, gD.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gE = GroupFinder.findByUuid(s, gE.getUuid(), true, new QueryOptions().secondLevelCache(false));

      assertTrue("gA getLastMembershipChange > pre", gA.getLastMembershipChange().getTime() > pre);
      assertTrue("gB getLastMembershipChange > pre", gB.getLastMembershipChange().getTime() > pre);
      assertTrue("gC getLastMembershipChange > pre", gC.getLastMembershipChange().getTime() > pre);
      assertTrue("gD getLastMembershipChange < pre", gD.getLastMembershipChange().getTime() < pre);
      assertTrue("gE getLastMembershipChange < pre", gE.getLastMembershipChange().getTime() < pre);
      
      assertTrue("gA getLastImmediateMembershipChange < pre", gA.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gB getLastImmediateMembershipChange < pre", gB.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gC getLastImmediateMembershipChange > pre", gC.getLastImmediateMembershipChange().getTime() > pre);
      assertTrue("gD getLastImmediateMembershipChange < pre", gD.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gE getLastImmediateMembershipChange < pre", gE.getLastImmediateMembershipChange().getTime() < pre);

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testGroupModifyAttributesAfterEnablingMembership2() {    
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 5, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Group   gD    = r.getGroup("a", "d");
      Group   gE    = r.getGroup("a", "e");

      gC.addMember(gD.toSubject());
      gC.grantPriv(gD.toSubject(), AccessPrivilege.ADMIN);
      gD.addMember(gE.toSubject());
      gA.addMember(gC.toSubject());
      gB.grantPriv(gC.toSubject(), AccessPrivilege.ADMIN);

      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
          gC.getUuid(), gD.toMember().getUuid(), FieldFinder.find(Field.FIELD_NAME_ADMINS, true), MembershipType.IMMEDIATE.getTypeString(), true, true);
      ms.setEnabled(false);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);

      ms.setEnabled(true);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      gA = GroupFinder.findByUuid(s, gA.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gB = GroupFinder.findByUuid(s, gB.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gC = GroupFinder.findByUuid(s, gC.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gD = GroupFinder.findByUuid(s, gD.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gE = GroupFinder.findByUuid(s, gE.getUuid(), true, new QueryOptions().secondLevelCache(false));

      assertTrue("gA getLastMembershipChange < pre", gA.getLastMembershipChange().getTime() < pre);
      assertTrue("gB getLastMembershipChange < pre", gB.getLastMembershipChange().getTime() < pre);
      assertTrue("gC getLastMembershipChange > pre", gC.getLastMembershipChange().getTime() > pre);
      assertTrue("gD getLastMembershipChange < pre", gD.getLastMembershipChange().getTime() < pre);
      assertTrue("gE getLastMembershipChange < pre", gE.getLastMembershipChange().getTime() < pre);

      assertTrue("gA getLastImmediateMembershipChange < pre", gA.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gB getLastImmediateMembershipChange < pre", gB.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gC getLastImmediateMembershipChange > pre", gC.getLastImmediateMembershipChange().getTime() > pre);
      assertTrue("gD getLastImmediateMembershipChange < pre", gD.getLastImmediateMembershipChange().getTime() < pre);
      assertTrue("gE getLastImmediateMembershipChange < pre", gE.getLastImmediateMembershipChange().getTime() < pre);
      
      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
} // public class TestGroup43 extends GrouperTest

