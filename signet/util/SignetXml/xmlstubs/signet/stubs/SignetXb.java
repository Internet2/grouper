/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/SignetXb.java,v 1.4 2008-06-23 22:27:44 ddonn Exp $

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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SignetXb 
 * 
 */
@XmlRootElement(name="Signet", namespace="http://www.internet2.edu/signet")

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="SignetXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = {
			"scopeTreeSet",
			"subsystemSet",
			"subjectSet",
			"proxySet",
			"assignmentSet",
			"permissions"
		}
)
public class SignetXb
{
	@XmlAttribute(name="SignetVersion", required=true)
	protected String					signetVersion;

	@XmlAttribute(name="XmlCreateDate", required=false)
	protected String					xmlCreateDate;

	///////////////////////////////
	//  Signet Metadata
	///////////////////////////////

	@XmlElement(name="ScopeTreeSet", required=false)
	protected ScopeTreeSetXb		scopeTreeSet;

	@XmlElement(name="SubjectSet", required=false)
	protected SignetSubjectSetXb	subjectSet;

	@XmlElement(name="SubsystemSet", required=false)
	protected SubsystemSetXb		subsystemSet;

	///////////////////////////////
	//  Privilege Data
	///////////////////////////////

	@XmlElement(name="AssignmentSet", required=false)
	protected AssignmentSetXb		assignmentSet;

	@XmlElement(name="ProxySet", required=false)
	protected ProxySetXb			proxySet;

	///////////////////////////////
	//  Provisioning and/or Information only, not a true export
	///////////////////////////////

	@XmlElement(name="Permissions", required=false)
	protected List<PermissionsDocXb>	permissions;

}
