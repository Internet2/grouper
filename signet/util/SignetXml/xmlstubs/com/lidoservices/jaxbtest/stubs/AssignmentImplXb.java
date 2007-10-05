/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/AssignmentImplXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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
@XmlType(name = "AssignmentImplXb", propOrder = {
    "limitValues"
})
public class AssignmentImplXb extends GrantableImplXb
{
	@XmlAttribute(name="scope", required=true)
	protected String				scopePath;

	@XmlAttribute(name="function", required=true)
	protected String				functionId;

	@XmlAttribute(name="subsystem", required=true)
	protected String				subsystemId;

    @XmlElement(name="LimitValue")
	protected Set<LimitValueXb>		limitValues;

    @XmlAttribute(name="canGrant", required=true)
	protected boolean				canGrant;

    @XmlAttribute(name="canUse", required=true)
	protected boolean				canUse;

}
