/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/SubsystemImplXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SubsystemImplXb 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubsystemImplXb", propOrder = {
    "scopeTreeId",
    "helpText",
    "categories",
    "functions",
    "choiceSets",
    "limits",
    "permissions"
})
public class SubsystemImplXb extends EntityImplXb
{
	@XmlElement(required=true)
	protected String					scopeTreeId;

	@XmlElement(required=false)
	protected String					helpText;

	@XmlElement(required=true)
	protected List<CategoryImplXb>		categories;

	@XmlElement(required=true)
	protected List<FunctionImplXb>		functions;

	@XmlElement(required=true)
	protected List<ChoiceSetImplXb>		choiceSets;

	@XmlElement(required=true)
	protected List<LimitImplXb>			limits;

	@XmlElement(required=true)
	protected List<PermissionImplXb>	permissions;

}
