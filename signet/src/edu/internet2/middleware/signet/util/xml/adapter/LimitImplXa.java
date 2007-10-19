/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/LimitImplXa.java,v 1.2 2007-10-19 23:27:11 ddonn Exp $

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
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.util.xml.binder.LimitImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * LimitImplXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a LimitImpl and a LimitImplXb
 * @see LimitImpl
 * @see LimitImplXb
 */
public class LimitImplXa extends LimitImplRefXa
{
	public LimitImplXa()
	{
		super();
	}

	public LimitImplXa(Signet signet)
	{
		super(signet);
	}

	public LimitImplXa(LimitImpl signetLimitImpl, Signet signet)
	{
		this(signet);
		this.signetLimitImpl = signetLimitImpl;
		xmlLimitImplRef = new ObjectFactory().createLimitImplXb();
		setValues(signetLimitImpl);
	}

	public LimitImplXa(LimitImplXb xmlLimitImpl, Signet signet)
	{
		this(signet);
		this.xmlLimitImplRef = xmlLimitImpl;
		signetLimitImpl = new LimitImpl();
		setValues(xmlLimitImpl);
	}

	public LimitImpl getSignetLimitImpl()
	{
		return (signetLimitImpl);
	}

	public void setValues(LimitImpl signetLimitImpl)
	{
		super.setValues(signetLimitImpl);

		LimitImplXb xmlLimitImpl = (LimitImplXb)xmlLimitImplRef;
//	protected Integer			key;
//		xmlLimitImplRef.setLimitPK(signetLimitImpl.getKey());

//	protected String			subsystemId;
//		xmlLimitImplRef.setSubsystemId(signetLimitImpl.getSubsystem().getId());

//	protected String			id;
//		xmlLimitImplRef.setId(signetLimitImpl.getId());

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
//		List<LimitValueXb> xmlLimitValueList = xmlLimitImplRef.getLimitValues();
//		for (Iterator<LimitValue> sigLimitValues = signetLimitImpl.get
	}

	public LimitImplXb getXmlLimitImpl()
	{
		return ((LimitImplXb)xmlLimitImplRef);
	}

	public void setValues(LimitImplXb xmlLimitImpl)
	{
		super.setValues(xmlLimitImpl);

//	protected Integer			key;
//		signetLimitImpl.setKey(new Integer(xmlLimitImplRef.getLimitPK()));

//	protected String			subsystemId;
//		try
//		{
//			Subsystem subsys = signet.getPersistentDB().getSubsystem(xmlLimitImplRef.getSubsystemId());
//			signetLimitImpl.setSubsystem(subsys);
//		}
//		catch (ObjectNotFoundException e) { e.printStackTrace(); }

//	protected String			id;
//		signetLimitImpl.setId(xmlLimitImplRef.getId());

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
