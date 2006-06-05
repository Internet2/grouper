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
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.Type;

/**
 * Find fields.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldFinder.java,v 1.11 2006-06-05 19:54:40 blair Exp $
 */
public class FieldFinder {

  // PRIVATE CLASS VARIABLES //
  private static final Map fields  = new HashMap();


  // STATIC
  static {
    Iterator iter = findAll().iterator();
    while (iter.hasNext()) {
      Field f = (Field) iter.next();
      fields.put(f.getName(), f);
    }
  } // static 


  // PUBLIC CLASS METHODS //

  /**
   * Get the specified field.
   * <pre class="eg">
   * Field f = FieldFinder.find(field);
   * </pre>
   * @param   name  Name of {@link Field} to return.
   * @throws  SchemaException
   */
  public static Field find(String name) 
    throws  SchemaException
  {
    // First check to see if type is cached.
    if (fields.containsKey(name)) {
      return (Field) fields.get(name);
    }
    // If not, refresh known types as it may be new and try again. 
    _updateKnownFields();
    if (fields.containsKey(name)) {
      return (Field) fields.get(name);
    }
    throw new SchemaException("field not found: " + name);
  } // public static Field find(name)

  /**
   * Find all fields.
   * <pre class="eg">
   * Set fields = FieldFinder.findAll();
   * </pre>
   */
  public static Set findAll() {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Field order by field_name asc");
      qry.setCacheable(GrouperConfig.QRY_FF_FA);  // TODO I'm wary
      qry.setCacheRegion(GrouperConfig.QCR_FF_FA);
      fields.addAll(qry.list());
      hs.close();  
    }
    catch (HibernateException eH) {
      String msg = E.FIELD_FINDALL + eH.getMessage();
      ErrorLog.fatal(FieldFinder.class, msg);
      throw new RuntimeException(msg, eH);
    }
    return fields;
  } // public Static Set findAll()

  /**
   * Find all fields of the specified type.
   * <pre class="eg">
   * Set types = FieldFinder.findAllByType(type);
   * </pre>
   */
  public static Set findAllByType(FieldType type) 
    throws  SchemaException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Field where field_type = :type order by field_name asc"
      );
      qry.setCacheable(GrouperConfig.QRY_FF_FABT);
      qry.setCacheRegion(GrouperConfig.QCR_FF_FABT);
      qry.setString("type", type.toString());
      fields.addAll(qry.list());
      hs.close();
    }
    catch (HibernateException eH) {
      String msg = E.FIELD_FINDTYPE + eH.getMessage();
      ErrorLog.error(FieldFinder.class, msg);
      throw new SchemaException(msg, eH);
    }
    return fields;
  } // public static Set fieldAllByType(type)


  // PRIVATE CLASS METHODS //
  private static void _updateKnownFields() {
    // TODO This method irks me still even if it is now more
    //      functionally correct
    Set fieldsInRegistry = findAll();
    // Look for types to add
    Iterator addIter = fieldsInRegistry.iterator();
    while (addIter.hasNext()) {
      Field f = (Field) addIter.next();
      if (!fields.containsKey(f.getName())) {
        fields.put(f.getName(), f); // New field.  Add it to the cached list.
      }
    }
    // Look for fields to remove
    Set       toDel   = new LinkedHashSet();
    Iterator  delIter = fields.values().iterator();
    while (delIter.hasNext()) {
      Field f = (Field) delIter.next();
      if (!fieldsInRegistry.contains(f)) {
        toDel.add(f.getName()); 
      }
    }
    Iterator  toDelIter = toDel.iterator();
    while (toDelIter.hasNext()) {
      String field = (String) toDelIter.next();
      fields.remove(field);  
    }
  } // private static void _updateKnownFields()

}

