/*--
  $Id: Housekeeping.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;

/**
 *
 * This interface specifies some common methods that every 
 * Signet entity must provide. These methods, however, are
 * only for the use of Signet debuggers and maintainers, not
 * Signet application programs.
 * 
 * I removed these methods from the {@link Entity} interface to reduce
 * the clutter in the public APIs of each Signet entity.
 */
class Housekeeping
{
	/* A comment for the use of metadata maintainers. */
	private String comment;
    /* The date and time this entity was first created. */
    private Date	createDateTime;
    /* The account which created this entity. */ 
    private String	createDbAccount;
    /* The application program responsible for this entity's creation. */
    private String	createContext;
    /* The date and time this entity was last modified. */
    private Date	modifyDateTime;
    /* The database account which last modified this entity. */
    private String	modifyDbAccount;
    /* The application program responsible for this entity's last
     * modification. */
    private String	modifyContext;
    /* The end-user who was logged in to the GUI or other application
     * program that originally generated this entity.
     */
    private String 	createUserID;
    /* The end-user who was logged in to the GUI or other application
     * program that last modified this entity.
     */
    private String 	modifyUserID;
    

    /**
     * Hibernate requires that each persistable entity have a default
     * constructor.
     */
    Housekeeping()
    {
       super(); 
    }

    /**
     * @return Returns the account which created this entity.
     */
    final String getCreateDbAccount()
    {
        return this.createDbAccount;
    }
    
    /**
     * @return Returns the source of this entity's creation.
     */
    final String getCreateContext()
    {
        return this.createContext;
    }
    
    /**
     * @return Returns the date and time this entity was first created.
     */
    final Date getCreateDateTime()
    {
        return this.createDateTime;
    }
    
    /**
     * @return Returns the account which last modified this entity.
     */
    final String getModifyDbAccount()
    {
        return this.modifyDbAccount;
    }
    /**
     * @return Returns the source of this entity's last modification.
     */
    final String getModifyContext()
    {
        return this.modifyContext;
    }
    /**
     * @return Returns the date and time this entity was last modified.
     */
    final Date getModifyDateTime()
    {
        return this.modifyDateTime;
    }
    
    
    /**
     * @param createDbAccount The createDbAccount to set.
     */
    final void setCreateDbAccount(String createDbAccount)
    {
        this.createDbAccount = createDbAccount;
    }
    /**
     * @param createContext The createContext to set.
     */
    final void setCreateContext(String createContext)
    {
        this.createContext = createContext;
    }
    /**
     * @param createDateTime The createDateTime to set.
     */
    final void setCreateDateTime(Date createDateTime)
    {
        this.createDateTime = createDateTime;
    }
    /**
     * @param modifyDbAccount The modifyDbAccount to set.
     */
    final void setModifyDbAccount(String modifyDbAccount)
    {
        this.modifyDbAccount = modifyDbAccount;
    }
    /**
     * @param modifyContext The modifyContext to set.
     */
    final void setModifyContext(String modifyContext)
    {
        this.modifyContext = modifyContext;
    }
    
    /**
     * @param modifyDateTime The modifyDateTime to set.
     */
    final void setModifyDateTime(Date modifyDateTime)
    {
        this.modifyDateTime = modifyDateTime;
    }
    
    /* (non-Javadoc)
     * @see edu.internet2.middleware.signet.Entity#setCreateUserID(java.lang.String)
     */
    void setCreateUserID(String userID)
    {
        this.createUserID = userID;
    }

    /* (non-Javadoc)
     * @see edu.internet2.middleware.signet.Entity#setModifyUserID(java.lang.String)
     */
    void setModifyUserID(String userID)
    {
        this.modifyUserID = userID;
    }

    /* (non-Javadoc)
     * @see edu.internet2.middleware.signet.Entity#getCreateUserID()
     */
    String getCreateUserID()
    {
        return this.createUserID;
    }

    /* (non-Javadoc)
     * @see edu.internet2.middleware.signet.Entity#getModifyUserID()
     */
    String getModifyUserID()
    {
        return this.modifyUserID;
    }
    
    /**
     * @param comment A comment for the use of metadata maintainers.
     */
    void setComment(String comment)
    {
        this.comment = comment;
    }
    
    /**
     * @return A comment for the use of metadata maintainers.
     */
    String getComment()
    {
        return this.comment;
    }

}
