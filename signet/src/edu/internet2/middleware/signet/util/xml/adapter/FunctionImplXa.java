/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/FunctionImplXa.java,v 1.2 2007-10-19 23:27:11 ddonn Exp $

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

import edu.internet2.middleware.signet.FunctionImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.util.xml.binder.FunctionImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * FunctionImplXa <p>
 * Adapter class for Signet XML Binding.
 * Maps a FunctionImpl and a FunctionImplXb.
 * @see FunctionImpl
 * @see FunctionImplXb
 */
public class FunctionImplXa extends EntityImplXa
{
	public FunctionImplXa()
	{
		super();
	}

	public FunctionImplXa(Signet signet)
	{
		super(signet);
	}

	public FunctionImplXa(FunctionImpl signetFunction, Signet signet)
	{
		signetEntity = signetFunction;
		xmlEntity = new ObjectFactory().createFunctionImplXb();
		setValues(signetFunction, signet);
	}

	public FunctionImplXa(FunctionImplXb xmlFunction, Signet signet)
	{
		xmlEntity = xmlFunction;
		signetEntity = new FunctionImpl();
		setValues(xmlFunction, signet);
	}


	public FunctionImpl getSignetFunction()
	{
		return ((FunctionImpl)signetEntity);
	}

	public void setValues(FunctionImpl signetFunction, Signet signet)
	{
		this.signet = signet;
		setValues(signetFunction);
	}

	public void setValues(FunctionImpl signetFunction)
	{
//TODO FunctionImplXa.setValues
	}


	public FunctionImplXb getXmlFunction()
	{
		return ((FunctionImplXb)xmlEntity);
	}

	public void setValues(FunctionImplXb xmlFunctionImpl, Signet signet)
	{
		this.signet = signet;
		setValues(xmlFunctionImpl);
	}

	public void setValues(FunctionImplXb xmlFunctionImpl)
	{
//TODO FunctionImplXa.setValues
	}

}
