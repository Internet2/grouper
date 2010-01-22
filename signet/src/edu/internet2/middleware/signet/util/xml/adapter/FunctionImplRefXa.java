/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/FunctionImplRefXa.java,v 1.1 2008-05-17 20:54:09 ddonn Exp $

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

import org.hibernate.Session;
import edu.internet2.middleware.signet.FunctionImpl;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.util.xml.binder.FunctionImplRefXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * FunctionImplRefXa <p>
 * Adapter class for Signet XML Binding.
 * Maps a FunctionImpl and a FunctionImplRefXb.
 * @see FunctionImpl
 * @see FunctionImplRefXb
 */
public class FunctionImplRefXa
{
	protected Signet				signet;
	protected FunctionImpl			signetFunction;
	protected FunctionImplRefXb		xmlFunctionRef;


	/**
	 * Default constructor
	 */
	public FunctionImplRefXa()
	{
	}

	/**
	 * Initalize signet value only
	 * @param signet
	 */
	public FunctionImplRefXa(Signet signet)
	{
		this.signet = signet;
	}

	/**
	 * Initialize this adapter with the given FunctionImpl and intialize an
	 * XML binder for it
	 * @param signetFunction A Signet Function
	 * @param signet An instance of Signet
	 */
	public FunctionImplRefXa(FunctionImpl signetFunction, Signet signet)
	{
		this.signetFunction = signetFunction;
		xmlFunctionRef = new ObjectFactory().createFunctionImplRefXb();
		setValues(signetFunction, signet);
	}

	/**
	 * Initialize this adapter with the given XML binder and initialize a
	 * Signet FunctionImpl for it
	 * @param xmlFunction An XML binder
	 * @param signet An instance of Signet
	 */
	public FunctionImplRefXa(FunctionImplRefXb xmlFunction, Signet signet)
	{
		this.xmlFunctionRef = xmlFunction;
		signetFunction = new FunctionImpl();
		setValues(xmlFunction, signet);
	}


	/**
	 * @return The Signet FunctionImpl
	 */
	public FunctionImpl getSignetFunction()
	{
		return (signetFunction);
	}

	/**
	 * Initialize the XML binder (previously created) from the FunctionImpl
	 * @param signetFunction A Signet FunctionImpl
	 * @param signet An instance of Signet
	 */
	public void setValues(FunctionImpl signetFunction, Signet signet)
	{
		this.signet = signet;
		setValues(signetFunction);
	}

	/**
	 * Initialize the XML binder (previously created) from the FunctionImpl
	 * @param signetFunction A Signet FunctionImpl
	 */
	public void setValues(FunctionImpl signetFunction)
	{
		xmlFunctionRef.setKey(signetFunction.getKey().intValue());
		xmlFunctionRef.setId(signetFunction.getId());
		xmlFunctionRef.setSubsystemId(signetFunction.getSubsystem().getId());
	}


	/**
	 * @return The XML binder
	 */
	public FunctionImplRefXb getXmlFunction()
	{
		return (xmlFunctionRef);
	}

	/**
	 * Since the binder is a reference to, not a definition of, a Function a
	 * DB lookup is required
	 * @param xmlFunction An XML binder
	 * @param signet An instance of Signet
	 */
	public void setValues(FunctionImplRefXb xmlFunction, Signet signet)
	{
		this.signet = signet;
		setValues(xmlFunction);
	}

	/**
	 * Since the binder is a reference to, not a definition of, a Function a
	 * DB lookup is required
	 * @param xmlFunction An XML binder
	 */
	public void setValues(FunctionImplRefXb xmlFunction)
	{
		HibernateDB hibr = signet.getPersistentDB();
		Session hs = hibr.openSession();

		try
		{
			signetFunction = (FunctionImpl)hibr.getFunction(xmlFunction.getKey());
		}
		catch (ObjectNotFoundException onfe)
		{
			onfe.printStackTrace();
		}
		finally
		{
			hibr.closeSession(hs);
		}
	}

}
