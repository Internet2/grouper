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
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import  edu.internet2.middleware.subject.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup43.java,v 1.14 2008-08-27 01:31:20 mchyzer Exp $
 * @since   1.2.0
 */
public class TestGroup43 extends GrouperTest {

  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGroup43("testGroupModifyAttributesUpdatedAfterAddingImmediateMember"));
  }
  
  private static final Log LOG = LogFactory.getLog(TestGroup43.class);

  public TestGroup43(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // MEMBERSHIPS //

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterAddingImmediateMember() {
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
      assertTrue( "gA modify time updated: " + gA.getModifyTime().getTime() 
          + ", " + orig, gA.getModifyTime().getTime() > orig );
      assertTrue( "gA modifyTime >= pre",    gA.getModifyTime().getTime() >= pre );
      assertTrue( "gA modifyTime <= post",   gA.getModifyTime().getTime() <= post );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupModifyAttributesUpdatedAfterAddingImmediateMember()

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterDeletingImmediateMember() {
    LOG.info("testGroupModifyAttributesUpdatedAfterDeletingImmediateMember");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      gA.addMember(subjA);

      long    orig  = gA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      Thread.sleep(1); // TODO 20070430 hack
      gA.deleteMember(subjA);
      Thread.sleep(1); // TODO 20070430 hack
      long    post  = new java.util.Date().getTime();
      long    mtime = gA.getModifyTime().getTime();
      assertTrue( "gA modify time updated (" + mtime + " >= " + orig + ")", mtime >= orig );
      assertTrue( "gA modify time >= pre (" + mtime + " >= " + pre + ")", mtime >= pre );
      assertTrue( "gA modify time <= post (" + mtime + " <= " + post + ")", mtime <= post );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterAddingEffectiveMember() {
    LOG.info("testGroupModifyAttributesUpdatedAfterAddingEffectiveMember");
    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");

      gA.addMember( gB.toSubject() );
      long  pre   = new java.util.Date().getTime();
      
      GrouperUtil.sleep(50);

      gB.addMember(subjA);
      long  post  = new java.util.Date().getTime();

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g = GroupFinder.findByUuid( s, gA.getUuid() );
      assertTrue( "gA modifyTime >= pre",  g.getModifyTime().getTime() >= pre );
      assertTrue( "gA modifyTime <= post", g.getModifyTime().getTime() <= post );

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
    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
      gA.addMember( gB.toSubject() );
      gB.addMember(subjA);

      long  pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(50);

      gB.deleteMember(subjA);
      long  post  = new java.util.Date().getTime();

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g = GroupFinder.findByUuid( s, gA.getUuid() );
      assertTrue( "gA modifyTime >= pre",  g.getModifyTime().getTime() >= pre );
      assertTrue( "gA modifyTime <= post", g.getModifyTime().getTime() <= post );

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
    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");

      gA.addMember(subjA);
      gB.addMember(subjB);
      gC.addCompositeMember(CompositeType.COMPLEMENT, gA, gB);

      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(50);

      gA.addMember(subjC);
      long    post  = new java.util.Date().getTime();

      // load group in new session so we don't (potentially) get stale data
      GrouperSession  s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group           g     = GroupFinder.findByUuid( s, gC.getUuid() );
      long            mtime = g.getModifyTime().getTime();

      assertTrue( "gC modifyTime >= pre",  mtime >= pre );
      assertTrue( "gC modifyTime <= post", mtime <= post );

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
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");

      long    orig  = gA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(50);

      gA.grantPriv(subjA, AccessPrivilege.ADMIN);
      long    post  = new java.util.Date().getTime();
      long    mtime = gA.getModifyTime().getTime();
      assertTrue( "gA modify time updated (" + mtime + " >= " + orig + ")", mtime >= orig );
      assertTrue( "gA modify time >= pre (" + mtime + " >= " + pre + ")", mtime >= pre );
      assertTrue( "gA modify time <= post (" + mtime + " <= " + post + ")", mtime <= post );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 

  // @since   1.2.0
  public void testGroupModifyAttributesUpdatedAfterRevokingImmediatePriv() {
    LOG.info("testGroupModifyAttributesUpdatedAfterRevokingImmediatePriv");
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
      long    mtime = gA.getModifyTime().getTime();
      assertTrue( "gA modify time updated (" + mtime + " >= " + orig + ")", mtime >= orig );
      assertTrue( "gA modify time >= pre (" + mtime + " >= " + pre + ")", mtime >= pre );
      assertTrue( "gA modify time <= post (" + mtime + " <= " + post + ")", mtime <= post );
      r.rs.stop();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  } // public void testGroupModifyAttributesUpdatedAfterRevokingImmediatePriv()

  // @since   1.2.0
  public void testGroupModifyAttributesNotUpdatedAfterGrantingEffectivePriv() {
    LOG.info("testGroupModifyAttributesNotUpdatedAfterGrantingEffectivePriv");
    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
      gA.addMember( gB.toSubject() );

      long pre = new java.util.Date().getTime();
      
      GrouperUtil.sleep(50);

      gB.grantPriv(subjA, AccessPrivilege.ADMIN);

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g     = GroupFinder.findByUuid( s, gA.getUuid() );
      long  mtime = g.getModifyTime().getTime();
      assertTrue( "gA modifyTime <= pre (" + mtime + " <= " + pre + ")",  mtime <= pre );

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
    try {
      R       r     = R.populateRegistry(1, 2, 1);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Subject subjA = r.getSubject("a");
      gA.addMember( gB.toSubject() );
      gB.grantPriv(subjA, AccessPrivilege.ADMIN);

      GrouperUtil.sleep(50);

      long pre = new java.util.Date().getTime();
      
      
      gB.revokePriv(subjA, AccessPrivilege.ADMIN);

      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g = GroupFinder.findByUuid( s, gA.getUuid() );
      assertTrue( "gA modifyTime < pre",  g.getModifyTime().getTime() < pre );

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGroupModifyAttributesNotUpdatedAfterRevokingEffectivePriv()


} // public class TestGroup43 extends GrouperTest

