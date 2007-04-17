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
import  edu.internet2.middleware.grouper.internal.cache.SimpleCache;
import  edu.internet2.middleware.grouper.internal.dto.FieldDTO;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;

/**
 * Find fields.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldFinder.java,v 1.34 2007-04-17 18:45:13 blair Exp $
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
    Set       fields  = new LinkedHashSet();
    Iterator  it      = GrouperDAOFactory.getFactory().getField().findAll().iterator();
    while (it.hasNext()) {
      fields.add( new Field().setDTO( (FieldDTO) it.next() ) );
    }
    return fields;
  }

  /**
   * Find all fields of the specified type.
   * <pre class="eg">
   * Set types = FieldFinder.findAllByType(type);
   * </pre>
   */
  public static Set findAllByType(FieldType type) 
    throws  SchemaException
  {
    Set       fields  = new LinkedHashSet();
    Iterator  it      = GrouperDAOFactory.getFactory().getField().findAllByType(type).iterator();
    while (it.hasNext()) {
      fields.add( new Field().setDTO( (FieldDTO) it.next() ) );
    }
    return fields;
  }


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

