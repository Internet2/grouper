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
 * @version $Id: FieldFinder.java,v 1.4 2005-11-28 18:33:22 blair Exp $
 */
public class FieldFinder {

  // Private Static Variables
  private static final Map FIELDS = new HashMap();

  static {
    Iterator iter = findAll().iterator();
    while (iter.hasNext()) {
      Field f = (Field) iter.next();
      FIELDS.put(f.getName(), f);
    }
  } // static 


  // Public Class Methods

  /**
   * Get the specified field.
   * <pre class="eg">
   * Field f = FieldFinder.find(field);
   * </pre>
   */
  public static Field find(String field) 
    throws SchemaException
  {
    if (FIELDS.containsKey(field)) {
      return (Field) FIELDS.get(field);
    }
    throw new SchemaException("invalid field: " + field);
  } // public static Field find(field)

  /**
   * Find all fields.
   * <pre class="eg">
   * Set fields = FieldFinder.findAll();
   * </pre>
   */
  public static Set findAll() {
    // TODO Should this return the cached results if they exist?
    //      Likewise, should it update the cached results if they
    //      exist?
    Set fields = new LinkedHashSet();
    try {
      Session hs = HibernateHelper.getSession();
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

  /**
   * Find all fields of the specified type.
   * <pre class="eg">
   * Set types = FieldFinder.findAllByType(type);
   * </pre>
   */
  public static Set findAllByType(FieldType type) {
    Set fields = new LinkedHashSet();
    try {
      Session hs = HibernateHelper.getSession();
      fields.addAll(
        hs.find(
          "from Field where field_type = ? order by field_name asc",
          type.toString(),
          Hibernate.STRING
        )
      );
      hs.close();  
    }
    catch (HibernateException eH) {
      throw new RuntimeException(
        "unable to find fields: " + eH.getMessage()
      );
    }
    return fields;
  } // public static Set fieldAllByType(type)

}

