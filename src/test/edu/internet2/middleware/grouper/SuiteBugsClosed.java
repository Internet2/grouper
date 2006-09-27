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
import  junit.framework.*;

/**
 * Test Closed Bugs.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SuiteBugsClosed.java,v 1.10 2006-09-27 14:15:30 blair Exp $
 * @since   1.0
 */
public class SuiteBugsClosed extends TestCase {

  public SuiteBugsClosed(String name) {
    super(name);
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();        
    suite.addTestSuite( TestBug0.class        );  // Population of children-of-via 
                                                  // groups in an immediate mof delete
    suite.addTestSuite( TestMembership1.class );  // Invalid setting of parent membership
    suite.addTestSuite( TestMemberOf1.class   );  // Forward MemberOf deletion.
    suite.addTestSuite( TestGroup37.class     );  // GCODE:11 getTypes() + getRemovableTypes() - with custom type + !root subject with ADMIN
    suite.addTestSuite( TestStem11.class      );  // GCODE:10 getPrivs(), getStemmers(), getCreators() as !root
    suite.addTestSuite( TestGroup38.class     );  // GCODE:16 throw eIP, not eGM, if not priv'd to modify attr
    suite.addTestSuite( TestGroup39.class     );  // GCODE:16 throw eIP, not eGM, if not priv'd to delete attr
    suite.addTestSuite( TestWheelGroup3.class );  // GCODE:13 !wheel; grant; wheel
    // TODO 20060927 Split pre-existing tests
    suite.addTestSuite( TestBugsClosed.class);
    return suite;
  } // static public Test suite()

} // public class SuiteBugsClosed

