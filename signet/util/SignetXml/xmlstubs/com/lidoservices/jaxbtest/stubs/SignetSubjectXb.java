/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/SignetSubjectXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SignetSubjectXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignetSubjectXb", propOrder = {
		"actingAs",
		"assignmentsGranted",
		"assignmentsReceived",
		"proxiesGranted",
		"proxiesReceived"
})
public class SignetSubjectXb extends SignetSubjectRefXb
{
	/** A Subject may act as another Subject for the purpose of managing
	 * Proxies and Assignments. Not a Hibernate field. */
	@XmlElement(required=false)
	protected SignetSubjectRefXb		actingAs;

	/** The set of assignments granted BY this subject */
	@XmlElement(required=false)
	protected List<AssignmentImplRefXb>	assignmentsGranted;

	/** The set of assignments granted TO this subject */
	@XmlElement(required=false)
	protected List<AssignmentImplRefXb>	assignmentsReceived;

	/** The set of proxies granted BY this subject */
	@XmlElement(required=false)
	protected List<ProxyImplRefXb>		proxiesGranted;

	/** The set of proxies granted TO this subject */
	@XmlElement(required=false)
	protected List<ProxyImplRefXb>		proxiesReceived;

}
