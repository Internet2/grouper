/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/SignetSubjectRefXb.java,v 1.1 2007-10-19 23:27:11 ddonn Exp $

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
@XmlType(name = "SignetSubjectRefXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = { }
)
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

}
