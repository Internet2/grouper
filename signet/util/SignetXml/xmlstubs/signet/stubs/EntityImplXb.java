/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/EntityImplXb.java,v 1.2 2008-05-17 20:54:09 ddonn Exp $

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
 * EntityImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EntityImplXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = {
			"comment",
			"modifyDatetime",
			"createDbAccount",
			"modifyDbAccount",
			"createContext",
			"modifyContext",
			"createUserID",
			"modifyUserID" }
)
public abstract class EntityImplXb
{
	/** The status (ACTIVE | INACTIVE | PENDING) of this EntityImplXb */
	@XmlAttribute(name="status", required=false)
	protected String	status;

	/** The name of this EntityImplXb */
	@XmlAttribute(name="name", required=false)
	protected String	name;

	/** A String-based ID, overridden by GrantableImplXb */
	@XmlAttribute(name="id", required=true)
	protected String	id;	// see GrantableImplXb, has an Integer id defined

	/** A comment for the use of metadata maintainers. */
	@XmlElement(name="comment", required=false)
	protected String	comment;

	/** The date and time this entity was last modified. */
	@XmlElement(name="modifyDatetime", required=false)
	protected String	modifyDatetime;

	/** The account which created this entity. */
	@XmlElement(name="createDbAccount", required=false)
	protected String	createDbAccount;

	/** The database account which last modified this entity. */
	@XmlElement(name="modifyDbAccount", required=false)
	protected String	modifyDbAccount;

	/** The application program responsible for this entity's creation. */
	@XmlElement(name="createContext", required=false)
	protected String	createContext;

	/** The application program responsible for this entity's last modification. */
	@XmlElement(name="modifyContext", required=false)
	protected String	modifyContext;

	/** The user or program that originally generated this entity. */
	@XmlElement(name="createUserID", required=false)
	protected String	createUserID;

	/** The user or program that last modified this entity. */
	@XmlElement(name="modifyUserID", required=false)
	protected String	modifyUserID;

}
