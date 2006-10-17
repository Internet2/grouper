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
 * @author  blair christensen.
 * @version $Id: SubSuiteSimpleWPCache.java,v 1.1 2006-10-17 16:12:00 blair Exp $
 * @since   1.1.0
 */
public class SubSuiteSimpleWPCache extends TestCase {

  static public Test suite() {
    TestSuite suite = new TestSuite();        

    suite.addTestSuite( TestSimpleWPCache0.class ); // instantiate

    // get(o, subj, p)
    suite.addTestSuite( TestSimpleWPCache1.class ); // get() - OK NullPrivilegeCacheElement

    // grantPriv(o, subj, p)

    // put(o, subj, p, hasPriv)
    suite.addTestSuite( TestSimpleWPCache2.class ); // put() - OK add cache entry

    // removeAll()

    // revokePriv(o, p)

    // revokePriv(o, subj, p)

    return suite;
  } // static public Test suite()

} // public class SubSuiteSimpleWPCache

