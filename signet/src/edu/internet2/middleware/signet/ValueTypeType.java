/*--
$Id: ValueTypeType.java,v 1.3 2006-02-09 10:27:01 lmcrae Exp $
$Date: 2006-02-09 10:27:01 $

Copyright 2006 Internet2, Stanford University

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
package edu.internet2.middleware.signet;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.UserType;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.type.Type;

/**
* Should never be used by Signet application programs. Manages persistence for
* the {@link ValueType} typesafe enumeration. <br />
* This class is just part of the implementation of a typesafe enumeration.
* Unfortunately, Hibernate currently requires this class to be public, even
* though it is of no use to Signet users. If, in the future, Hibernate allows
* this class to be non-public, its visibility should be reduced, to avoid
* distracting and confusing Signet application programmers.
*/

public class ValueTypeType implements UserType
{
 /**
  * Indicates whether objects managed by this type are mutable.
  * 
  * @return <code>false</code>, since enumeration instances are immutable
  *         singletons.
  */
 public boolean isMutable()
 {
   return false;
 }

 /**
  * Return a deep copy of the persistent state, stopping at entities and
  * collections.
  * 
  * @param value
  *          the object whose state is to be copied.
  * @return the same object, since enumeration instances are singletons.
  * @throws ClassCastException
  *           for non {@link ValueType}values.
  */
 public Object deepCopy(Object value)
 {
   return (ValueType) value;
 }

 /**
  * Compare two instances of the class mapped by this type for persistence
  * "equality".
  * 
  * @param x
  *          first object to be compared.
  * @param y
  *          second object to be compared.
  * @return <code>true</code> iff both represent the same ValueType type.
  * @throws ClassCastException
  *           if x or y isn't a {@link ValueType}.
  */
 public boolean equals(Object x, Object y)
 {
   // We can compare instances, since ValueType values are immutable
   // singletons.
   return (x == y);
 }

 /**
  * Determine the class that is returned by {@link #nullSafeGet}.
  * 
  * @return {@link ValueType}, the actual type returned by {@link #nullSafeGet}.
  */
 public Class returnedClass()
 {
   return ValueType.class;
 }

 /**
  * Determine the SQL type(s) of the column(s) used by this type mapping.
  * 
  * @return a single VARCHAR column.
  */
 public int[] sqlTypes()
 {
   // Allocate a new array each time to protect against callers changing
   // its contents.
   int[] typeList = { Types.VARCHAR };
   return typeList;
 }

 /**
  * Retrieve an instance of the mapped class from a JDBC {@link ResultSet}.
  * 
  * @param rs
  *          the results from which the instance should be retrieved.
  * @param names
  *          the columns from which the instance should be retrieved.
  * @param owner
  *          the entity containing the value being retrieved.
  * @return the retrieved {@link ValueType} value, or <code>null</code>.
  * @throws HibernateException
  *           if there is a problem performing the mapping.
  * @throws SQLException
  *           if there is a problem accessing the database.
  */
 public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
     throws HibernateException, SQLException
 {
   // Start by looking up the value name
   String name = (String) Hibernate.STRING.nullSafeGet(rs, names[0]);
   if (name == null)
   {
     return null;
   }
   // Then find the corresponding enumeration value
   try
   {
     return ValueType.getInstanceByName(name);
   }
   catch (java.util.NoSuchElementException e)
   {
     throw new HibernateException("Bad ValueType value: " + name, e);
   }
 }

 /**
  * Write an instance of the mapped class to a {@link PreparedStatement},
  * handling null values.
  * 
  * @param st
  *          a JDBC prepared statement.
  * @param value
  *          the SourceMedia value to write.
  * @param index
  *          the parameter index within the prepared statement at which this
  *          value is to be written.
  * @throws HibernateException
  *           if there is a problem performing the mapping.
  * @throws SQLException
  *           if there is a problem accessing the database.
  */
 public void nullSafeSet(PreparedStatement st, Object value, int index)
     throws HibernateException, SQLException
 {
   String name = null;
   if (value != null)
     name = ((ValueType) value).getName();
   Hibernate.STRING.nullSafeSet(st, name, index);
 }

}
