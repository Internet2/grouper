/*
 * Created on Dec 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.hibernate.CompositeUserType;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;
import net.sf.hibernate.engine.SessionImplementor;
import net.sf.hibernate.type.Type;

/**
 *	TreeNode IDs are composite in nature, naming both the TreeNode
 *	and its enclosing Tree. Both parts are required to uniquely
 *	identify a TreeNode.
 *
 *	This code was copied from example code in "Hibernate in Action", page
 *  206.
 */

class ChoiceFullyQualifiedIdCompositeUserType
implements CompositeUserType
{
  // The ChoiceFullyQualifiedID gets mapped to two VARCHAR
  // columns.
  private static final int[] SQL_TYPES
  	= {Types.VARCHAR, Types.VARCHAR};
  
  public int[] sqlTypes()
  {
    return SQL_TYPES;
  }
  
  public Class returnedClass()
  {
    return ChoiceFullyQualifiedId.class;
  }
  
  public boolean isMutable()
  {
    return false;
  }
  
  public Object deepCopy(Object value)
  throws HibernateException
  {
    // Since ChoiceFullyQualifiedId is immutable, this
    // method just returns its argument.
    return value;
  }
  
  public boolean equals(Object x, Object y)
  throws HibernateException
  {
    return x == null ? y == null : x.equals(y);
  }
  
  public Object nullSafeGet
  	(ResultSet					resultSet,
  	 String[]						names,
  	 SessionImplementor session,
  	 Object							owner)
  throws HibernateException, SQLException
  {
    if (resultSet.wasNull())
    {
      return null;
    }
    
    String choiceSetId = resultSet.getString(names[0]);
    String choiceValue = resultSet.getString(names[1]);
    
    if ((choiceSetId == null) && (choiceValue == null))
    {
      return null;
    }

    return new ChoiceFullyQualifiedId(choiceSetId, choiceValue);
  }
  
  public void nullSafeSet
  	(PreparedStatement 	statement,
  	 Object							value,
  	 int								index,
  	 SessionImplementor	session)
  throws HibernateException, SQLException
  {
    ChoiceFullyQualifiedId cfqId
    	= (ChoiceFullyQualifiedId)value;
    
    String choiceSetId;
    String choiceValue;
    
    if (cfqId == null)
    {
      choiceSetId = null;
      choiceValue = null;
    }
    else
    {
      choiceSetId = cfqId.getChoiceSetId();
      choiceValue = cfqId.getChoiceValue();
    }
    
    Hibernate.STRING.nullSafeSet(statement, choiceSetId, index);
    Hibernate.STRING.nullSafeSet(statement, choiceValue, index+1);
  }
  
  public String[] getPropertyNames()
  {
    return  new String[] { "choiceSetId", "treeNodeId" };
  }
  
  public Type[] getPropertyTypes()
  {
    return new Type[] { Hibernate.STRING, Hibernate.STRING };
  }
  
  public Object getPropertyValue
  	(Object component,
  	 int		property)
  throws HibernateException
  {
    ChoiceFullyQualifiedId cfqId
    	= (ChoiceFullyQualifiedId)component;
    
    if (property == 0)
    {
      return cfqId.getChoiceSetId();
    }
    else
    {
      return cfqId.getChoiceValue();
    }
  }
  
  public void setPropertyValue
  	(Object component,
  	 int		property,
  	 Object	value)
  throws HibernateException
  {
    throw new UnsupportedOperationException
    	("ChoiceFullyQualifiedId is an immutable class.");
  }
  
  public Object assemble
  	(Serializable				cached,
  	 SessionImplementor session,
  	 Object							owner)
  throws HibernateException
  {
   return cached; 
  }
  
  public Serializable disassemble
  	(Object							value,
  	 SessionImplementor session)
  {
    return (Serializable) value;
  }
}
