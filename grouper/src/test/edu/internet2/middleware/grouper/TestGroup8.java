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
import  junit.framework.*;
import  org.apache.commons.logging.*;

import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * @author  blair christensen.
 * @version $Id: TestGroup8.java,v 1.5 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestGroup8 extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroup8.class);


  public TestGroup8(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailCanReadFieldValidSubjectNullField() {
    LOG.info("testFailCanReadFieldValidSubjectNullField");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      Group   a     = r.getGroup("a", "a");

      try {
        a.canReadField(r.rs.getSubject(), null);
        Assert.fail("IllegalArgumentException not thrown");
      }
      catch (IllegalArgumentException eIA) {
        T.string( "IllegalArgumentException", "field: null value", eIA.getMessage() );
      }

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadFieldValidSubjectNullField()

}

