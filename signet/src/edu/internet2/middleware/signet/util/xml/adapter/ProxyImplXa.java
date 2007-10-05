/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/ProxyImplXa.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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

import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.ProxyImplXb;

/**
 * ProxyImplXa 
 * 
 */
public class ProxyImplXa extends GrantableImplXa
{
	public ProxyImplXa()
	{
	}

	public ProxyImplXa(Signet signet)
	{
		super(signet);
	}

	public ProxyImplXa(ProxyImpl signetProxyImpl, Signet signet)
	{
		super(signet);
		signetEntity = signetProxyImpl;
		xmlEntity = new ObjectFactory().createProxyImplXb();
		setValues(signetProxyImpl);
	}

	public ProxyImplXa(ProxyImplXb xmlProxyImpl, Signet signet)
	{
		super(signet);
		xmlEntity = xmlProxyImpl;
		signetEntity = new ProxyImpl();
		setValues(xmlProxyImpl);
	}


	public ProxyImpl getSignetProxy()
	{
		return ((ProxyImpl)signetEntity);
	}

	public void setValues(ProxyImpl signetProxyImpl)
	{
//TODO ProxyImplXa.setValues
	}


	public ProxyImplXb getXmlProxy()
	{
		return ((ProxyImplXb)xmlEntity);
	}

	public void setValues(ProxyImplXb xmlProxyImpl)
	{
//TODO ProxyImplXa.setValues
	}

}
