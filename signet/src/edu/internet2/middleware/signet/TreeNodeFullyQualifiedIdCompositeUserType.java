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

class TreeNodeFullyQualifiedIdCompositeUserType
implements CompositeUserType
{
  // The TreeNodeFullyQualifiedID gets mapped to two VARCHAR
  // columns.
  private static final int[] SQL_TYPES
  	= {Types.VARCHAR, Types.VARCHAR};
  
  public int[] sqlTypes()
  {
    return SQL_TYPES;
  }
  
  public Class returnedClass()
  {
    return TreeNodeFullyQualifiedId.class;
  }
  
  public boolean isMutable()
  {
    return false;
  }
  
  public Object deepCopy(Object value)
  throws HibernateException
  {
    // Since TreeNodeFullyQualifiedId is immutable, this
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
    String nodeId = resultSet.getString(names[1]);
    
    if ((treeId == null) && (nodeId == null))
    {
      return null;
    }

    return new TreeNodeFullyQualifiedId(treeId, nodeId);
  }
  
  public void nullSafeSet
  	(PreparedStatement 	statement,
  	 Object							value,
  	 int								index,
  	 SessionImplementor	session)
  throws HibernateException, SQLException
  {
    TreeNodeFullyQualifiedId tnfqId
    	= (TreeNodeFullyQualifiedId)value;
    
    String treeId;
    String nodeId;
    
    if (tnfqId == null)
    {
      treeId = null;
      nodeId = null;
    }
    else
    {
      treeId = tnfqId.getTreeId();
      nodeId = tnfqId.getTreeNodeId();
    }
    
    Hibernate.STRING.nullSafeSet(statement, treeId, index);
    Hibernate.STRING.nullSafeSet(statement, nodeId, index+1);
  }
  
  public String[] getPropertyNames()
  {
    return  new String[] { "treeId", "treeNodeId" };
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
    TreeNodeFullyQualifiedId tnfqId
    	= (TreeNodeFullyQualifiedId)component;
    
    if (property == 0)
    {
      return tnfqId.getTreeId();
    }
    else
    {
      return tnfqId.getTreeNodeId();
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
