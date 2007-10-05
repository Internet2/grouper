/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/ChoiceImplXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp.
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * ChoiceImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChoiceImplXb", propOrder = {
		"value",
		"displayValue",
		"displayOrder",
		"rank",
		"modifyDatetime"
})
public class ChoiceImplXb
{
	@XmlAttribute(name="choiceId", required=true)
	protected int			key;

	@XmlElement(name="displayOrder", required=true)
	protected int			displayOrder;

	@XmlElement(name="rank", required=true)
	protected int			rank;

	@XmlElement(name="displayValue", required=false)
	protected String		displayValue;

	@XmlElement(name="value", required=true)
	protected String		value;

	@XmlElement(name="modifyDatetime", required=false)
	protected String		modifyDatetime;
}
