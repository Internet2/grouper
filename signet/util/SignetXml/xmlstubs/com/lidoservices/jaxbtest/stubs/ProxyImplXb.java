/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/ProxyImplXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * ProxyImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProxyImplXb", propOrder = {
		"subsystemId"
})
public class ProxyImplXb extends GrantableImplXb
{
	@XmlElement(name="subsystemId", required=true)
	protected String	subsystemId;

	@XmlAttribute(required=true)
	protected boolean	canExtend;

	@XmlAttribute(required=true)
	protected boolean	canUse;

}
