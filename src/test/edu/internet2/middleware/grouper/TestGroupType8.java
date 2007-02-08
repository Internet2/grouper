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

/**
 * @author  blair christensen.
 * @version $Id: TestGroupType8.java,v 1.4 2007-02-08 16:25:25 blair Exp $
 */
public class TestGroupType8 extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroupType8.class);

  public TestGroupType8(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailToDeleteWhenInUse() {
    LOG.info("testFailToDeleteWhenInUse");
    try {
      R               r       = R.populateRegistry(1, 1, 0);
      GroupType       custom  = GroupType.createType(r.rs, "custom");
      Group           gA      = r.getGroup("a", "a");
      gA.addType(custom);
      try {
        custom.delete(r.rs);
        Assert.fail("deleted in use type");
      }
      catch (SchemaException eS) {
        T.string(
          "OK: failed to delete in use type", 
          E.GROUPTYPE_DELINUSE,
          eS.getMessage()
        );
      } 
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteWhenInUse()

}

