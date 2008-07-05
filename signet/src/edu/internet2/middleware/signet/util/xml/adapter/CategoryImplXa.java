/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/CategoryImplXa.java,v 1.4 2008-07-05 01:22:17 ddonn Exp $

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

import edu.internet2.middleware.signet.CategoryImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.util.xml.binder.CategoryImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * CategoryImplXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a CategoryImpl and a CategoryImplXb.
 * @see CategoryImpl
 * @see CategoryImplXb
 */
public class CategoryImplXa extends EntityImplXa
{
	public CategoryImplXa()
	{
		super();
	}

	public CategoryImplXa(Signet signet)
	{
		super(signet);
	}

	public CategoryImplXa(CategoryImpl signetCategory, Signet signet)
	{
		signetEntity = signetCategory;
		xmlEntity = new ObjectFactory().createCategoryImplXb();
		setValues(signetCategory, signet);
	}

	public CategoryImplXa(CategoryImplXb xmlCategory, Signet signet)
	{
		xmlEntity = xmlCategory;
		signetEntity = new CategoryImpl();
		setValues(xmlCategory, signet);
	}


	public CategoryImpl getSignetCategory()
	{
		return ((CategoryImpl)signetEntity);
	}

	public void setValues(CategoryImpl signetCategory, Signet signet)
	{
		this.signet = signet;
		setValues(signetCategory);
	}

	public void setValues(CategoryImpl signetCategory)
	{
		super.setValues(signetCategory);

		CategoryImplXb xmlCategory = (CategoryImplXb)xmlEntity;

		xmlCategory.setKey(signetCategory.getKey());

		xmlCategory.setSubsystemId(signetCategory.getSubsystem().getId());

	}


	public CategoryImplXb getXmlCategory()
	{
		return ((CategoryImplXb)xmlEntity);
	}

	public void setValues(CategoryImplXb xmlCategory, Signet signet)
	{
		this.signet = signet;
		setValues(xmlCategory);
	}

	public void setValues(CategoryImplXb xmlCategory)
	{
		super.setValues(xmlCategory);

		CategoryImpl signetCategory = (CategoryImpl)signetEntity;

		signetCategory.setKey(xmlCategory.getKey());

		HibernateDB hibr = signet.getPersistentDB();
		Subsystem sigSubsys = hibr.getSubsystem(xmlCategory.getSubsystemId());
		signetCategory.setSubsystem(sigSubsys);
	}

}
