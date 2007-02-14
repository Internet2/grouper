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
import  edu.internet2.middleware.subject.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestStem12.java,v 1.1 2007-02-14 19:55:17 blair Exp $
 * @since   1.2.0
 */
public class TestStem12 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestStem12.class);

  public TestStem12(String name) {
    super(name);
  }

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
      nsA.grantPriv(subjA, NamingPrivilege.STEM);
      long    post  = new java.util.Date().getTime();
      assertTrue( "nsA modify time updated", nsA.getModifyTime().getTime() > orig );
      assertTrue( "nsA modifyTime >= pre",    nsA.getModifyTime().getTime() >= pre );
      assertTrue( "nsA modifyTime <= post",   nsA.getModifyTime().getTime() <= post );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testStemModifyAttributesUpdatedAfterGrantingImmediatePriv()

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
      nsA.revokePriv(subjA, NamingPrivilege.STEM);
      long    post  = new java.util.Date().getTime();
      assertTrue( "nsA modify time updated", nsA.getModifyTime().getTime() > orig );
      assertTrue( "nsA modifyTime >= pre",    nsA.getModifyTime().getTime() >= pre );
      assertTrue( "nsA modifyTime <= post",   nsA.getModifyTime().getTime() <= post );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testStemModifyAttributesUpdatedAfterRevokingImmediatePriv()

} // public class TestStem12 extends GrouperTest

