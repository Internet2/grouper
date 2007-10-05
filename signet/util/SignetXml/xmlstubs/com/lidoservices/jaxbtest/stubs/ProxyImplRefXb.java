/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/ProxyImplRefXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * ProxyImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProxyImplRefXb", propOrder = {
})
public class ProxyImplRefXb
{
	/** A String-based ID */
	@XmlAttribute(name="id", required=true)
	protected String	id;	// see GrantableImplXb, has an Integer id defined

	/** The name of this EntityImplXb */
	@XmlAttribute(name="name", required=false)
	protected String	name;

	/** The status (ACTIVE | INACTIVE | PENDING) of this EntityImplXb */
	@XmlAttribute(name="status", required=true)
	protected String	status;

	@XmlAttribute(required=true)
	protected boolean	canExtend;

	@XmlAttribute(required=true)
	protected boolean	canUse;

}
