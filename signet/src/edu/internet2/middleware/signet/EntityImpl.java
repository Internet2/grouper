/*--
$Id: EntityImpl.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
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
abstract class EntityImpl implements Entity, Name
{
private String 			id;
private String 			name;
private Status 			status;
private Housekeeping 	housekeeping;
  
  /**
   * @param id
   *            A short mnemonic id which will appear in XML documents and
   *            other documents used by analysts.
   * @param name
   *            A descriptive name which will appear in UIs and documents
   *            exposed to users.
   * @param description
   *            A prose description which will appear in help-text and other
   *            explanatory materials.
   * @param status
   * 			The {@link Status} of this EntityImpl.
   */
  public EntityImpl
  	(String id,
  	 String name,
  	 Status status)
  {
      super();
      this.id = id;
      this.name = name;
      this.status = status;
      this.housekeeping = new Housekeeping();
  }
  
  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public EntityImpl()
  {
     super(); 
     this.housekeeping = new Housekeeping();
  }

  /**
   * @return Returns a short mnemonic id which will appear in XML
   * 		documents and other documents used by analysts.
   */
public final String getId()
{
	return id;
}

/**
 * @param id The id to set.
 */
final void setId(String id)
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
public final String getName() {
	return name;
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
public Date getCreateDatetime()
{
  return this.housekeeping.getCreateDatetime();
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
String getCreateContext()
{
  return this.housekeeping.getCreateContext();
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
Date getModifyDatetime()
{
  return this.housekeeping.getModifyDatetime();
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
String getModifyContext()
{
  return this.housekeeping.getModifyContext();
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
 * @param createDatetime
 */
void setCreateDatetime(Date createDatetime)
{
  this.housekeeping.setCreateDatetime(createDatetime);
}
/**
 * @param createDbAccount
 */
void setCreateDbAccount(String createDbAccount)
{
  this.housekeeping.setCreateDbAccount(createDbAccount);
}
/**
 * @param createContext
 */
void setCreateContext(String createContext)
{
  this.housekeeping.setCreateContext(createContext);
}
/**
 * @param userID
 */
void setCreateUserID(String userID)
{
  this.housekeeping.setCreateUserID(userID);
}
/**
 * @param modifyDatetime
 */
void setModifyDatetime(Date modifyDatetime)
{
  this.housekeeping.setModifyDatetime(modifyDatetime);
}
/**
 * @param modifyDbAccount
 */
void setModifyDbAccount(String modifyDbAccount)
{
  this.housekeeping.setModifyDbAccount(modifyDbAccount);
}
/**
 * @param modifyContext
 */
void setModifyContext(String modifyContext)
{
  this.housekeeping.setModifyContext(modifyContext);
}
/**
 * @param userID
 */
void setModifyUserID(String userID)
{
  this.housekeeping.setModifyUserID(userID);
}
/**
 * @return Returns the status.
 */
public Status getStatus() {
	return status;
}

/**
 * @param status The status to set.
 */
public final void setStatus(Status status) {
	this.status = status;
}

/**
 * @param name The name to set.
 */
public final void setName(String name) {
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
    			.append("id", getId())
    			.append("status", getStatus())
    			.append("createDatetime", getCreateDatetime())
    			.append("modifyDatetime", getModifyDatetime())
    			.toString();
  }
}
