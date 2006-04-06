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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * @author  blair christensen.
 * @version $Id: TestMships0.java,v 1.1 2006-04-06 16:53:38 blair Exp $
 */
public class TestMships0 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestMships0.class);

  public TestMships0(String name) {
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

  public void testGetMembers() {
    LOG.info("testGetMembers");
    try {
      R r = R.createOneStemAndTwoGroups();
      try {
        // Base
        T.getMembers(         r.i2, 0);
        T.getImmediateMembers(r.i2, 0);
        T.getEffectiveMembers(r.i2, 0);
        T.getMembers(         r.uc, 0);
        T.getImmediateMembers(r.uc, 0);
        T.getEffectiveMembers(r.uc, 0);

        // Add member to a group
        r.i2.addMember(r.subj0);
        r.rs.waitForTx();
        T.getMembers(         r.i2, 1);
        T.getImmediateMembers(r.i2, 1);
        T.getEffectiveMembers(r.i2, 0);
        T.getMembers(         r.uc, 0);
        T.getImmediateMembers(r.uc, 0);
        T.getEffectiveMembers(r.uc, 0);

        // Add member to another group
        r.uc.addMember(r.subj1);
        r.rs.waitForTx();
        T.getMembers(         r.i2, 1);
        T.getImmediateMembers(r.i2, 1);
        T.getEffectiveMembers(r.i2, 0);
        T.getMembers(         r.uc, 1);
        T.getImmediateMembers(r.uc, 1);
        T.getEffectiveMembers(r.uc, 0);

        // Add group as a member
        r.i2.addMember(r.uc.toSubject());
        r.rs.waitForTx();
        T.getMembers(         r.i2, 3);
        T.getImmediateMembers(r.i2, 2);
        T.getEffectiveMembers(r.i2, 1);
        T.getMembers(         r.uc, 1);
        T.getImmediateMembers(r.uc, 1);
        T.getEffectiveMembers(r.uc, 0);

        // Now test the actual memberships
        Iterator iter  = r.i2.getMembers().iterator();
        while (iter.hasNext()) {
          Subject subj = ( (Member) iter.next()).getSubject();
          if      (SubjectHelper.eq(subj, r.subj0         ) ) {
            Assert.assertTrue("i2 getMember == subj0" , true);
          }
          else if (SubjectHelper.eq(subj, r.uc.toSubject()) ) {
            Assert.assertTrue("i2 getMember == uc"    , true);
          }
          else if (SubjectHelper.eq(subj, r.subj1         ) ) {
            Assert.assertTrue("i2 getMember == subj1" , true);
          }
          else {
            Assert.fail("i2 getMember? " + subj);
          }
        }
        iter  = r.i2.getImmediateMembers().iterator();
        while (iter.hasNext()) {
          Subject subj = ( (Member) iter.next()).getSubject();
          if      (SubjectHelper.eq(subj, r.subj0         ) ) {
            Assert.assertTrue("i2 getImmMember == subj0" , true);
          }
          else if (SubjectHelper.eq(subj, r.uc.toSubject()) ) {
            Assert.assertTrue("i2 getImmMember == uc"    , true);
          }
          else {
            Assert.fail("i2 getImmMember? " + subj);
          }
        }
        iter  = r.i2.getEffectiveMembers().iterator();
        while (iter.hasNext()) {
          Subject subj = ( (Member) iter.next()).getSubject();
          if (SubjectHelper.eq(subj, r.subj1) ) {
            Assert.assertTrue("i2 getEffMember == subj1" , true);
          }
          else {
            Assert.fail("i2 getEffMember? " + subj);
          }
        }
        iter  = r.uc.getMembers().iterator();
        while (iter.hasNext()) {
          Subject subj = ( (Member) iter.next()).getSubject();
          if (SubjectHelper.eq(subj, r.subj1) ) {
            Assert.assertTrue("uc getMember == subj1" , true);
          }
          else {
            Assert.fail("uc getMember? " + subj);
          }
        }
        iter  = r.uc.getImmediateMembers().iterator();
        while (iter.hasNext()) {
          Subject subj = ( (Member) iter.next()).getSubject();
          if (SubjectHelper.eq(subj, r.subj1) ) {
            Assert.assertTrue("uc getImmMember == subj1" , true);
          }
        }
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
  } // public void testGetMembers()

}

