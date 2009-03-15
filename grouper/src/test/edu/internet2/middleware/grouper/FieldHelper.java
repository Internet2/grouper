/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import junit.framework.Assert;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.privs.Privilege;

/**
* Field-related helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: FieldHelper.java,v 1.8 2009-03-15 06:37:22 mchyzer Exp $
 */
public class FieldHelper {

  // Protected Class Methods

  protected static void testField(
    Field f, String name, FieldType type, Privilege read, Privilege write
  ) 
  {
    _testField(f, name, type, read, write);
    try {
      Field field = FieldFinder.find(name, true);
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
    T.string( "field name", name, f.getName() );
    T.string( "field type", type.toString(), f.getType().toString() );
    T.string( "field read priv", read.toString(), f.getReadPriv().toString() );
    T.string( "field write priv", write.toString(), f.getWritePriv().toString() );
  } // private static void _testField(f, name, type, read, write)

}

