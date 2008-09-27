/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/AssignmentSetXa.java,v 1.4 2008-09-27 01:02:09 ddonn Exp $

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentImplXb;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentSetXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * AssignmentSetXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a Collection&lt;AssignmentImpl&gt; and an AssignmentSetXb.
 * @see AssignmentImpl
 * @see AssignmentSetXb
 * 
 */
public class AssignmentSetXa
{
	protected Signet						signet;
	protected Collection<AssignmentImpl>	signetAssignments;
	protected AssignmentSetXb				xbAssignments;
	/** logging */
	private static Log						log = LogFactory.getLog(AssignmentSetXa.class);


	/**
	 * Constructor
	 */
	protected AssignmentSetXa()
	{
	}

	/**
	 * Constructor
	 * @param signetAssignments
	 * @param signet
	 */
	public AssignmentSetXa(Collection<AssignmentImpl> signetAssignments, Signet signet)
	{
		this();
		this.signet = signet;
		setValues(signetAssignments);
//		setSignetAssignments(signetAssignments);
	}

	/**
	 * Constructor
	 * @param xbAssignments
	 * @param signet
	 */
	public AssignmentSetXa(AssignmentSetXb xbAssignments, Signet signet)
	{
		this();
		this.signet = signet;
		setValues(xbAssignments);
//		setXmlAssignments(xbAssignments);
	}


	/**
	 * Get the assignments from this XML adapter
	 * @return The assignments from this XML adapter
	 */
	public Collection<AssignmentImpl> getSignetAssignments()
	{
		return (signetAssignments);
	}

	/**
	 * Replaces the set of assignments
	 * @param signetAssignments
	 */
	public void setValues(Collection<AssignmentImpl> signetAssignments)
	{
		this.signetAssignments = signetAssignments;

		ObjectFactory of = new ObjectFactory();
		xbAssignments = of.createAssignmentSetXb();

		List<AssignmentImplXb> list = xbAssignments.getAssignment();
		for (AssignmentImpl assign : signetAssignments)
		{
//log.info("AssignmentSetXa.setValues(Collection<AssignmentImpl>): signetAssignment = " + assign.toString());
			AssignmentImplXa xaAssignment = new AssignmentImplXa(assign, signet);
			list.add(xaAssignment.getXmlAssignment());
		}
	}

	/**
	 * Get the set of XML assignments
	 * @return The set of XML assignments
	 */
	public AssignmentSetXb getXmlAssignments()
	{
		return (xbAssignments);
	}

	/**
	 * Replaces the set of assignments
	 * @param xbAssignments
	 */
	public void setValues(AssignmentSetXb xbAssignments)
	{
		this.xbAssignments = xbAssignments;
		signetAssignments = new HashSet<AssignmentImpl>();
		for (Iterator<AssignmentImplXb> xmlAssigns = xbAssignments.getAssignment().iterator();
				xmlAssigns.hasNext(); )
		{
			AssignmentImplXb assign = xmlAssigns.next();
//log.info("AssignmentSetXa.setValues(AssignmentSetXb): xmlAssignment = " + assign.toString());
			AssignmentImplXa adapter = new AssignmentImplXa(assign, signet);
			signetAssignments.add(adapter.getSignetAssignment());
		}
	}

}
