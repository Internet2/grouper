/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/SignetXa.java,v 1.3 2008-06-18 01:21:39 ddonn Exp $

Copyright (c) 2007 Internet2, Stanford University

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
package edu.internet2.middleware.signet.util.xml.adapter;

import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;

/**
 * SignetXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a Signet and a SignetXb.
 * @see Signet
 * @see SignetXb
 */
public class SignetXa
{
	protected Signet		signet;
	protected SignetXb		xmlSignet;


	public SignetXa()
	{
	}

	public SignetXa(Signet signet)
	{
		this.signet = signet;
		xmlSignet = new ObjectFactory().createSignetXb();
		setValues(signet);
	}

	public SignetXa(SignetXb xmlSignet)
	{
		this.xmlSignet = xmlSignet;
		signet = new Signet();
		setValues(xmlSignet);
	}


	public void setValues(Signet signet)
	{
		xmlSignet.setVersion(Signet.getVersion());
		ObjectFactory of = new ObjectFactory();
		xmlSignet.setAssignmentSet(of.createAssignmentSetXb());
		xmlSignet.setProxieSet(of.createProxySetXb());
		xmlSignet.setScopeTreeSet(of.createScopeTreeSetXb());
		xmlSignet.setSubjectSet(of.createSignetSubjectSetXb());
		xmlSignet.setSubsystemSet(of.createSubsystemSetXb());
//		xmlSignet.setPermissions(...) // initialized by auto-generated code in SignetXb.java
	}

	public void setValues(SignetXb xmlSignet)
	{
		// nothing to do, for now
	}


	public Signet getSignet()
	{
		return (signet);
	}

	public SignetXb getXmlSignet()
	{
		return (xmlSignet);
	}

}
