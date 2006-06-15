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
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.*;
import  org.apache.commons.lang.builder.*;

/** 
 * Custom {@link CompositeType} user type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CompositeTypeUserType.java,v 1.4 2006-06-15 04:45:58 blair Exp $    
 * @since   1.0
 */
public class CompositeTypeUserType implements UserType {

  // PRIVATE CLASS CONSTANTS //
  private static final int[] SQL_TYPES = { Types.VARCHAR };


  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.0
   */
  public Object deepCopy(Object value) {
    return value;
  } // public Object deepCopy(value)

  /**
   * @since 1.0
   */
  public boolean equals(Object x, Object y) {
    return x == y;
  } // public boolean equals(x, y)

  /**
   * @since 1.0
   */
  public boolean isMutable() {
    return false;
  } // public boolean isMutable()

  /**
   * @since 1.0
   */
  public Object nullSafeGet(
    ResultSet resultSet, String[] types, Object owner
  )
    throws HibernateException, SQLException
  {
    String type = resultSet.getString(types[0]); 
    return resultSet.wasNull() ? null : CompositeType.getInstance(type);
  } // public Object nullSafeGet(resultSet, types, owner)
     
  /**
   * @since 1.0
   */
  public void nullSafeSet(
    PreparedStatement statement, Object value, int index
  ) 
    throws HibernateException, SQLException
  {
    if (value == null) {
      statement.setNull(index, Types.VARCHAR);
    } 
    else {
      statement.setString(index, value.toString());
    }
  } // public void nullSafeSet(statement, value, index)
 
  /**
   * @since 1.0
   */
  public Class returnedClass() {
    return CompositeType.class;
  } // public Class returnedClass()
  
  /**
   * @since 1.0
   */
  public int[] sqlTypes() {
    return SQL_TYPES;
  } // public int[] sqlTypes()

}

