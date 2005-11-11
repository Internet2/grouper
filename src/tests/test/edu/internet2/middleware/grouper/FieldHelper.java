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
import  junit.framework.*;

/**
* Field-related helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: FieldHelper.java,v 1.2 2005-11-11 18:39:35 blair Exp $
 */
public class FieldHelper {

  // Protected Class Methods

  protected static void testField(Field f, String name, String type) {
    Assert.assertTrue("f instanceof Field", f instanceof Field);
    Assert.assertTrue("f name = " + name, f.getName().equals(name));
    Assert.assertTrue("f type = " + type, f.getType().toString().equals(type));
    try {
      Field field = FieldFinder.getField(name);
      Assert.assertTrue("got field " + f, true);
      Assert.assertTrue("field instanceof Field", field instanceof Field);
      Assert.assertTrue("field name = " + name, field.getName().equals(name));
      Assert.assertTrue(
        "field type = " + type, field.getType().toString().equals(type)
      );
    }
    catch (SchemaException eS) {
      Assert.fail("failed to get " + name);
    }
  } // protected static void testField(f, name, type)

}

