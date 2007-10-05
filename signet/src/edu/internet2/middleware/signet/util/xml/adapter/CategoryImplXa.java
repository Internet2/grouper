/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/CategoryImplXa.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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
import edu.internet2.middleware.signet.util.xml.binder.CategoryImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * CategoryImplXa 
 * 
 */
public class CategoryImplXa extends EntityImplXa
{
	public CategoryImplXa()
	{
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
		((CategoryImplXb)xmlEntity).setCategoryPK(signetCategory.getKey());
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
		((CategoryImpl)signetEntity).setKey(xmlCategory.getCategoryPK());
	}

}
