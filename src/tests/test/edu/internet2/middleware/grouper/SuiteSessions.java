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

package test.edu.internet2.middleware.grouper;

import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: SuiteSessions.java,v 1.1 2006-03-29 17:19:25 blair Exp $
 */
public class SuiteSessions extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteSessions.class); 

  public SuiteSessions(String name) {
    super(name);
  } // public SuiteSessions(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestSession0.class  );
    suite.addTestSuite( TestSession1.class  );
    suite.addTestSuite( TestSession2.class  );
    suite.addTestSuite( TestSession3.class  );
    suite.addTestSuite( TestSession4.class  );
    suite.addTestSuite( TestSession5.class  );
    return suite;
  } // static public Test suite()

}

