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
 * @version $Id: FieldHelper.java,v 1.4 2005-11-28 18:33:22 blair Exp $
 */
public class FieldHelper {

  // Protected Class Methods

  protected static void testField(
    Field f, String name, FieldType type, Privilege read, Privilege write
  ) 
  {
    _testField(f, name, type, read, write);
    try {
      Field field = FieldFinder.find(name);
      _testField(field, name, type, read, write);
    }
    catch (SchemaException eS) {
      Assert.fail("failed to get " + name);
    }
  } // protected static void testField(f, name, type, read, write)


  // Private Class Methods
  private static void _testField(
    Field f, String name, FieldType type, Privilege read, Privilege write
  )
  {
    Assert.assertTrue(name + " instanceof Field", f instanceof Field);
    Assert.assertTrue(name + " name  = " + name, f.getName().equals(name));
    Assert.assertTrue(name + " type  = " + type, f.getType().toString().equals(type.toString()));
    Assert.assertTrue(name + " read  = " + read, f.getReadPriv().equals(read));
    Assert.assertTrue(name + " write = " + read, f.getWritePriv().equals(write));
  } // private static void _testField(f, name, type, read, write)

}

