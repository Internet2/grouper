/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/SignetSubjectXb.java,v 1.2 2008-05-17 20:54:09 ddonn Exp $

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

import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SignetSubjectXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignetSubjectXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = {
			"subjectName",
			"actingAs",
			"modifyDatetime",
			"synchDatetime",
			"subjectAttr" //,
//			"assignmentGranted",
//			"assignmentReceived",
//			"proxyGranted",
//			"proxyReceived"
		}
)
public class SignetSubjectXb extends SignetSubjectRefXb
{
//	/** Primary key for persistent store of Subjects.
//	 * If non-null and non-zero, subject_PK indicates this Subject exists in
//	 * Persisted store.
//	 * Hibernate field. */
//	@XmlAttribute(name="subject_PK", required=true)
//	protected Long			subject_PK;
//
//	/** The identifier of this Subject as defined in the original SubjectAPI
//	 * Subject. Hibernate field. */
//	@XmlAttribute(name="subjectId", required=false)
//	protected String		subjectId;
//
//	/** The identifier of the originating Source of this Subject. Hibernate field. */
//	@XmlAttribute(name="sourceId", required=true)
//	protected String		sourceId;

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
	@XmlElement(name="subjectAttr", required=false)
	protected Set<SignetSubjectAttrXb>	subjectAttr;

	/** A Subject may act as another Subject for the purpose of managing
	 * Proxies and Assignments. Not a Hibernate field. */
	@XmlElement(required=false)
	protected SignetSubjectRefXb		actingAs;

//	/** The set of assignments granted BY this subject */
//	@XmlElement(required=false)
//	protected List<AssignmentImplRefXb>	assignmentGranted;
//
//	/** The set of assignments granted TO this subject */
//	@XmlElement(required=false)
//	protected List<AssignmentImplRefXb>	assignmentReceived;
//
//	/** The set of proxies granted BY this subject */
//	@XmlElement(required=false)
//	protected List<ProxyImplRefXb>		proxyGranted;
//
//	/** The set of proxies granted TO this subject */
//	@XmlElement(required=false)
//	protected List<ProxyImplRefXb>		proxyReceived;

}
