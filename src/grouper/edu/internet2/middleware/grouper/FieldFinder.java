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
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;

/**
 * Find fields.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldFinder.java,v 1.28 2007-02-22 23:23:55 blair Exp $
 */
public class FieldFinder {

  // PRIVATE CLASS VARIABLES //
  private static SimpleCache fieldCache = new SimpleCache();


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
    if ( fieldCache.containsKey(name) ) {
      return (Field) fieldCache.get(name);
    }
    internal_updateKnownFields();
    if ( fieldCache.containsKey(name) ) {
      return (Field) fieldCache.get(name);
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
    return new LinkedHashSet( Rosetta.getAPI( HibernateFieldDAO.findAll() ) );
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
    return new LinkedHashSet( Rosetta.getAPI( HibernateFieldDAO.findAllByType(type) ) );
  } // public static Set findAllByType(type)


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  // TODO 20070222 i really hate this method
  protected static void internal_updateKnownFields() {
    Field f;
    Set   fieldsInRegistry = findAll();

    // find fields to add to the cache
    Iterator it = fieldsInRegistry.iterator();
    while (it.hasNext()) {
      f = (Field) it.next();
      if ( !fieldCache.containsKey( f.getName() ) ) {
        fieldCache.put( f.getName(), f ); // TODO 20070222 make sure the "GroupType" is loaded?
      }
    }

    // find fields to remove from the cache
    // TODO 20070222 this exposes a failing of "SimpleCache" that i should remedy
    Set       toDel = new LinkedHashSet();
    Map.Entry kv;
    it              = ( (Map) fieldCache.getCache() ).entrySet().iterator();
    while (it.hasNext()) {
      kv = (Map.Entry) it.next();
      if ( !fieldsInRegistry.contains( (Field) kv.getValue() ) ) {
        toDel.add( kv.getKey() );
      }
    }
    // and now remove the fields
    it = toDel.iterator();
    while (it.hasNext()) {
      fieldCache.remove( (String) it.next() );
    }
  } // protected static void internal_updateKnownFields()

}

