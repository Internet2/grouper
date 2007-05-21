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
 * @version $Id: Suite_U_Util.java,v 1.1 2007-05-21 16:16:41 blair Exp $
 * @since   1.2.0
 */
public class Suite_U_Util extends GrouperTest {

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest( Suite_U_Util_XML.suite() );
    return suite;
  } 

} 

