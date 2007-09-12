/*--
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/EntityImpl.java,v 1.20 2007-09-12 15:41:57 ddonn Exp $

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

import java.util.Calendar;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Every Signet entity contains an entity of this abstract class, which
 * ensures that every Signet entity has its full complement of common
 * attributes.
 * 
 * NOTE: This class and all it's subclasses should be 
 * rearchitected to better handle the notion of "id". GrantableImpl and it's 
 * subclasses use an integer-based id whereas EntityImpl uses a String-based id. 
 */
public abstract class EntityImpl implements Entity, Name
{
	protected Log				log;

  protected Signet				signet;
  private String 				id; // see GrantableImpl, has an Integer id defined
  private String 				name;
  private Status 				status;
  
  /* A comment for the use of metadata maintainers. */
  private String comment;
  
  /* The date and time this entity was first created. */
  private Date	createDatetime;
  
  /* The account which created this entity. */ 
  private String	createDbAccount;
  
  /* The application program responsible for this entity's creation. */
  private String	createContext;
  
  /* The date and time this entity was last modified. */
  private Date	modifyDatetime;
  
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
	 * Hibernate requires that each persistable entity have a default constructor.
	 */
	protected EntityImpl()
	{
		log = LogFactory.getLog(this.getClass());
		this.id = null;
		setCreateDatetime(Calendar.getInstance().getTime());
	}

	/**
	 * @param signet The Signet instance thats associated with thie EntityImpl.
	 * @param id A short mnemonic id which will appear in XML documents and other documents used by analysts.
	 * @param name A descriptive name which will appear in UIs and documents exposed to users.
	 * @param status The {@link Status} of this EntityImpl.
	 */
	protected EntityImpl(Signet signet, String id, String name, Status status)
	{
		this();
		this.signet = signet;
		this.id = id;
		this.name = name;
		this.status = status;
	}


  /**
	 * @return Returns a short mnemonic id which will appear in XML documents and other documents used by analysts.
	 */
  public String getStringId()
  {
    return id;
  }
  
  /**
   * @param id The id to set.
   */
  public void setStringId(String id)
  {
    if ((this.id != null) && !(this.id.equals(id)))
    {
      throw new IllegalStateException
      ("Once a Signet entity has its ID assigned, it is illegal to"
        + " attempt to change that ID. This entity already has the ID"
        + "'" 
        + this.id
        + "', and there was an attempt to change that ID to '"
        + id
        + ".");
    }
    
    this.id = id;
  }
  
  /**
   * @return Returns a descriptive name which will appear in UIs and
   * 		documents exposed to users.
   */
  public String getName()
  {
    return name;
  }
  
  /**
   * @return Returns the account which created this entity.
   */
  public String getCreateDbAccount()
  {
    return (createDbAccount);
  }
  
  /**
   * @return Returns the source of this entity's creation.
   */
  public String getCreateContext()
  {
    return (createContext);
  }
  
  /**
   * @return Returns the date and time this entity was first created.
   */
  public Date getCreateDatetime()
  {
    return this.createDatetime;
  }
  
  /**
   * @return Returns the account which last modified this entity.
   */
  public String getModifyDbAccount()
  {
    return (modifyDbAccount);
  }
  
  /**
   * @return Returns the source of this entity's last modification.
   */
  public String getModifyContext()
  {
    return (modifyContext);
  }
  
  /**
   * @return Returns the date and time this entity was last modified.
   */
  public Date getModifyDatetime()
  {
    return (modifyDatetime);
  }
  
  /**
   * @param createDbAccount The createDbAccount to set.
   */
  public void setCreateDbAccount(String createDbAccount)
  {
    this.createDbAccount = createDbAccount;
  }
  
  /**
   * @param createContext The createContext to set.
   */
  public void setCreateContext(String createContext)
  {
    this.createContext = createContext;
  }
  
  /**
   * @param createDatetime The createDatetime to set.
   */
  public void setCreateDatetime(Date createDatetime)
  {
    this.createDatetime = createDatetime;
  }
  
