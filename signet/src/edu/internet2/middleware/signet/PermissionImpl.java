/*--
  $Id: PermissionImpl.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.subject.Subject;

/**
 * PermissionImpl describes an application-level action that a {@link Subject}may
 * be allowed to perform.
 * 
 */
/* Hibernate requires this class to be non-final. */

class PermissionImpl
implements Permission
{
  private String				id;
  private Subsystem			subsystem;
  private Set						functions;
  private Housekeeping	housekeeping;
    
    /**
     * Hibernate requires that each persistable entity have a default
     * constructor.
     */
    public PermissionImpl()
    {
        super();
        this.functions = new HashSet();
        this.housekeeping = new Housekeeping();
    }

    /**
     * @param id
     *            A short mnemonic id which will appear in XML documents and
     *            other documents used by analysts.
     */
    PermissionImpl
    	(String 		id,
    	 Subsystem 	subsystem)
    {
      	super();
        this.id = id;
        this.subsystem = subsystem;
        this.functions = new HashSet();
        this.housekeeping = new Housekeeping();
    }
    
    /**
     * @return Returns a short mnemonic id which will appear in XML
     * 		documents and other documents used by analysts.
     */
    public String getId()
    {
      return this.id;
    }

    /**
     * @param id A short mnemonic id which will appear in XML
     * 		documents and other documents used by analysts.
     */
    public void setId(String id)
    {
      this.id = id;
    }

    /**
     * @return Returns the {@link Function}s associated with this Permission.
     */
    public Function[] getFunctionsArray()
    {
      Function[] functionsArray;
      
      if (this.functions == null)
      {
        functionsArray = new Function[0];
      }
      else
      {
        functionsArray = new Function[this.functions.size()];
        Iterator functionsIterator = this.functions.iterator();
        int i = 0;
        while (functionsIterator.hasNext())
        {
          functionsArray[i] = (Function)(functionsIterator.next());
          i++;
        }
      }
      
      return functionsArray;
    }

    /**
     * @param functions The Functions to associate with this Permission.
     */
    public void setFunctionsArray(Function[] functions)
    {
      int functionCount = (functions == null ? 0 : functions.length);
      this.functions = new HashSet(functionCount);
        
      for (int i = 0; i < functionCount; i++)
      {
        this.functions.add(functions[i]);
      }
    }
    
    /* This method exists only for use by Hibernate. */
    void setFunctions(Set functions)
    {
      this.functions = functions;
    }

    /* This method exists only for use by Hibernate.
     */
    Set getFunctions()
    {
      return this.functions;
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

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Permission#addFunction(edu.internet2.middleware.signet.Function)
   */
  public void addFunction(Function function)
  {
    // Do we have this Function already? If so, just return. That
    // helps to prevent an infinite loop of adding Permissions and
    // Functions to each other.
    
    if (!(this.functions.contains(function)))
    {
      this.functions.add(function);
      function.addPermission(this);
    }
  }
  
  public Subsystem getSubsystem()
  {
    return this.subsystem;
  }
  
  // This method exists only for use by Hibernate.
  void setSubsystem(Subsystem subsystem)
  {
    this.subsystem = subsystem;
  }
}