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

import  edu.internet2.middleware.grouper.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link Field}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestField.java,v 1.6 2006-02-03 19:38:53 blair Exp $
 */
public class TestField extends TestCase {

  public TestField(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testFields() {
    Set       fields  = FieldFinder.findAll();
    Assert.assertTrue("fields: 14", fields.size() == 14);
    Iterator  iter    = fields.iterator();
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "admins"              , FieldType.ACCESS,
      AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "creators"            , FieldType.NAMING,
      NamingPrivilege.STEM  , NamingPrivilege.STEM
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "description"         , FieldType.ATTRIBUTE,
      AccessPrivilege.READ  , AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "displayExtension"    , FieldType.ATTRIBUTE,
      AccessPrivilege.VIEW  , AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "displayName"         , FieldType.ATTRIBUTE,
      AccessPrivilege.VIEW  , AccessPrivilege.SYSTEM
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "extension"           , FieldType.ATTRIBUTE,
      AccessPrivilege.VIEW, AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "members"             , FieldType.LIST,
      AccessPrivilege.READ  , AccessPrivilege.UPDATE
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "name"                , FieldType.ATTRIBUTE,
      AccessPrivilege.VIEW  , AccessPrivilege.SYSTEM
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "optins"              , FieldType.ACCESS,
      AccessPrivilege.UPDATE, AccessPrivilege.UPDATE
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "optouts"             , FieldType.ACCESS,
      AccessPrivilege.UPDATE, AccessPrivilege.UPDATE
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "readers"             , FieldType.ACCESS,
      AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "stemmers"            , FieldType.NAMING,
      NamingPrivilege.STEM  , NamingPrivilege.STEM
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "updaters"            , FieldType.ACCESS,
      AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
    );
    FieldHelper.testField( 
      (Field) iter.next()   , 
      "viewers"             , FieldType.ACCESS,
      AccessPrivilege.ADMIN , AccessPrivilege.ADMIN
    );
  } // public void testFields()

}

