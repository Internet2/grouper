/*--
  $Id: SubjectTypeImpl.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.lang.reflect.Constructor;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.SubjectTypeAdapter;

/* These are the columns in the SubjectType table in the database:
 * 
 * subjectTypeID
 * name
 * adapterClass
 * createDatetime
 * createDbAccount
 * createUserID
 * createContext
 * modifyDatetime
 * modifyDbAccount
 * modifyUserID
 * modifyContext
 * comment
 */
/* Hibernate requires this class to be non-final. */

class SubjectTypeImpl implements SubjectType
{
  private Signet							signet;
  private String 							id;
  private String 							name;
  private SubjectTypeAdapter 	adapter;
  private Housekeeping				housekeeping;

  public SubjectTypeImpl()
  {
    super();
    this.housekeeping = new Housekeeping();
  }
  
  public SubjectTypeImpl
  	(Signet							signet,
  	 String 						id,
  	 String 						name,
  	 SubjectTypeAdapter adapter)
  {
    this.signet = signet;
    this.id = id;
    this.name = name;
    this.adapter = adapter;
    this.housekeeping = new Housekeeping();
    
    if (this.adapter instanceof SubjectTypeAdapterImpl)
    {
      ((SubjectTypeAdapterImpl)(this.adapter)).setSignet(this.signet);
    }
  }
  
  void setSignet(Signet signet)
  {
    this.signet = signet;
    
    if (this.adapter instanceof SubjectTypeAdapterImpl)
    {
      ((SubjectTypeAdapterImpl)(this.adapter)).setSignet(this.signet);
    }
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.subject.SubjectType#getId()
   */
  public String getId()
  {
    return this.id;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.subject.SubjectType#getName()
   */
  public String getName()
  {
    return this.name;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.subject.SubjectType#getAdapter()
   */
  public SubjectTypeAdapter getAdapter()
  {
    if (this.adapter instanceof SubjectTypeAdapterImpl)
    {
      ((SubjectTypeAdapterImpl)(this.adapter)).setSignet(this.signet);
    }
    
    return this.adapter;
  }
  
  void setId(String id)
  {
    this.id = id;
  }
  
  void setName(String name)
  {
    this.name = name;
  }

  void setAdapter(SubjectTypeAdapter adapter)
  {
    this.adapter = adapter;
    
    if (this.adapter instanceof SubjectTypeAdapterImpl)
    {
      ((SubjectTypeAdapterImpl)(this.adapter)).setSignet(this.signet);
    }
  }
  
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return this.id;
  }
  
  void setAdapterClassName(String name)
  {
    Class clazz = null;
    
    try
    {
      clazz = Class.forName(name);
    }
    catch (ClassNotFoundException cnfe)
    {
      throw new SignetRuntimeException
      	("A PrivilegedSubject in the Signet database refers to a Subject"
      	 + " whose SubjectTypeAdapter is implemented by the class named '"
      	 + name
      	 + "'. This class cannot be found in Signet's classpath.",
      	 cnfe);
    }
    
    Class[] noParams = new Class[0];
    Constructor constructor;
    
    try
    {
      constructor = clazz.getConstructor(noParams);
    }
    catch (NoSuchMethodException nsme)
    {
      throw new SignetRuntimeException
    	("A PrivilegedSubject in the Signet database refers to a Subject"
    	 + " whose SubjectTypeAdapter is implemented by the class named '"
    	 + name
    	 + "'. This class is in Signet's classpath, but it does not provide"
    	 + " a default, parameterless constructor.",
    	 nsme);
    }
    
    try
    {
      this.adapter = (SubjectTypeAdapter)(constructor.newInstance(noParams));
    }
    catch (Exception e)
    {
      throw new SignetRuntimeException
    	("A PrivilegedSubject in the Signet database refers to a Subject"
    	 + " whose SubjectTypeAdapter is implemented by the class named '"
    	 + name
    	 + "'. This class is in Signet's classpath, and it does provide"
    	 + " a default, parameterless constructor, but Signet did not succeed"
    	 + " in invoking that constructor.",
    	 e);
    }
    
    if (this.adapter instanceof SubjectTypeAdapterImpl)
    {
      ((SubjectTypeAdapterImpl)(this.adapter)).setSignet(this.signet);
    }
  }
  
  String getAdapterClassName()
  {
    return this.adapter.getClass().getName();
  }
  /**
   * @return
   */
  String getComment()
  {
    return this.housekeeping.getComment();
  }
  /**
   * @return
   */
  String getCreateContext()
  {
    return this.housekeeping.getCreateContext();
  }
  /**
   * @return
   */
  Date getCreateDateTime()
  {
    return this.housekeeping.getCreateDateTime();
  }
  /**
   * @return
   */
  String getCreateDbAccount()
  {
    return this.housekeeping.getCreateDbAccount();
  }
  /**
   * @return
   */
  String getCreateUserID()
  {
    return this.housekeeping.getCreateUserID();
  }
  /**
   * @return
   */
  String getModifyContext()
  {
    return this.housekeeping.getModifyContext();
  }
  /**
   * @return
   */
  Date getModifyDateTime()
  {
    return this.housekeeping.getModifyDateTime();
  }
  /**
   * @return
   */
  String getModifyDbAccount()
  {
    return this.housekeeping.getModifyDbAccount();
  }
  /**
   * @return
   */
  String getModifyUserID()
  {
    return this.housekeeping.getModifyUserID();
  }
  /**
   * @param comment
   */
  void setComment(String comment)
  {
    this.housekeeping.setComment(comment);
  }
  /**
   * @param createContext
   */
  void setCreateContext(String createContext)
  {
    this.housekeeping.setCreateContext(createContext);
  }
  /**
   * @param createDateTime
   */
  void setCreateDateTime(Date createDateTime)
  {
    this.housekeeping.setCreateDateTime(createDateTime);
  }
  /**
   * @param createDbAccount
   */
  void setCreateDbAccount(String createDbAccount)
  {
    this.housekeeping.setCreateDbAccount(createDbAccount);
  }
  /**
   * @param userID
   */
  void setCreateUserID(String userID)
  {
    this.housekeeping.setCreateUserID(userID);
  }
  /**
   * @param modifyContext
   */
  void setModifyContext(String modifyContext)
  {
    this.housekeeping.setModifyContext(modifyContext);
  }
  /**
   * @param modifyDateTime
   */
  void setModifyDateTime(Date modifyDateTime)
  {
    this.housekeeping.setModifyDateTime(modifyDateTime);
  }
  /**
   * @param modifyDbAccount
   */
  void setModifyDbAccount(String modifyDbAccount)
  {
    this.housekeeping.setModifyDbAccount(modifyDbAccount);
  }
  /**
   * @param userID
   */
  void setModifyUserID(String userID)
  {
    this.housekeeping.setModifyUserID(userID);
  }
  
  public boolean equals(Object obj)
  {
    if ( !(obj instanceof SubjectTypeImpl) )
    {
      return false;
    }
    
    SubjectTypeImpl rhs = (SubjectTypeImpl) obj;
    return new EqualsBuilder()
                    .append(this.getId(), rhs.getId())
                    .isEquals();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37).   
       append(this.getId())
       .toHashCode();
  }

  /**
   * @return
   */
  public Signet getSignet()
  {
    return this.signet;
  }
}
