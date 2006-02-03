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

import  java.io.Serializable;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.engine.*;
import  net.sf.hibernate.type.*;


/** 
 * Custom {@link Field} user type.
 * <p />
 * @author  blair christensen.
 * @version $Id: FieldUserType.java,v 1.4 2006-02-03 19:38:53 blair Exp $    
 */
public class FieldUserType implements CompositeUserType {

  // Public Instance Methods
  public Object assemble(
    Serializable cached, SessionImplementor session, Object owner
  )
    throws HibernateException
  {
    return cached;
  } // public Object assemble(cached, session, owner)

  public Object deepCopy(Object value) {
    return value;
  } // public Object deepCopy(value)

  public Serializable disassemble(Object value, SessionImplementor session)
    throws HibernateException
  {
    return (Serializable) value;
  } // public Object disassemble(value, session)

  public boolean equals(Object x, Object y) {
    if (x == y) {
      return true;
    }
    if (x == null || y == null) {
      return false;
    }
    return x.equals(y);
  } // public boolean equals(x, y)

  public String[] getPropertyNames() {
    return new String[] { "name", "type" };
  } // public String[] getPropertyNames()

  public Type[] getPropertyTypes() {
    return new Type[] { Hibernate.STRING, Hibernate.STRING };
  } // public Type[] getPropertyTypes()

  public Object getPropertyValue(Object component, int property)
    throws HibernateException
  {
    Field f = (Field) component;
    if (property == 0) {
      return f.getName();
    }
    else {
      return f.getType().toString();
    }
  } // public Object getPropertyValue(component, property)

  public boolean isMutable() {
    return false;
  } // public boolean isMutable()

  public Object nullSafeGet(
    ResultSet resultSet, String[] names, SessionImplementor session, Object owner
  )
    throws HibernateException, SQLException
  {
    if (resultSet.wasNull()) {
      return null;
    }
    try {
      return FieldFinder.find(
        resultSet.getString( names[0] )
      );
    }
    catch (SchemaException eS) {
      throw new HibernateException(
        "unable to get field: " + eS.getMessage()
      );
    }
  } // public Object nullSafeGet(resultSet, types, session, owner)
     
  public void nullSafeSet(
    PreparedStatement statement, Object value, int index, SessionImplementor session
  ) 
    throws HibernateException, SQLException
  {
    if (value == null) {
      statement.setNull(index,    Types.VARCHAR);
      statement.setNull(index+1,  Types.VARCHAR);
    } 
    else {
      Field f = (Field) value;
      statement.setString(index,    f.getName());
      statement.setString(index+1,  f.getType().toString());
    }
  } // public void nullSafeSet(statement, value, index, session)
 
  public Class returnedClass() {
    return Field.class;
  } // public Class returnedClass() 
 
  public void setPropertyValue(Object component, int property, Object value) 
    throws HibernateException
  {
    throw new UnsupportedOperationException("immutable!");
  } // public void setPropertyValue(comonent, property, value)

}

