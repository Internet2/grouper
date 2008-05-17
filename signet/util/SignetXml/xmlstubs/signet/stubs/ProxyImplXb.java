/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/ProxyImplXb.java,v 1.2 2008-05-17 20:54:09 ddonn Exp $

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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * ProxyImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProxyImplXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = {"actAs", "canExtend", "canUse", "subsystemId" }
)
public class ProxyImplXb extends GrantableImplXb
{
//	/** A String-based ID */
//	@XmlAttribute(name="id", required=true)
//	protected int		id;	// see GrantableImplXb, has an Integer id defined

	/** Can this proxy be extended by the recipient to other Subjects? */
	@XmlElement(name="canExtend", required=true)
	protected boolean	canExtend;

	/** Can this proxy be used by the recipient? */
	@XmlElement(name="canUse", required=true)
	protected boolean	canUse;

//	/** The status (ACTIVE | INACTIVE | PENDING) of this EntityImplXb */
//	@XmlElement(name="status", required=true)
//	protected String	status;

	/** The Subject this is a proxy for */
	@XmlElement(name="actAs", required=true)
	protected String	actAs;

	/** The Subsystem this proxy is valid for */
	@XmlElement(name="subsystemId", required=true)
	protected String	subsystemId;

}
