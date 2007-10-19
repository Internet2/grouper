/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/SubsystemImplXb.java,v 1.1 2007-10-19 23:27:11 ddonn Exp $

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * SubsystemImplXb 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubsystemImplXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = { "scopeTreeId", "helpText", "categories", "functions",
			"choiceSets", "limits", "permissions" }
)
public class SubsystemImplXb extends EntityImplXb
{
	@XmlElement(required=true)
	protected String					scopeTreeId;

	@XmlElement(required=false)
	protected String					helpText;

	@XmlElement(required=true)
	protected List<CategoryImplXb>		categories;

	@XmlElement(required=true)
	protected List<FunctionImplXb>		functions;

	@XmlElement(required=true)
	protected List<ChoiceSetImplXb>		choiceSets;

	@XmlElement(required=true)
	protected List<LimitImplXb>			limits;

	@XmlElement(required=true)
	protected List<PermissionImplXb>	permissions;

}
