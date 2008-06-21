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
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup42.java,v 1.3 2008-06-21 04:16:12 mchyzer Exp $
 * @since   1.1.0
 */
public class TestGroup42 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestGroup42.class);

  public TestGroup42(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testSetAttributeIsPersistedAcrossSessions() {
    LOG.info("testSetAttributeIsPersistedAcrossSessions");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   gA    = r.getGroup("a", "a");
      String  de    = "new display extension";
      String  dn    = gA.getParentStem().getDisplayName() + ":" + de;
      String  uuid  = gA.getUuid();
      gA.setDisplayExtension(de);
      gA.store();
      assertTrue( "group has new de", gA.getDisplayExtension().equals(de) );
      assertTrue( "group has new dn", gA.getDisplayName().equals(dn) );
      r.rs.stop();
      GrouperSession s = GrouperSession.start( SubjectFinder.findRootSubject() );
      Group g = GroupFinder.findByUuid(s, uuid);
      assertTrue( "group still has new de", g.getDisplayExtension().equals(de) );
      assertTrue( "group still has new dn", g.getDisplayName().equals(dn) );
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testSetAttributeIsPersistedAcrossSessions()

} // public class TestGroup42 extends GrouperTest

