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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestPrivAdmin2.java,v 1.1 2006-09-13 15:52:09 blair Exp $
 * @since   1.1.0
 */
public class TestPrivAdmin2 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestPrivAdmin2.class);

  public TestPrivAdmin2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testDeleteAttributesWithAllAdmin() {
    LOG.info("testDeleteAttributesWithAllAdmin");
    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Group   gA    = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      gA.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
      String  val   = "new attribute value";
      GrouperSession nrs = GrouperSession.start(subjA);
      gA.setSession(nrs);
      // fail: delete empty attr
      try {
        gA.deleteAttribute("");
        Assert.fail("FAIL: deleted empty attribute");
      }
      catch (AttributeNotFoundException e) {
        Assert.assertTrue("OK: failed to delete empty attribute", true);
      }
      // fail: delete unset attribute
      try {
        gA.deleteAttribute("description");
        Assert.fail("FAIL: deleted unset attribute");
      }
      catch (AttributeNotFoundException e) {
        Assert.assertTrue("OK: failed to delete unset attribute", true);
      }
      // ok: delete attribute with set value
      gA.setAttribute("description", val);
      T.string("set description", val, gA.getAttribute("description"));
      gA.deleteAttribute("description");
      //T.string("deleted description", GrouperConfig.EMPTY_STRING, gA.getAttribute("description"));
      T.string("BUG deleted description", val, gA.getAttribute("description"));
      // fail: cannot delete `displayName`
      try {
        gA.deleteAttribute("displayName");
        Assert.fail("FAIL: deleted displayName");
      }
      catch (GroupModifyException e) {
        Assert.assertTrue("OK: failed to delete displayName", true);
      }
      // fail: cannot delete `displayExtension`
      try {
        gA.deleteAttribute("displayExtension");
        Assert.fail("FAIL: deleted displayExtension");
      }
      catch (GroupModifyException e) {
        Assert.assertTrue("OK: failed to delete displayExtension", true);
      }
      // fail: cannot delete `extension`
      try {
        gA.deleteAttribute("extension");
        Assert.fail("FAIL: deleted extension");
      }
      catch (GroupModifyException e) {
        Assert.assertTrue("OK: failed to delete extension", true);
      }
      // fail: cannot delete `name`
      try {
        gA.deleteAttribute("name");
        Assert.fail("FAIL: deleted name");
      }
      catch (GroupModifyException e) {
        Assert.assertTrue("OK: failed to delete name", true);
      }
      nrs.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }

  } // public void testDeleteAttributesWithAllAdmin()

} // public class TestPrivAdmin2
