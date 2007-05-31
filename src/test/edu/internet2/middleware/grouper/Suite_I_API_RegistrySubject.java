/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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
 * @version $Id: Suite_I_API_RegistrySubject.java,v 1.1 2007-05-31 17:57:45 blair Exp $
 * @since   1.2.0
 */
public class Suite_I_API_RegistrySubject extends GrouperTest {

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( Test_I_API_RegistrySubject_delete.class );
    return suite;
  } 

} 

