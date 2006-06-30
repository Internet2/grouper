/*--
$Id: EntityImpl.java,v 1.10 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $
 
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

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Every Signet entity contains an entity of this abstract class, which
 * ensures that every Signet entity has its full complement of common
 * attributes.
 * 
 */
public abstract class EntityImpl implements Entity, Name
{
  private Signet				signet;
  private String 				id;
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
   * @param signet
   * 		The Signet instance thats associated with thie EntityImpl.
   * @param id
   *		A short mnemonic id which will appear in XML documents and
   *    other documents used by analysts.
   * @param name
   *    A descriptive name which will appear in UIs and documents
   *    exposed to users.
   * @param status
   * 		The {@link Status} of this EntityImpl.
   */
  EntityImpl
  (Signet signet,
   String id,
   String name,
   Status status)
  {
    super();
    this.signet = signet;
    this.id = id;
    this.name = name;
    this.status = status;
  }
  
  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  EntityImpl()
  {
    super();
  }
  
  /**
   * @return Returns a short mnemonic id which will appear in XML
   * 		documents and other documents used by analysts.
   */
  public String getStringId()
  {
    return id;
  }
  
  /**
   * @param id The id to set.
   */
  final void setStringId(String id)
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
  public final Date getCreateDatetime()
  {
    return this.createDatetime;
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
  final Date getModifyDatetime()
  {
    return this.modifyDatetime;
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
   * @param createDatetime The createDatetime to set.
   */
  final void setCreateDatetime(Date createDatetime)
  {
    this.createDatetime = createDatetime;
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
   * @param modifyDatetime The modifyDatetime to set.
   */
  final void setModifyDatetime(Date modifyDatetime)
  {
    this.modifyDatetime = modifyDatetime;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#setCreateUserID(java.lang.String)
   */
  final void setCreateUserID(String userID)
  {
    this.createUserID = userID;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#setModifyUserID(java.lang.String)
   */
  final void setModifyUserID(String userID)
  {
    this.modifyUserID = userID;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#getCreateUserID()
   */
  final String getCreateUserID()
  {
    return this.createUserID;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#getModifyUserID()
   */
  final String getModifyUserID()
  {
    return this.modifyUserID;
  }
  
  /**
   * @param comment A comment for the use of metadata maintainers.
   */
  final void setComment(String comment)
  {
    this.comment = comment;
  }
  
  /**
   * @return A comment for the use of metadata maintainers.
   */
  final String getComment()
  {
    return this.comment;
  }
  
  /**
   * @return Returns the status.
   */
  public final Status getStatus()
  {
    return status;
  }
  
  /**
   * @param status The status to set.
   */
  public final void setStatus(Status status)
  {
    this.status = status;
  }
  
  /**
   * @param name The name to set.
   */
  final void setName(String name)
  {
    this.name = name;
  }
  
  /**
   * @return A brief description of this entity. The exact details
   * 		of the representation are unspecified and subject to change.
   */
  public String toString()
  {
    return 
    	new 
    		ToStringBuilder(this)
    			.append("id", getStringId())
    				.append("status", getStatus())
    				.append("createDatetime", getCreateDatetime())
    				.append("modifyDatetime", getModifyDatetime())
    				.toString();
  }
  
  /**
   * @return Returns the Signet instance associated with this
   * EntityImpl.
   */
  final Signet getSignet()
  {
    return this.signet;
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
  
  public void save()
  {
    setModifyDatetime(new Date());
    getSignet().getPersistentDB().save(this);
  }
}
