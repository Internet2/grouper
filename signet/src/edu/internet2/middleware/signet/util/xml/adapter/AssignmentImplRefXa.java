/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/AssignmentImplRefXa.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentImplRefXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * AssignmentImplRefXa 
 * 
 */
public class AssignmentImplRefXa
{
	protected Signet				signet;
	protected AssignmentImpl		signetAssignment;
	protected AssignmentImplRefXb	xmlAssignmentRef;


	public AssignmentImplRefXa()
	{
	}

	public AssignmentImplRefXa(Signet signet)
	{
		this.signet = signet;
	}

	public AssignmentImplRefXa(AssignmentImpl signetAssignment, Signet signet)
	{
		this(signet);
		this.signetAssignment = signetAssignment;
		xmlAssignmentRef = new ObjectFactory().createAssignmentImplRefXb();
		setValues(signetAssignment);
	}

	public AssignmentImplRefXa(AssignmentImplRefXb xmlAssignmentRef, Signet signet)
	{
		this(signet);
		this.xmlAssignmentRef = xmlAssignmentRef;
		// setValues does a DB lookup, so don't set signetAssignment here
		setValues(xmlAssignmentRef);
	}


	public AssignmentImpl getSignetAssignment()
	{
		return (signetAssignment);
	}

	public void setValues(AssignmentImpl signetAssignment)
	{
		xmlAssignmentRef.setId(signetAssignment.getId().toString());
		xmlAssignmentRef.setName(signetAssignment.getName());
		xmlAssignmentRef.setStatus(signetAssignment.getStatus().toString());
		xmlAssignmentRef.setCanGrant(signetAssignment.canGrant());
		xmlAssignmentRef.setCanUse(signetAssignment.canUse());
	}


	public AssignmentImplRefXb getXmlAssignmentRef()
	{
		return (xmlAssignmentRef);
	}

	public void setValues(AssignmentImplRefXb xmlAssignmentRef)
	{
		HibernateDB hibr = signet.getPersistentDB();
		Assignment dbAssignment = null;
		try
		{
			dbAssignment = hibr.getAssignment(Integer.parseInt(xmlAssignmentRef.getId()));
		}
		catch (NumberFormatException nfe) { nfe.printStackTrace(); }
		catch (ObjectNotFoundException onfe) { onfe.printStackTrace(); }

		signetAssignment = (AssignmentImpl)dbAssignment;
	}

}
