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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestGAttr11.java,v 1.1 2006-09-21 16:10:23 blair Exp $
 * @since   1.1.0
 */
public class TestGAttr11 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestGAttr11.class);

  public TestGAttr11(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailDeleteAttributeBlankAttribute() {
    LOG.info("testFailDeleteAttributeBlankAttribute");
    try {
      R     r   = R.populateRegistry(1, 1, 0);
      Group gA  = r.getGroup("a", "a");

      try {
        gA.deleteAttribute("");
        T.fail("deleted blank attribute");
      }
      catch (AttributeNotFoundException eANF) {
        T.ok("did not delete blank attribute");
      }

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailDeleteAttributeBlankAttribute()

} // public class TestGAttr11

