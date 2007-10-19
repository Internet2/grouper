/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/ScopeTreeXb.java,v 1.1 2007-10-19 23:27:11 ddonn Exp $

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
 * ScopeTreeXb
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScopeTreeXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = { "adapterClassName", "rootNodes" }
)
public class ScopeTreeXb extends EntityImplXb
{
	@XmlElement(name="rootNodes", required=false)
	protected List<TreeNodeImplXb>	rootNodes;

	@XmlElement(name="adapterClassName", required=false)
	protected String				adapterClassName;

}
