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
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: SuiteGroupTypes.java,v 1.1.2.1 2006-04-11 16:55:14 blair Exp $
 */
public class SuiteGroupTypes extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteGroupTypes.class); 

  public SuiteGroupTypes(String name) {
    super(name);
  } // public SuiteGroupTypes(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestGroupType0.class  );  // GroupTypeFinder.findAll()
    suite.addTestSuite( TestGroupType1.class  );  // GroupTypeFinder.findAll() after addition
    // TODO split - and the damn ordering is important here as these tests leave junk behind.
    //      i should really fix that.
    suite.addTestSuite( TestGroupTypes.class  );  
    return suite;
  } // static public Test suite()

}

