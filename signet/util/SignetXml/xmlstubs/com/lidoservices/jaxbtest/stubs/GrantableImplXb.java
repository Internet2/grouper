/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/GrantableImplXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * GrantableImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GrantableImplXb", propOrder = {
		"effectiveDate",
		"expirationDate",
		"grantor",
		"proxy",
		"grantee",
		"revoker",
		"instanceNumber"
})
public abstract class GrantableImplXb extends EntityImplXb
{
	/** Database primary key. GrantableImplXb is unusual among Signet entities in
	 * that it has a numeric, not alphanumeric ID.
	 * Note!! Overrides/masks super.id (a dangerous practice) */
//	protected Integer		id;

	/** If this Grantable instance was granted directly by a PrivilegedSubject,
	 * then this is that PrivilegedSubject and 'proxy', below, will be null.
	 * If this Grantable instance was granted by an "acting as" Subject, then
	 * this is that "acting as" Subject and 'proxy' will be the logged-in Subject. */
	@XmlElement(name="Grantor", required=true)
	protected SignetSubjectRefXb	grantor;

	/** If this Grantable instance was granted/revoked directly by a Subject,
	 * then this is null. If this Grantable instance was granted/revoked by an
	 * "acting as" Subject, then this is the "acting as" PrivilegedSubject. */
	@XmlElement(name="ActingAs", required=false)
	protected SignetSubjectRefXb	proxy;

	/** The recipient of this grant */
	@XmlElement(name="Grantee", required=true)
	protected SignetSubjectRefXb	grantee;

	/** The revoker of this grant */
	@XmlElement(name="Revoker", required=false)
	protected SignetSubjectRefXb	revoker;

	@XmlElement(name="effectiveDate", required=true)
	protected String	    effectiveDate;

	@XmlElement(name="expirationDate", required=false)
	protected String   		expirationDate;

	@XmlElement(name="instanceNumber", required=true)
	protected int			instanceNumber;

}
