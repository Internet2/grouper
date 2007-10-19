/*--
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/signet/stubs/ChoiceImplXb.java,v 1.1 2007-10-19 23:27:11 ddonn Exp $

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
import javax.xml.bind.annotation.XmlType;


/**
 * ChoiceImplXb 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChoiceImplXb",
		namespace="http://www.internet2.edu/signet",
		propOrder = { "value", "displayValue", "displayOrder", "rank", "modifyDatetime" }
)
public class ChoiceImplXb
{
	@XmlAttribute(name="choiceId", required=true)
	protected int			key;

	@XmlElement(name="displayOrder", required=true)
	protected int			displayOrder;

	@XmlElement(name="rank", required=true)
	protected int			rank;

	@XmlElement(name="displayValue", required=false)
	protected String		displayValue;

	@XmlElement(name="value", required=true)
	protected String		value;

	@XmlElement(name="modifyDatetime", required=false)
	protected String		modifyDatetime;
}
