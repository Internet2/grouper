/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/LimitImplXa.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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

import edu.internet2.middleware.signet.DataType;
import edu.internet2.middleware.signet.LimitImpl;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.util.xml.binder.LimitImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * LimitImplXa 
 * 
 */
public class LimitImplXa
{
	protected Signet		signet;
	protected LimitImpl		signetLimitImpl;
	protected LimitImplXb	xmlLimitImpl;

	public LimitImplXa(LimitImpl signetLimitImpl, Signet signet)
	{
		this.signet = signet;
		this.signetLimitImpl = signetLimitImpl;
		xmlLimitImpl = new ObjectFactory().createLimitImplXb();
		setValues(signetLimitImpl);
	}

	public LimitImplXa(LimitImplXb xmlLimitImpl, Signet signet)
	{
		this.signet = signet;
		this.xmlLimitImpl = xmlLimitImpl;
		signetLimitImpl = new LimitImpl();
		setValues(xmlLimitImpl);
	}

	public LimitImpl getSignetLimitImpl()
	{
		return (signetLimitImpl);
	}

	public void setValues(LimitImpl signetLimitImpl)
	{
//	protected Integer			key;
		xmlLimitImpl.setLimitPK(signetLimitImpl.getKey());

//	protected String			subsystemId;
		xmlLimitImpl.setSubsystemId(signetLimitImpl.getSubsystem().getId());

//	protected String			id;
		xmlLimitImpl.setId(signetLimitImpl.getId());

//	protected String			dataType;
		xmlLimitImpl.setDataType(signetLimitImpl.getDataType().getName());

//	protected String			choiceSetId;
		xmlLimitImpl.setChoiceSetId(signetLimitImpl.getChoiceSet().getId());

//	protected String			name;
		xmlLimitImpl.setName(signetLimitImpl.getName());

//	protected String			status;
		xmlLimitImpl.setStatus(signetLimitImpl.getStatus().getName());

//	protected int				displayOrder;
		xmlLimitImpl.setDisplayOrder(signetLimitImpl.getDisplayOrder());

//	protected Set<LimitValueXb>	limitValues;
//		List<LimitValueXb> xmlLimitValueList = xmlLimitImpl.getLimitValues();
//		for (Iterator<LimitValue> sigLimitValues = signetLimitImpl.get
	}

	public LimitImplXb getXmlLimitImpl()
	{
		return (xmlLimitImpl);
	}

	public void setValues(LimitImplXb xmlLimitImpl)
	{
//	protected Integer			key;
		signetLimitImpl.setKey(new Integer(xmlLimitImpl.getLimitPK()));

//	protected String			subsystemId;
		try
		{
			Subsystem subsys = signet.getPersistentDB().getSubsystem(xmlLimitImpl.getSubsystemId());
			signetLimitImpl.setSubsystem(subsys);
		}
		catch (ObjectNotFoundException e) { e.printStackTrace(); }

//	protected String			id;
		signetLimitImpl.setId(xmlLimitImpl.getId());

//	protected String			dataType;
		signetLimitImpl.setDataType((DataType)DataType.getInstanceByName(xmlLimitImpl.getDataType()));

//	protected String			choiceSetId;
		signetLimitImpl.setChoiceSetId(xmlLimitImpl.getChoiceSetId());

//	protected String			name;
		signetLimitImpl.setName(xmlLimitImpl.getName());

//	protected String			status;
		signetLimitImpl.setStatus((Status)Status.getInstanceByName(xmlLimitImpl.getStatus()));

//	protected int				displayOrder;
		signetLimitImpl.setDisplayOrder(xmlLimitImpl.getDisplayOrder());
	}

}
