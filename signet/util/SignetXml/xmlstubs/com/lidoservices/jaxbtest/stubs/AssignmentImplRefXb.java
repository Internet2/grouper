/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/AssignmentImplRefXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp.
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * AssignmentImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssignmentImplRefXb", propOrder = {
})
public class AssignmentImplRefXb
{
	/** A String-based ID, overridden by GrantableImplXb */
	@XmlAttribute(name="id", required=true)
	protected String	id;	// see GrantableImplXb, has an Integer id defined

	/** The name of this EntityImplXb */
	@XmlAttribute(name="name", required=false)
	protected String	name;

	/** The status (ACTIVE | INACTIVE | PENDING) of this EntityImplXb */
	@XmlAttribute(name="status", required=false)
	protected String	status;

    @XmlAttribute(name="canGrant", required=true)
	protected boolean	canGrant;

    @XmlAttribute(name="canUse", required=true)
	protected boolean	canUse;

}
