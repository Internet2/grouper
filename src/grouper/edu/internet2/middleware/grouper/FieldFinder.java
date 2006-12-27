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
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;

/**
 * Find fields.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldFinder.java,v 1.23 2006-12-27 18:22:21 blair Exp $
 */
public class FieldFinder {

  // PRIVATE CLASS CONSTANTS //
  private static final Map FIELDS = new HashMap();


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
    return HibernateFieldDAO.findAll();
  } // public static Set findAll()

  /**
   * Find all fields of the specified type.
   * <pre class="eg">
   * Set types = FieldFinder.findAllByType(type);
   * </pre>
   */
  public static Set findAllByType(FieldType type) 
    throws  SchemaException
  {
    return HibernateFieldDAO.findAllByType(type);
  } // public static Set findAllByType(type)


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

