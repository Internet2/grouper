/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/PermissionImplXa.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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

import edu.internet2.middleware.signet.PermissionImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.PermissionImplXb;

/**
 * PermissionImplXa 
 * 
 */
public class PermissionImplXa extends EntityImplXa
{
	public PermissionImplXa()
	{
	}

	public PermissionImplXa(Signet signet)
	{
		super(signet);
	}

	public PermissionImplXa(PermissionImpl signetPermission, Signet signet)
	{
		this(signet);
		signetEntity = signetPermission;
		xmlEntity = new ObjectFactory().createPermissionImplXb();
		setValues(signetPermission);
	}

	public PermissionImplXa(PermissionImplXb xmlPermission, Signet signet)
	{
		this(signet);
		xmlEntity = xmlPermission;
		signetEntity = new PermissionImpl();
		setValues(xmlPermission);
	}


	public PermissionImpl getSignetPermission()
	{
		return ((PermissionImpl)signetEntity);
	}

	public void setValues(PermissionImpl signetPermission)
	{
//TODO PermissionImplXa.setValues
	}


	public PermissionImplXb getXmlPermission()
	{
		return ((PermissionImplXb)xmlEntity);
	}

	public void setValues(PermissionImplXb xmlPermission)
	{
//TODO PermissionImplXa.setValues
	}

}
