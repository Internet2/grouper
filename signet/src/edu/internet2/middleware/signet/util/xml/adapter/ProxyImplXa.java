/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/ProxyImplXa.java,v 1.3 2008-05-17 20:54:09 ddonn Exp $

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

import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.ProxyImplXb;

/**
 * ProxyImplXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a ProxyImpl and a ProxyImplXb.
 * @see ProxyImpl
 * @see ProxyImplXb
 */
public class ProxyImplXa extends GrantableImplXa
{
	public ProxyImplXa()
	{
		super();
	}

	public ProxyImplXa(Signet signet)
	{
		super(signet);
	}

	public ProxyImplXa(ProxyImpl signetProxyImpl, Signet signet)
	{
		this(signet);
		signetEntity = signetProxyImpl;
		xmlEntity = new ObjectFactory().createProxyImplXb();
		setValues(signetProxyImpl);
	}

	public ProxyImplXa(ProxyImplXb xmlProxyImpl, Signet signet)
	{
		this(signet);
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
		super.setValues(signetProxyImpl);

		ProxyImplXb xmlProxy = (ProxyImplXb)this.xmlEntity;

		xmlProxy.setCanExtend(signetProxyImpl.canExtend());
		xmlProxy.setCanUse(signetProxyImpl.canUse());
		xmlProxy.setSubsystemId(signetProxyImpl.getSubsystem().getId());
	}


	public ProxyImplXb getXmlProxy()
	{
		return ((ProxyImplXb)xmlEntity);
	}

	public void setValues(ProxyImplXb xmlProxyImpl)
	{
		super.setValues(xmlProxyImpl);

		ProxyImpl sigProxy = (ProxyImpl)signetEntity;

		sigProxy.setCanExtend(xmlProxyImpl.isCanExtend());
		sigProxy.setCanUse(xmlProxyImpl.isCanUse());
		HibernateDB hibr = signet.getPersistentDB();
		try
		{
			sigProxy.setSubsystem(hibr.getSubsystem(xmlProxyImpl.getSubsystemId()));
		}
		catch (ObjectNotFoundException e)
		{
			e.printStackTrace();
		}
	}

}
