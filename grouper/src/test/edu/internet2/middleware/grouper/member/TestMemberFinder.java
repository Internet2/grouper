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

package edu.internet2.middleware.grouper.member;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  tzeller
 * @version 
 * @since   1.3.0
 */
public class TestMemberFinder extends GrouperTest {
  
  private static final Log LOG = GrouperUtil.getLog(TestMemberFinder.class);
  
  public TestMemberFinder(String name) {
    super(name);
  }
  // TESTS //
  
  public void testFindAll() {
    LOG.info("testFindAll");
    try {      
      R r = R.populateRegistry(1, 3, 2);      
      Group gA = r.getGroup("a", "a");
      Group gB = r.getGroup("a", "b");      
      Subject subjA = SubjectFinder.findById("a", true);
      
      gA.addMember(gB.toSubject());
      gB.addMember(subjA);

      Set members = MemberFinder.findAll(GrouperSession.start(SubjectFinder
          .findRootSubject()));
      assertTrue("OK: found 6 members", members.size() == 6);
      
      Source gsaSource = SubjectFinder.getSource("g:gsa");
      Set gsaMembers = MemberFinder.findAll(GrouperSession.start(SubjectFinder
          .findRootSubject()), gsaSource);
      assertTrue("OK: gsa source has 2 members", gsaMembers.size() == 3);
      
      Source isaSource = SubjectFinder.getSource("g:isa");
      Set isaMembers = MemberFinder.findAll(GrouperSession.start(SubjectFinder
          .findRootSubject()), isaSource);
      assertTrue("OK: isa source has 2 members", isaMembers.size() == 2);
      
      Source jdbcSource = SubjectFinder.getSource("jdbc");
      Set jdbcMembers = MemberFinder.findAll(GrouperSession.start(SubjectFinder
          .findRootSubject()), jdbcSource);
      assertTrue("OK: jdbc source has 1 member", jdbcMembers.size() == 1);
      assertTrue("jdbc source member has id a", ((Member) jdbcMembers.iterator().next()).getSubjectId().equals("a"));
            
    } catch (Exception e) {
      T.e(e);
    }
  } // public void testFindAll()
  // TESTS //
  
  public void testFailToFindByNullSubject() {
    LOG.info("testFailToFindByNullSubject");
    try {
      MemberFinder.findBySubject(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        null, true
      );
      fail("found member by null subject");
    } catch (NullPointerException npe) {
      //ok
    } catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToFindByNullSubject()
  // TESTS //
  
  public void testFailToFindByNullUuid() {
    LOG.info("testFailToFindByNullUuid");
    try {
      MemberFinder.findByUuid(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        null, true
      );
      fail("found member by null uuid");
    }
    catch (MemberNotFoundException eMNF) {
      assertTrue("OK: did not find member by null uuid", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToFindByNullUuid()
  public void testFindGrouperSystemBySubject() {
    LOG.info("testFindGrouperSystemBySubject");
    try {
      MemberFinder.findBySubject(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        SubjectFinder.findRootSubject(), true
      );
      assertTrue("OK: found member by subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperSystemBySubject()
  public void testFindGrouperSystemByUuid() {
    LOG.info("testFindGrouperSystemByUuid");
    try {
      Subject         root  = SubjectFinder.findRootSubject();
      GrouperSession  s     = GrouperSession.start(root);
      Member          m     = MemberFinder.findBySubject(s, root, true);
      MemberFinder.findByUuid( s, m.getUuid(), true );
      assertTrue("OK: found member by uuid", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperSystemByUuid()

} // public class TestMemberFinder_FindAll