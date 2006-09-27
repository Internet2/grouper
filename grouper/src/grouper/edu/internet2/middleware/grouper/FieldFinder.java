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

/**
 * Find fields.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldFinder.java,v 1.20 2006-09-27 14:15:30 blair Exp $
 */
public class FieldFinder {

  // PRIVATE CLASS CONSTANTS //
  private static final Map    FIELDS  = new HashMap();
  private static final String KLASS   = FieldFinder.class.getName();


  // STATIC
  static {
    Field     f;
    Iterator  iter  = findAll().iterator();
    while (iter.hasNext()) {
      f = (Field) iter.next();
      FIELDS.put(f.getName(), f);
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
    if (FIELDS.containsKey(name)) {
      return (Field) FIELDS.get(name);
    }
    // If not, refresh known types as it may be new and try again. 
    updateKnownFields();
    if (FIELDS.containsKey(name)) {
      return (Field) FIELDS.get(name);
    }
    throw new SchemaException("field not found: " + name);
  } // public static Field find(name)

  /**
   * Find all fields.
   * <pre class="eg">
   * Set fields = FieldFinder.findAll();
   * </pre>
   * @return  {@link Set} of {@link Field} objects.
   * @throws  GrouperRuntimeException
   */
  public static Set findAll() 
    throws  GrouperRuntimeException
  {
    // TODO 20060927 Refactor caching and move to session-context.
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Field order by field_name asc");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAll");
      fields.addAll(qry.list());
      hs.close();  
    }
    catch (HibernateException eH) {
      String msg = E.FIELD_FINDALL + eH.getMessage();
      ErrorLog.fatal(FieldFinder.class, msg);
      throw new GrouperRuntimeException(msg, eH);
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
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByType");
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


  // PROTECTED CLASS METHODS //
  // @since 1.0
  protected static void updateKnownFields() {
    // This method irks me still even if it is now more functionally correct
    Set fieldsInRegistry = findAll();
    // Look for types to add
    Field     fA;
    Iterator  addIter = fieldsInRegistry.iterator();
    while (addIter.hasNext()) {
      fA = (Field) addIter.next();
      if (!FIELDS.containsKey(fA.getName())) {
        FIELDS.put(fA.getName(), fA); // New field.  Add it to the cached list.
      }
    }
    // Look for fields to remove
    Set       toDel   = new LinkedHashSet();
    Field     fD;
    Iterator  delIter = FIELDS.values().iterator();
    while (delIter.hasNext()) {
      fD = (Field) delIter.next();
      if (!fieldsInRegistry.contains(fD)) {
        toDel.add(fD.getName()); 
      }
    }
    String    field;
    Iterator  toDelIter = toDel.iterator();
    while (toDelIter.hasNext()) {
      field = (String) toDelIter.next();
      FIELDS.remove(field);  
    }
  } // protected static void updateKnownFields()

}

