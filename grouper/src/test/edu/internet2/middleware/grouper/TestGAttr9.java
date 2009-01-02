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
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestGAttr9.java,v 1.7 2009-01-02 06:57:11 mchyzer Exp $
 * @since   1.1.0
 */
public class TestGAttr9 extends TestCase {

  private static final Log LOG = GrouperUtil.getLog(TestGAttr9.class);

  public TestGAttr9(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testSetAttribute() {
    LOG.info("testSetAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");

      GroupType groupType = GroupType.createType(r.rs, "theGroupType", false); 
      groupType.addAttribute(r.rs, "theAttribute1", 
            AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
      gA.addType(groupType, false);
      String theAttribute = "theAttribute1";

      String  v = "foo";
      gA.setAttribute(theAttribute, v);
      gA.store();
      T.ok("set attr value");
      T.string(
        "updated attr value",
        v,
        gA.getAttribute(theAttribute)
      );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testSetAttribute()

} // public class TestGAttr9

