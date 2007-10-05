/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/SignetSubjectRefXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.Date;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SignetSubjectRefXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignetSubjectRefXb", propOrder = {
    "modifyDatetime",
    "synchDatetime",
    "subjectName",
    "signetSubjectAttrs"
})
public class SignetSubjectRefXb
{
	/** Primary key for persistent store of Subjects.
	 * If non-null and non-zero, subject_PK indicates this Subject exists in
	 * Persisted store.
	 * Hibernate field. */
	@XmlAttribute(name="subject_PK", required=true)
	protected Long			subject_PK;

	/** The identifier of this Subject as defined in the original SubjectAPI
	 * Subject. Hibernate field. */
	@XmlAttribute(name="subjectId", required=false)
	protected String		subjectId;

	/** The identifier of the originating Source of this Subject. Hibernate field. */
	@XmlAttribute(name="sourceId", required=true)
	protected String		sourceId;

	/** The type of this Subject as defined in the original SubjectAPI Subject.
	 * Hibernate field. */
	@XmlAttribute(name="subjectType", required=false)
	protected String		subjectType;

	/** The name of this Subject as defined in the original SubjectAPI Subject.
	 * Hibernate field. */
	@XmlElement(name="subjectName", required=false)
	protected String		subjectName;

	/** The Date of the most recent modification to this Subject within Signet.
	 * Hibernate field. */
	@XmlElement(name="modifyDatetime", required=false)
	protected String		modifyDatetime;

	/** The Date of the most recent synchronization between the SubjectAPI and
	 * persisted store. Hibernate field. */
	@XmlElement(name="synchDatetime", required=false)
	protected String		synchDatetime;

	/** A Set of SignetSubjectAttribute representing the attributes of interest
	 * for this Subject. Hibernate collection */
	@XmlElement(name="signetSubjectAttrs", required=false)
	protected Set<SignetSubjectAttrXb>	signetSubjectAttrs;

}
