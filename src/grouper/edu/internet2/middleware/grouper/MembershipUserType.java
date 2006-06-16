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


/** 
 * Custom {@link MembershipType} user type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipUserType.java,v 1.1 2006-06-16 17:30:01 blair Exp $    
 */
public class MembershipUserType implements UserType {

  // Private Class Constants
  private static final int[] SQL_TYPES = { Types.VARCHAR };


  // Public Instance Methods

  public Object deepCopy(Object value) {
    return value;
  } // public Object deepCopy(value)

  public boolean equals(Object x, Object y) {
    return x == y;
  } // public boolean equals(x, y)

  public boolean isMutable() {
    return false;
  } // public boolean isMutable()

  public Object nullSafeGet(
    ResultSet resultSet, String[] types, Object owner
  )
    throws HibernateException, SQLException
  {
    String name = resultSet.getString(types[0]); 
    return resultSet.wasNull() ? null : MembershipType.getInstance(name);
  } // public Object nullSafeGet(resultSet, types, owner)
     
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
 
  public Class returnedClass() {
    return MembershipType.class;
  } // public Class returnedClass()
  
  public int[] sqlTypes() {
    return SQL_TYPES;
  } // public int[] sqlTypes()

}

