/*--
 $Id: StatusType.java,v 1.3 2005-01-11 20:38:44 acohen Exp $
 $Date: 2005-01-11 20:38:44 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
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
 * the {@link Status}typesafe enumeration. <br />
 * This class is just part of the implementation of a typesafe enumeration.
 * Unfortunately, Hibernate currently requires this class to be public, even
 * though it is of no use to Signet users. If, in the future, Hibernate allows
 * this class to be non-public, its visibility should be reduced, to avoid
 * distracting and confusing Signet application programmers.
 */

public class StatusType implements UserType
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
   *           for non {@link SourceMedia}values.
   */
  public Object deepCopy(Object value)
  {
    return (Status) value;
  }

  /**
   * Compare two instances of the class mapped by this type for persistence
   * "equality".
   * 
   * @param x
   *          first object to be compared.
   * @param y
   *          second object to be compared.
   * @return <code>true</code> iff both represent the same Status type.
   * @throws ClassCastException
   *           if x or y isn't a {@link Status}.
   */
  public boolean equals(Object x, Object y)
  {
    // We can compare instances, since Status values are immutable
    // singletons.
    return (x == y);
  }

  /**
   * Determine the class that is returned by {@link #nullSafeGet}.
   * 
   * @return {@link Status}, the actual type returned by {@link #nullSafeGet}.
   */
  public Class returnedClass()
  {
    return Status.class;
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
   * @return the retrieved {@link Status}value, or <code>null</code>.
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
      return Status.getInstanceByName(name);
    }
    catch (java.util.NoSuchElementException e)
    {
      throw new HibernateException("Bad Status value: " + name, e);
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
      name = ((Status) value).getName();
    Hibernate.STRING.nullSafeSet(st, name, index);
  }

}