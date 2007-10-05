/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/ChoiceSetImplXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp.
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * ChoiceSetImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChoiceSetImplXb", propOrder = {
    "subsystemId",
    "modifyDatetime",
    "choices"
})
public class ChoiceSetImplXb
{
	@XmlAttribute(name="choiceSet_PK", required=true)
	protected Integer				key;

	@XmlAttribute(name="choiceSetId", required=true)
	protected String				id;

	@XmlElement(name="subsystemId", required=true)
	protected String				subsystemId;

	@XmlElement(name="choices", required=false)
	protected List<ChoiceImplXb>	choices;

	@XmlAttribute(name="adapterClassName", required=false)
	protected String				adapterClassName;

	@XmlElement(name="modifyDatetime", required=false)
	protected String				modifyDatetime;
}
