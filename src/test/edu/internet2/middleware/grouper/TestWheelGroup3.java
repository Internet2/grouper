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
 * @version $Id: TestWheelGroup3.java,v 1.3 2006-09-27 13:10:39 blair Exp $
 * @since   1.1.0
 */
public class TestWheelGroup3 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestWheelGroup3.class);

  public TestWheelGroup3(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testNotWheelThenMakeWheel() {
    LOG.info("testNotWheelThenMakeWheel");
    try {
      R       r     = R.populateRegistry(1, 0, 1);
      Stem    nsA   = r.getStem("a");
      Subject subjA = r.getSubject("a");
      Member  mA    = MemberFinder.findBySubject(r.rs, subjA);
    
      // Before wheel 
      Assert.assertFalse( "!has create" , nsA.hasCreate(subjA)  );
      Assert.assertFalse( "!canCreate"  , mA.canCreate(nsA)     );
      
      // Enable wheel
      Stem    etc   = r.root.addChildStem("etc", "etc");
      Group   wheel = etc.addChildGroup("wheel", "wheel");
      wheel.addMember(subjA);
      GrouperConfig.setProperty(GrouperConfig.GWU, "true"     );
      GrouperConfig.setProperty(GrouperConfig.GWG, "etc:wheel");
      
      // After wheel
      Assert.assertTrue(  "now has create"  , nsA.hasCreate(subjA)  );
      Assert.assertTrue(  "now canCreate"   , mA.canCreate(nsA)     );

      r.rs.stop(); 
    }
    catch (Exception e) {
      T.e(e);
    }
    finally {
      GrouperConfig.setProperty(GrouperConfig.GWU, "false" ); // turn wheel back off
    }
  } // public void testNotWheelThenMakeWheel()

} // public class TestWheelGroup3

