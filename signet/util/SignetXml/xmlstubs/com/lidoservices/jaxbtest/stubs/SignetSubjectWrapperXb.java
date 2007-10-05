/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/SignetSubjectWrapperXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SignetSubjectWrapperXb - this class exists for the sole purpose of allowing
 * an GrantableImplXb to have a grantor, grantee, proxy, and revoker that
 * _contain_ a Subject, instead of being a Subject.
 * In other words, grantee has a Subject, instead of grantee is a Subject.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignetSubjectWrapperXb", propOrder = {
    "subject"
})
public class SignetSubjectWrapperXb
{
	@XmlElement(name="Subject", required=true)
	protected SignetSubjectRefXb subject;

	public SignetSubjectRefXb getSubject() { return (subject); }
	public void setSubject(SignetSubjectRefXb subject) { this.subject = subject; }
}
