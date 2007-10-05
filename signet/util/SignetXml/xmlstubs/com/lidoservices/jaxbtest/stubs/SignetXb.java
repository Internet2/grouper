/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/SignetXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SignetXb 
 * 
 */
@XmlRootElement(name="SignetRoot")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="SignetXb", propOrder = {
		"scopeTrees",
		"subsystems",
		"subjects",
		"proxies",
		"assignments"
})
public class SignetXb
{
	@XmlAttribute(name="version", required=true)
	protected String					version;

	@XmlElement(name="Assignment", required=false)
	protected List<AssignmentImplXb>	assignments;

	@XmlElement(name="Proxy", required=false)
	protected List<ProxyImplXb>			proxies;

	@XmlElement(name="Subject", required=false)
	protected List<SignetSubjectXb>		subjects;

	@XmlElement(name="ScopeTree", required=false)
	protected List<ScopeTreeXb>			scopeTrees;

	@XmlElement(name="Subsystems", required=false)
	protected List<SubsystemImplXb>		subsystems;

}
