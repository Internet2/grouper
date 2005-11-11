/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
 * @version $Id: TestField.java,v 1.2 2005-11-11 18:39:35 blair Exp $
 */
public class TestField extends TestCase {

  public TestField(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testFields() {
    Set       fields  = FieldFinder.findAll();
    Iterator  iter    = fields.iterator();
    FieldHelper.testField( (Field) iter.next(), "admins",           "list"      );
    FieldHelper.testField( (Field) iter.next(), "creators",         "list"      );
    FieldHelper.testField( (Field) iter.next(), "description",      "attribute" );
    FieldHelper.testField( (Field) iter.next(), "displayExtension", "attribute" );
    FieldHelper.testField( (Field) iter.next(), "displayName",      "attribute" );
    FieldHelper.testField( (Field) iter.next(), "extension",        "attribute" );
    FieldHelper.testField( (Field) iter.next(), "members",          "list"      );
    FieldHelper.testField( (Field) iter.next(), "name",             "attribute" );
    FieldHelper.testField( (Field) iter.next(), "optins",           "list"      );
    FieldHelper.testField( (Field) iter.next(), "optouts",          "list"      );
    FieldHelper.testField( (Field) iter.next(), "readers",          "list"      );
    FieldHelper.testField( (Field) iter.next(), "stemmers",         "list"      );
    FieldHelper.testField( (Field) iter.next(), "updaters",         "list"      );
    FieldHelper.testField( (Field) iter.next(), "viewers",          "list"      );
  } // public void testFields()

}

