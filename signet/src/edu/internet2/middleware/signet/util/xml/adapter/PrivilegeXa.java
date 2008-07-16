/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/PrivilegeXa.java,v 1.2 2008-07-16 07:34:00 ddonn Exp $

Copyright (c) 2008 Internet2, Stanford University

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.util.xml.binder.LimitValueXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.PrivilegeXb;

/**
 * PrivilegeXa 
 * 
 */
public class PrivilegeXa
{
	protected Signet			signet;
	protected AssignmentImpl	signetEntity;
	protected PrivilegeXb		xmlEntity;

	public PrivilegeXa()
	{
	}

	public PrivilegeXa(Signet signet)
	{
		this.signet = signet;
	}

	public PrivilegeXa(AssignmentImpl signetAssignment, Signet signet)
	{
		this(signet);
		signetEntity = signetAssignment;
		xmlEntity = new ObjectFactory().createPrivilegeXb();
		setValues(signetAssignment);
	}

//	public PrivilegeXa(PrivilegeXb xmlPrivilege, Signet signet)
//	{
//		this(signet);
//		xmlEntity = xmlPrivilege;
//		
//
//	}

	public AssignmentImpl getAssignment()
	{
		return (signetEntity);
	}


	public PrivilegeXb getXmlPrivilege()
	{
		return (xmlEntity);
	}

	public void setValues(AssignmentImpl signetAssignment, Signet signet)
	{
		this.signet = signet;
		setValues(signetAssignment);
	}

	public void setValues(AssignmentImpl signetAssignment)
	{
		xmlEntity.setCanGrant(signetAssignment.canGrant());

		xmlEntity.setCanUse(signetAssignment.canUse());

		xmlEntity.setEffectiveDate(Util.convertDateToString(signetAssignment.getEffectiveDate()));

		xmlEntity.setExpirationDate(Util.convertDateToString(signetAssignment.getExpirationDate()));

		xmlEntity.setFunction(signetAssignment.getFunction().getId());

		xmlEntity.setScope(signetAssignment.getScope().getStringId());

		xmlEntity.setStatus(signetAssignment.getStatus().getName());

		xmlEntity.setSubsystem(signetAssignment.getFunction().getSubsystem().getId());

		List<LimitValueXb> xmlLimits = xmlEntity.getLimitValue();
		Set<LimitValue> limits = (Set<LimitValue>)signetAssignment.getLimitValues();
		for (LimitValue limitValue : limits)
			xmlLimits.add(new LimitValueXa(limitValue, signet).getXmlLimitValue());
	}
}
