/*
 * Created on Dec 15, 2004
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
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TreeNodeRelationshipCompositeUserType
implements CompositeUserType
{
  // The TreeNodeRelationship gets mapped to three VARCHAR
  // columns.
  private static final int[] SQL_TYPES
  	= {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
  
  public int[] sqlTypes()
  {
    return SQL_TYPES;
  }
  
  public Class returnedClass()
  {
    return TreeNodeRelationship.class;
  }
  
  public boolean isMutable()
  {
    return false;
  }
  
  public Object deepCopy(Object value)
  throws HibernateException
  {
    // Since TreeNodeRelationship is immutable, this
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
    
    String treeId = resultSet.getString(names[0]);
    String childNodeId = resultSet.getString(names[1]);
    String parentNodeId = resultSet.getString(names[2]);
    
    if ((treeId == null)
        && (childNodeId == null)
        && (parentNodeId == null))
    {
      return null;
    }

    return new TreeNodeRelationship
    	(treeId, childNodeId, parentNodeId);
  }
  
  public void nullSafeSet
  	(PreparedStatement 	statement,
  	 Object							value,
  	 int								index,
  	 SessionImplementor	session)
  throws HibernateException, SQLException
  {
    TreeNodeRelationship tnr
    	= (TreeNodeRelationship)value;
    
    String treeId;
    String childNodeId;
    String parentNodeId;
    
    if (tnr == null)
    {
      treeId = null;
      childNodeId = null;
      parentNodeId = null;
    }
    else
    {
      treeId = tnr.getTreeId();
      childNodeId = tnr.getChildNodeId();
      parentNodeId = tnr.getParentNodeId();
    }
    
    Hibernate.STRING.nullSafeSet(statement, treeId, index);
    Hibernate.STRING.nullSafeSet(statement, childNodeId, index+1);
    Hibernate.STRING.nullSafeSet(statement, parentNodeId, index+2);
  }
  
  public String[] getPropertyNames()
  {
    return  new String[] { "treeId", "childNodeId", "parentNodeId" };
  }
  
  public Type[] getPropertyTypes()
  {
    return new Type[]
      { Hibernate.STRING,
        Hibernate.STRING,
        Hibernate.STRING };
  }
  
  public Object getPropertyValue
  	(Object component,
  	 int		property)
  throws HibernateException
  {
    TreeNodeRelationship tnr
    	= (TreeNodeRelationship)component;
    
    switch (property)
    {
      case 0:
        return tnr.getTreeId();
      case 1:
        return tnr.getChildNodeId();
      default:
        return tnr.getParentNodeId();
    }
  }
  
  public void setPropertyValue
  	(Object component,
  	 int		property,
  	 Object	value)
  throws HibernateException
  {
    throw new UnsupportedOperationException
    	("TreeNodeFullyQualifiedId is an immutable class.");
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