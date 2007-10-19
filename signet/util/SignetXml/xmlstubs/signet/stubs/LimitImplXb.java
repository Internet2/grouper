/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/LimitImplXb.java,v 1.1 2007-10-19 23:27:11 ddonn Exp $

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
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * LimitImplXb 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LimitImplXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = { "dataType", "choiceSetId", "name",
			"status", "displayOrder", "limitValues" }
)
public class LimitImplXb extends LimitImplRefXb
{
	@XmlElement(name="dataType", required=true)
	protected String			dataType;

	@XmlElement(name="choiceSetId", required=true)
	protected String			choiceSetId;

	@XmlElement(name="name", required=true)
	protected String			name;

//	protected String			helpText;
//	protected Date				modifyDatetime;

	@XmlElement(name="status", required=true)
	protected String			status;

//	protected Set				permissions;

	@XmlElement(name="displayOrder", required=true)
	protected int				displayOrder;

	@XmlElement(name="limitValues", required=false)
	protected List<LimitValueXb>	limitValues;

}
