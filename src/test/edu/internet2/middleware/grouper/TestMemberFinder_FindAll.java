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
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  tzeller
 * @version 
 * @since   1.3.0
 */
public class TestMemberFinder_FindAll extends GrouperTest {
  
  private static final Log LOG = LogFactory.getLog(TestMemberFinder_FindAll.class);
  
  public TestMemberFinder_FindAll(String name) {
    super(name);
  }
  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }
  
  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // TESTS //
  
  public void testFindAll() {
    LOG.info("testFindAll");
    try {      
      R r = R.populateRegistry(1, 3, 2);      
      Group gA = r.getGroup("a", "a");
      Group gB = r.getGroup("a", "b");      
      Subject subjA = SubjectFinder.findById("a");
      
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

} // public class TestMemberFinder_FindAll