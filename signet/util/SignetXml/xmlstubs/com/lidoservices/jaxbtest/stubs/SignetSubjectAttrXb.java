/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/SignetSubjectAttrXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.Date;
import java.util.Vector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SignetSubjectAttrXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignetSubjectAttrXb", propOrder = {
	"attrValue",
	"parent",
	"modifyDate"
})
public class SignetSubjectAttrXb
{
	/** DB primary key */
	@XmlAttribute(name="subjectAttr_PK", required=true)
	protected Long					subjectAttr_PK;

	/**
	 * Mapped attribute name as defined in SubjectSources.xml. Note that the
	 * SubjectAPI's attribute name (the name that is mappped _to_) is only
	 * maintained in the SignetSource that owns the SignetSubjectXb that owns
	 * this SignetSubjectAttrXb. mappedName is the Signet-internal attribute name
	 * that has been homogenized across all Sources. 
	 */
	@XmlAttribute(name="mappedName", required=false)
	protected String				mappedName;

	/** The attribute's value */
	@XmlElement(name="attrValue", required=true)
	protected String				attrValue;

	/** The attribute's type (e.g. string, integer, float, etc.) */
	@XmlAttribute(name="attrType", required=true)
	protected String				attrType;

	/** date/time stamp of the most recent update of the persisted value */
	@XmlElement(name="modifyDate", required=false)
	protected String				modifyDate;

	/** the sequence number for multi-valued attributes */
	@XmlAttribute(name="sequence", required=true)
	protected int					sequence;

	/** the owner of this attribute */
	@XmlElement(name="parent", required=true)
	protected SignetSubjectRefXb	parent;

}
