/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/AssignmentImplRefXa.java,v 1.3 2008-05-17 20:54:09 ddonn Exp $

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
 * AssignmentImplRefXa<p>
 * Adapter class for Signet XML Binding.
 * Maps an AssignmentImpl and an AssignmentImplRefXb.
 * @see AssignmentImpl
 * @see AssignmentImplRefXb
 */
public class AssignmentImplRefXa
{
	protected Signet				signet;
	protected AssignmentImpl		signetAssignment;
	protected AssignmentImplRefXb	xmlAssignmentRef;


	/**
	 * Default constructor
	 */
	public AssignmentImplRefXa()
	{
	}

	/**
	 * Constructor - initializes Signet reference
	 * @param signet An instance of Signet
	 */
	public AssignmentImplRefXa(Signet signet)
	{
		this.signet = signet;
	}

	/**
	 * Initialize this adapter with the given Signet Assignment, and
	 * initialize an XML binder for the Ref
	 * @param signetAssignment A Signet Assignment
	 * @param signet An instance of Signet
	 */
	public AssignmentImplRefXa(AssignmentImpl signetAssignment, Signet signet)
	{
		this(signet);
		this.signetAssignment = signetAssignment;
		xmlAssignmentRef = new ObjectFactory().createAssignmentImplRefXb();
		setValues(signetAssignment);
	}

	/**
	 * Initialize this adapter with the given XML binder and initialize a
	 * Signet Assignment for it
	 * @param xmlAssignmentRef
	 * @param signet
	 */
	public AssignmentImplRefXa(AssignmentImplRefXb xmlAssignmentRef, Signet signet)
	{
		this(signet);
		this.xmlAssignmentRef = xmlAssignmentRef;
		// setValues does a DB lookup, so don't set signetAssignment here
		setValues(xmlAssignmentRef);
	}


	/**
	 * @return The Signet Assignment
	 */
	public AssignmentImpl getSignetAssignment()
	{
		return (signetAssignment);
	}

	/**
	 * Initialize the XML binder (previously created) with the values from the
	 * signet assignment
	 * @param signetAssignment A Signet Assignment
	 */
	public void setValues(AssignmentImpl signetAssignment)
	{
		// note: GrantableImpl overrides EntityImpl's ID field, must convert
		xmlAssignmentRef.setId(signetAssignment.getId().toString());
		xmlAssignmentRef.setStatus(signetAssignment.getStatus().toString());
		xmlAssignmentRef.setCanGrant(signetAssignment.canGrant());
		xmlAssignmentRef.setCanUse(signetAssignment.canUse());
	}


	/**
	 * @return The XML binder
	 */
	public AssignmentImplRefXb getXmlAssignmentRef()
	{
		return (xmlAssignmentRef);
	}

	/**
	 * The XML binder is a reference to, not a definition of, an Assignment and
	 * therefore a DB lookup is required
	 * @param xmlAssignmentRef An XML binder
	 */
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
