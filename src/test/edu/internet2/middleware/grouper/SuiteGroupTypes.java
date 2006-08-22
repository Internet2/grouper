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
 * @version $Id: SuiteGroupTypes.java,v 1.5 2006-08-22 19:48:22 blair Exp $
 */
public class SuiteGroupTypes extends TestCase {

  public SuiteGroupTypes(String name) {
    super(name);
  } // public SuiteGroupTypes(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestGroupType0.class  );  // GroupTypeFinder.findAll()
    suite.addTestSuite( TestGroupType1.class  );  // GroupTypeFinder.findAll() after addition
    suite.addTestSuite( TestGroupType2.class  );  // GroupTypeFinder.findAllAssignable() 
    suite.addTestSuite( TestGroupType3.class  );  // GroupTypeFinder.findAllAssignable() after addition
    suite.addTestSuite( TestGroupType4.class  );  // add and delete custom type as non-root
    suite.addTestSuite( TestGroupType5.class  );  // fail: add list to "base" as non-root
    suite.addTestSuite( TestGroupType6.class  );  // fail: delete type as !root
    suite.addTestSuite( TestGroupType7.class  );  // fail: delete system type
    suite.addTestSuite( TestGroupType8.class  );  // fail: delete in use type
    suite.addTestSuite( TestGroupType9.class  );  // delete type
    suite.addTestSuite( TestGroupType10.class );  // delete type with fields
    suite.addTestSuite( TestGroupType11.class );  // use custom attribute as non-root
    // TODO split - and the damn ordering is important here as these tests leave junk behind.
    //      i should really fix that.
    suite.addTestSuite( TestGroupTypes.class  );  
    return suite;
  } // static public Test suite()

}

