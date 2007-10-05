/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/ChoiceSetImplXa.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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

import edu.internet2.middleware.signet.ChoiceSetImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.util.xml.binder.ChoiceSetImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * ChoiceSetImplXa 
 * 
 */
public class ChoiceSetImplXa
{
	protected Signet			signet;
	protected ChoiceSetImpl		signetChoiceSet;
	protected ChoiceSetImplXb	xmlChoiceSet;


	public ChoiceSetImplXa()
	{
	}

	public ChoiceSetImplXa(Signet signet)
	{
		this.signet = signet;
	}

	public ChoiceSetImplXa(ChoiceSetImpl signetChoiceSet, Signet signet)
	{
		this(signet);
		this.signetChoiceSet = signetChoiceSet;
		xmlChoiceSet = new ObjectFactory().createChoiceSetImplXb();
		setValues(signetChoiceSet);
	}

	public ChoiceSetImplXa(ChoiceSetImplXb xmlChoiceSet, Signet signet)
	{
		this(signet);
		this.xmlChoiceSet = xmlChoiceSet;
		signetChoiceSet = new ChoiceSetImpl();
		setValues(xmlChoiceSet);
	}


	public ChoiceSetImpl getSignetChoiceSet()
	{
		return (signetChoiceSet);
	}

	public void setValues(ChoiceSetImpl signetChoiceSet)
	{
//TODO ChoiceSetImplXa.setValues
	}


	public ChoiceSetImplXb getXmlChoiceSet()
	{
		return (xmlChoiceSet);
	}

	public void setValues(ChoiceSetImplXb xmlChoiceSet)
	{
//TODO ChoiceSetImplXa.setValues
	}

}
