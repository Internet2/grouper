/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/SignetSubjectAttrXb.java,v 1.1 2007-10-19 23:27:11 ddonn Exp $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package signet.stubs;

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
@XmlType(name = "SignetSubjectAttrXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = { "attrValue", "parent", "modifyDate" }
)
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
