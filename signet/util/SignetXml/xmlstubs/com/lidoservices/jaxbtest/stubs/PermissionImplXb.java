/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/PermissionImplXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp.
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * PermissionImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PermissionImplXb", propOrder = {
		"subsystemId",
		"limits"
})
public class PermissionImplXb extends EntityImplXb
{
  // This field is a simple synthetic key for this record in the database.
	@XmlAttribute(name="permissionPK", required=true)
	protected Integer			key;

	@XmlElement(name="subsystemId", required=true)
	protected String			subsystemId;

	@XmlElement(name="limits", required=false)
	protected List<LimitImplXb>	limits;

}
