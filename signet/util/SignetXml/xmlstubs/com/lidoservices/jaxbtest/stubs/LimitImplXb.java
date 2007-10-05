/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/LimitImplXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * LimitImplXb 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LimitImplXb", propOrder = {
	"key",
	"subsystemId",
	"dataType",
	"choiceSetId",
	"name",
	"status",
	"displayOrder",
    "limitValues"
})
public class LimitImplXb
{
	/** This field is a simple synthetic key for this record in the database. */
	@XmlElement(name="limitPK", required=true)
	protected Integer			key;

	@XmlElement(name="subsystemId", required=true)
	protected String			subsystemId;

	@XmlAttribute(name="id", required=false)
	protected String			id;

	@XmlElement(name="dataType", required=true)
	protected String			dataType;

	@XmlElement(name="choiceSetId", required=true)
	protected String			choiceSetId;

	@XmlElement(name="name", required=true)
	protected String			name;

//	protected String			helpText;
//	protected Date				modifyDatetime;

	@XmlElement(name="status", required=true)
	protected String			status;

//	protected Set				permissions;

	@XmlElement(name="displayOrder", required=true)
	protected int				displayOrder;

	@XmlElement(name="limitValues", required=false)
	protected List<LimitValueXb>	limitValues;

}