  /**
   * @param modifyDbAccount The modifyDbAccount to set.
   */
  public void setModifyDbAccount(String modifyDbAccount)
  {
    this.modifyDbAccount = modifyDbAccount;
  }
  
  /**
   * @param modifyContext The modifyContext to set.
   */
  public void setModifyContext(String modifyContext)
  {
    this.modifyContext = modifyContext;
  }
  
  /**
   * @param modifyDatetime The modifyDatetime to set.
   */
  public void setModifyDatetime(Date modifyDatetime)
  {
    this.modifyDatetime = modifyDatetime;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#setCreateUserID(java.lang.String)
   */
  public void setCreateUserID(String userID)
  {
    this.createUserID = userID;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#setModifyUserID(java.lang.String)
   */
  public void setModifyUserID(String userID)
  {
    this.modifyUserID = userID;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#getCreateUserID()
   */
  public String getCreateUserID()
  {
    return this.createUserID;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#getModifyUserID()
   */
  public String getModifyUserID()
  {
    return this.modifyUserID;
  }
  
  /**
   * @param comment A comment for the use of metadata maintainers.
   */
  public void setComment(String comment)
  {
    this.comment = comment;
  }
  
  /**
   * @return A comment for the use of metadata maintainers.
   */
  public String getComment()
  {
    return this.comment;
  }
  
  /**
   * @return Returns the status as defined in Status (ACTIVE, INACTIVE, PENDING).
   */
  public Status getStatus()
  {
    return status;
  }
  
  /**
   * @param status The status to set.
   * @return True if status was changed, otherwise false
   */
  public boolean setStatus(Status status)
  {
	boolean retval = true; // assume success

	if (null != this.status)
	{
		if (retval = !this.status.equals(status)) // yes, I do mean "="
			this.status = status;
	}
	else
		this.status = status;

	return (retval);
  }
  
  /**
   * @param name The name to set.
   */
  public void setName(String name)
  {
    this.name = name;
  }
  
  
  /**
   * @return Returns the Signet instance associated with this EntityImpl.
   */
  public Signet getSignet()
  {
    return (signet);
  }
  
  /**
   * Stows a handy Signet reference into this object.
   * 
   * @param signet The Signet instance associated with this EntityImpl.
   */
  public void setSignet(Signet signet)
  {
    if (signet != null)
    {
      this.signet = signet;
    }
  }


	/////////////////////////////////////////
	// overrides Object
	/////////////////////////////////////////

	public String toString()
	{
		StringBuffer buf = new StringBuffer();

		buf.append("[EntityImpl: "); //$NON-NLS-1$
		buf.append("name=" + name); //$NON-NLS-1$
		buf.append(", id(EntityImpl)=" + id); //$NON-NLS-1$
		buf.append(", status=" + status.toString()); //$NON-NLS-1$
		buf.append(", comment=\"" + comment + "\""); //$NON-NLS-1$ $NON-NLS-2$
		buf.append(", createDate=" + (null != createDatetime ? createDatetime.toString() : "<null>")); //$NON-NLS-1$ $NON-NLS-2$
		buf.append(", modifyDate=" + (null != modifyDatetime ? modifyDatetime.toString() : "<null>")); //$NON-NLS-1$ $NON-NLS-2$
		buf.append(", createDbAccount=" + createDbAccount); //$NON-NLS-1$
		buf.append(", createContext=" + createContext); //$NON-NLS-1$
		buf.append(", modifyDbAccount=" + modifyDbAccount); //$NON-NLS-1$
		buf.append(", modifyContext=" + modifyContext); //$NON-NLS-1$
		buf.append(", createUserID=" + createUserID); //$NON-NLS-1$
		buf.append(", modifyUserID=" + modifyUserID); //$NON-NLS-1$
		buf.append("]"); //$NON-NLS-1$

		return (buf.toString());
	}

}
