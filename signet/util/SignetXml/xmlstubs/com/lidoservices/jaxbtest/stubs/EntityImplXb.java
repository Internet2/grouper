/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/EntityImplXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $
Copyright (c) 2007 Lido Services Corp.
*/
package com.lidoservices.jaxbtest.stubs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * EntityImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EntityImplXb", propOrder = {
		"comment",
		"createDatetime",
		"modifyDatetime",
		"createDbAccount",
		"modifyDbAccount",
		"createContext",
		"modifyContext",
		"createUserID",
		"modifyUserID"
})
public abstract class EntityImplXb
{
	/** A String-based ID, overridden by GrantableImplXb */
	@XmlAttribute(name="id", required=true)
	protected String	id;	// see GrantableImplXb, has an Integer id defined

	/** The name of this EntityImplXb */
	@XmlAttribute
	protected String	name;

	/** The status (ACTIVE | INACTIVE | PENDING) of this EntityImplXb */
	@XmlAttribute(name="status")
	protected String	status;

	/** A comment for the use of metadata maintainers. */
	@XmlElement(required=false)
	protected String	comment;

	/** The date and time this entity was first created. */
	@XmlElement(required=true)
	protected String	createDatetime;

	/** The date and time this entity was last modified. */
	@XmlElement(required=false)
	protected String	modifyDatetime;

	/** The account which created this entity. */
	@XmlElement(required=false)
	protected String	createDbAccount;

	/** The database account which last modified this entity. */
	@XmlElement(required=false)
	protected String	modifyDbAccount;

	/** The application program responsible for this entity's creation. */
	@XmlElement(required=false)
	protected String	createContext;

	/** The application program responsible for this entity's last modification. */
	@XmlElement(required=false)
	protected String	modifyContext;

	/** The user or program that originally generated this entity. */
	@XmlElement(required=false)
	protected String	createUserID;

	/** The user or program that last modified this entity. */
	@XmlElement(required=false)
	protected String	modifyUserID;

}
