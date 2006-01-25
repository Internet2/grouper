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


import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.Type;
import  org.apache.commons.logging.*;


/**
 * Find fields.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldFinder.java,v 1.8 2006-01-25 22:27:09 blair Exp $
 */
public class FieldFinder {

  // Private Class Constants
  private static final String ERR_FA  = "unable to find all fields: "; 
  private static final String ERR_FAT = "unable to find all fields by type: "; 
  private static final Log    LOG     = LogFactory.getLog(FieldFinder.class);    


  // Private Class Variables
  private static final Map fields  = new HashMap();


  static {
    Iterator iter = findAll().iterator();
    while (iter.hasNext()) {
      Field f = (Field) iter.next();
      fields.put(f.getName(), f);
    }
  } // static 


  // Public Class Methods

  /**
   * Get the specified field.
   * <pre class="eg">
   * Field f = FieldFinder.find(field);
   * </pre>
   * @throws  SchemaException
   */
  public static Field find(String field) 
    throws  SchemaException
  {
    if (fields.containsKey(field)) {
      return (Field) fields.get(field);
    }
    Iterator iter = findAll().iterator();
    while (iter.hasNext()) {
      Field f = (Field) iter.next();
      if (f.getName().equals(field)) {
        fields.put(field, f);
        return f;
      }
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
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Field order by field_name asc");
      qry.setCacheable(GrouperConfig.QRY_FF_FA);
      qry.setCacheRegion(GrouperConfig.QCR_FF_FA);
      fields.addAll(qry.list());
      hs.close();  
    }
    catch (HibernateException eH) {
      String err = ERR_FA + eH.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
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
      String err = ERR_FAT + eH.getMessage();
      LOG.error(err);
      throw new SchemaException(err);
    }
    return fields;
  } // public static Set fieldAllByType(type)

}

