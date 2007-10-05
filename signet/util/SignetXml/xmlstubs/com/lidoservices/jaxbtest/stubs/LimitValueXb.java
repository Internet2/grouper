/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/LimitValueXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * LimitValueXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LimitValueXb", propOrder = {
		"limit",
		"value"
})
public class LimitValueXb
{
	@XmlElement(name="limit", required=true)
	protected LimitImplXb	limit;

//	@XmlElement(name="limitId", required=true)
//	protected String		limitId;

	@XmlElement(name="value", required=true)
	protected String		value;
}
