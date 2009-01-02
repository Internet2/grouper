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

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestGAttr17.java,v 1.11 2009-01-02 06:57:11 mchyzer Exp $
 * @since   1.1.0
 */
public class TestGAttr17 extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGAttr17("testDeleteAttributeNotRootButHasAdmin"));
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestGAttr17.class);

  public TestGAttr17(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testDeleteAttributeNotRootButHasAdmin() {
    LOG.info("testDeleteAttributeNotRootButHasAdmin");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");

      GrouperSession grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );

      GroupType groupType = GroupType.createType(grouperSession, "theGroupType", false); 
      groupType.addAttribute(grouperSession, "theAttribute1", 
            AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
      gA.addType(groupType, false);
      String theAttribute = "theAttribute1";
      gA.setAttribute(theAttribute, "whatever");

      gA.store();

      gA.grantPriv(subjA, AccessPrivilege.ADMIN);

      grouperSession.stop();
      

      r.rs.stop();  
      GrouperSession.start(subjA);
      gA.setAttribute(theAttribute, theAttribute);
      gA.store();
      gA.deleteAttribute(theAttribute);
      T.string(
        "fetch deleted attribute",
        GrouperConfig.EMPTY_STRING,
        gA.getAttribute(theAttribute)
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testDeleteAttributeNotRootButHasAdmin()

} // public class TestGAttr17

