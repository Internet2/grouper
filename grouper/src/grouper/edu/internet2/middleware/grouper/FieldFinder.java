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

package edu.internet2.middleware.grouper;

import java.util.*;
import net.sf.hibernate.*;
import net.sf.hibernate.type.Type;

/**
 * Find fields.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldFinder.java,v 1.1.2.6 2005-11-06 15:55:19 blair Exp $
 */
class FieldFinder {

  // Private Class Variables
  private static Map fields = new HashMap();


  // Protected Class Methods
 
  // @return  A singleton {@link Field} 
  protected static Field getField(String field) {
    if (fields.containsKey(field)) {
      return (Field) fields.get(field);
    }
    try {
      // TODO Schema should be predefined in registry
      //      Or should it be in XML?
      Field f = new Field(field);
System.err.println("saving field: " + field);
      HibernateHelper.save(f);
System.err.println("saved: " + field);
      fields.put(field, f);
      return f; 
    }
    catch (HibernateException e) {
      // TODO For lack of a better alternative at the moment
      throw new RuntimeException(
        "unable to save field '" + field + "': " + e.getMessage()
      );
    }
  } // protected static Field getField(field)

}

