/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/GrantableImplXb.java,v 1.2 2008-05-17 20:54:09 ddonn Exp $

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
@XmlType(name = "GrantableImplXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = {
			"effectiveDate",
			"expirationDate",
			"grantor",
			"proxy",
			"grantee",
			"revoker",
			"instanceNumber"
		}
)
public abstract class GrantableImplXb extends EntityImplXb
{
	/** Database primary key. GrantableImplXb is unusual among Signet entities in
	 * that it has a numeric, not alphanumeric ID.
	 * Note!! Overrides/masks super.id (a dangerous practice) */
//	@XmlAttribute(name="id", required=true)
//	protected int					id;

	/** If this Grantable instance was granted directly by a PrivilegedSubject,
	 * then this is that PrivilegedSubject and 'proxy', below, will be null.
	 * If this Grantable instance was granted by an "acting as" Subject, then
	 * this is that "acting as" Subject and 'proxy' will be the logged-in Subject. */
	@XmlElement(name="grantor", required=true)
	protected SignetSubjectRefXb	grantor;

	/** If this Grantable instance was granted/revoked directly by a Subject,
	 * then this is null. If this Grantable instance was granted/revoked by an
	 * "acting as" Subject, then this is the "acting as" PrivilegedSubject. */
	@XmlElement(name="actingAs", required=false)
	protected SignetSubjectRefXb	proxy;

	/** The recipient of this grant */
	@XmlElement(name="grantee", required=true)
	protected SignetSubjectRefXb	grantee;

	/** The revoker of this grant */
	@XmlElement(name="revoker", required=false)
	protected SignetSubjectRefXb	revoker;

	@XmlElement(name="effectiveDate", required=true)
	protected String				effectiveDate;

	@XmlElement(name="expirationDate", required=false)
	protected String				expirationDate;

	@XmlElement(name="instanceNumber", required=true)
	protected int					instanceNumber;

}
