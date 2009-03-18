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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestStem12.java,v 1.7 2009-03-18 18:51:58 shilen Exp $
 * @since   1.2.0
 */
public class TestStem12 extends GrouperTest {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(TestStem12.class);

  /**
   * 
   * @param name
   */
  public TestStem12(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestStem12("testStemModifyAttributesUpdatedAfterRevokingImmediatePriv"));
  }
  
  /**
   * 
   */
  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // NAMING PRIVS //

  // @since   1.2.0
  public void testStemModifyAttributesUpdatedAfterGrantingImmediatePriv() {
    LOG.info("testStemModifyAttributesUpdatedAfterGrantingImmediatePriv");
    try {
      R       r     = R.populateRegistry(1, 0, 1);
      Stem    nsA   = r.getStem("a");
      Subject subjA = r.getSubject("a");

      long    orig  = nsA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      assertTrue(nsA.grantPriv(subjA, NamingPrivilege.STEM, true));
      long    post  = new java.util.Date().getTime();
      nsA = StemFinder.findByName(r.rs, nsA.getName(), true);
      long    mtime = nsA.getModifyTime().getTime();
      long    mtime_mem = nsA.getLastMembershipChange().getTime();

      assertTrue( "nsA modify time not updated (" + mtime + " == " + orig + ")", mtime == orig );
      assertTrue( "nsA last membership time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "nsA last membership time <= post (" + mtime_mem + " <= " + post + ")", mtime_mem <= post );

      try {
        nsA.grantPriv(subjA, NamingPrivilege.STEM);
        fail("Should throw already exists exception");
      } catch (GrantPrivilegeException gpe) {
        
      }

      assertFalse(nsA.grantPriv(subjA, NamingPrivilege.STEM, false));
      
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  // @since   1.2.0
  public void testStemModifyAttributesUpdatedAfterRevokingImmediatePriv() {
    LOG.info("testStemModifyAttributesUpdatedAfterRevokingImmediatePriv");
    try {
      R       r     = R.populateRegistry(1, 0, 1);
      Stem    nsA   = r.getStem("a");
      Subject subjA = r.getSubject("a");
      nsA.grantPriv(subjA, NamingPrivilege.STEM);

      long    orig  = nsA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      Thread.sleep(1); // TODO 20070430 hack!
      nsA.revokePriv(subjA, NamingPrivilege.STEM);

      //try again
      try {
        nsA.revokePriv(subjA, NamingPrivilege.STEM);
        fail("Problem revoking priv");
      } catch (RevokePrivilegeAlreadyRevokedException rpare) {
        //good
      }

      nsA.revokePriv(subjA, NamingPrivilege.STEM, false);
      nsA = StemFinder.findByName(r.rs, nsA.getName(), true);

      Thread.sleep(1); // TODO 20070430 hack!
      long    post  = new java.util.Date().getTime();
      long    mtime = nsA.getModifyTime().getTime();
      long    mtime_mem = nsA.getLastMembershipChange().getTime();

      assertTrue( "nsA modify time not updated (" + mtime + " == " + orig + ")", mtime == orig );
      assertTrue( "nsA membership change time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "nsA membership change time <= post (" + mtime_mem + " <= " + post + ")", mtime_mem <= post );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 

} 

