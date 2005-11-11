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
 * @version $Id: FieldFinder.java,v 1.1.2.9 2005-11-11 05:33:03 blair Exp $
 */
public class FieldFinder {

  // Public Class Methods
  public static Set findAll() {
    Set fields = new LinkedHashSet();
    try {
      Session   hs    = HibernateHelper.getSession();
      fields.addAll(
        hs.find("from Field order by field_name asc")
      );
      hs.close();  
    }
    catch (HibernateException eH) {
      throw new RuntimeException(
        "unable to find fields: " + eH.getMessage()
      );
    }
    return fields;
  } // public Static Set findAll()

}

