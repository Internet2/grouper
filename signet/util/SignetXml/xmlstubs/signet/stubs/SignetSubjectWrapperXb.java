/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/SignetSubjectWrapperXb.java,v 1.1 2007-10-19 23:27:11 ddonn Exp $

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
@XmlType(name = "SignetSubjectWrapperXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = { "subject" }
)
public class SignetSubjectWrapperXb
{
	@XmlElement(name="Subject", required=true)
	protected SignetSubjectRefXb subject;

	public SignetSubjectRefXb getSubject() { return (subject); }
	public void setSubject(SignetSubjectRefXb subject) { this.subject = subject; }
}
