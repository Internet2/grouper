/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/LimitValueXa.java,v 1.3 2007-12-06 01:18:32 ddonn Exp $

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

import edu.internet2.middleware.signet.LimitImpl;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.util.xml.binder.LimitImplRefXb;
import edu.internet2.middleware.signet.util.xml.binder.LimitValueXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * LimitValueXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a LimitValue and a LimitValueXb
 * @see LimitValue
 * @see LimitValueXb
 */
public class LimitValueXa
{
	protected Signet		signet;
	protected LimitValue	signetLimitValue;
	protected LimitValueXb	xmlLimitValue;


	public LimitValueXa(LimitValue signetLimitValue, Signet signet)
	{
		this.signet = signet;
		this.signetLimitValue = signetLimitValue;
		xmlLimitValue = new ObjectFactory().createLimitValueXb();
		setValues(signetLimitValue);
	}

	public LimitValueXa(LimitValueXb xmlLimitValue, Signet signet)
	{
		this.signet = signet;
		this.xmlLimitValue = xmlLimitValue;
		// no default constructor for LimitValue, so do it in setValues
//		this.signetLimitValue = new LimitValue(
		setValues(xmlLimitValue);
	}


	public LimitValue getSignetLimitValue()
	{
		return (signetLimitValue);
	}

	public void setValues(LimitValue signetLimitValue)
	{
		LimitImplRefXa adapter = new LimitImplRefXa((LimitImpl)signetLimitValue.getLimit(), signet);
		xmlLimitValue.setLimit(adapter.getXmlLimitImplRef());
		xmlLimitValue.setValue(signetLimitValue.getValue());
	}


	public LimitValueXb getXmlLimitValue()
	{
		return (xmlLimitValue);
	}

	public void setValues(LimitValueXb xmlLimitValue)
	{
		// fetch the Limit, by Id, from Signet
		LimitImplRefXb xmlLimitImpl = xmlLimitValue.getLimit();
		LimitImplRefXa limitImplAdapter = new LimitImplRefXa(xmlLimitImpl, signet);
		signetLimitValue = new LimitValue(limitImplAdapter.getSignetLimitImpl(), xmlLimitValue.getValue());
	}

}
