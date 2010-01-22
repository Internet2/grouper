/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/AssignmentImplXa.java,v 1.6 2008-09-27 01:02:09 ddonn Exp $

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.FunctionImpl;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.TreeNodeImpl;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentImplXb;
import edu.internet2.middleware.signet.util.xml.binder.LimitValueXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * AssignmentImplXa<p>
 * Adapter class for Signet XML Binding.
 * Maps an AssignmentImpl and an AssignmentImplXb.
 * @see edu.internet2.middleware.signet.AssignmentImpl AssignmentImpl
 * @see edu.internet2.middleware.signet.util.xml.binder.AssignmentImplXb AssignmentImplXb
 */
public class AssignmentImplXa extends GrantableImplXa
{
	/** logging */
	private static Log	log = LogFactory.getLog(AssignmentImplXa.class);


	/**
	 * Default constructor
	 */
	public AssignmentImplXa()
	{
		super();
	}

	/**
	 * Constructor - Initialize the Signet value only
	 * @param signet The Signet instance to be used (later)
	 */
	public AssignmentImplXa(Signet signet)
	{
		super(signet);
	}

	/**
	 * Constructor - Initialize this adapter with the given Signet Assignment,
	 * and initialize an XML binder for it
	 * @param signetAssignment The Signet Assignment
	 * @param signet An instance of Signet
	 */
	public AssignmentImplXa(AssignmentImpl signetAssignment, Signet signet)
	{
		this(signet);
		signetEntity = signetAssignment;
		xmlEntity = new ObjectFactory().createAssignmentImplXb();
		setValues(signetAssignment, signet);
	}

	/**
	 * Constructor - Initialize this adapter with the given XML binder, and
	 * initialize a Signet Assignment for it
	 * @param xmlAssignment The XML Binder
	 * @param signet An instance of Signet
	 */
	public AssignmentImplXa(AssignmentImplXb xmlAssignment, Signet signet)
	{
		this(signet);
		xmlEntity = xmlAssignment;
		signetEntity = new AssignmentImpl();
		setValues(xmlAssignment, signet);
	}


	/**
	 * @return The Signet Assignment
	 */
	public AssignmentImpl getSignetAssignment()
	{
		return ((AssignmentImpl)signetEntity);
	}

	/**
	 * Initialize an XML binder (previously created) from the Signet Assignment
	 * @param signetAssignment The Signet Assignment
	 * @param signet An instance of Signet
	 */
	public void setValues(AssignmentImpl signetAssignment, Signet signet)
	{
		this.signet = signet;
		setValues(signetAssignment);
	}

	/**
	 * Initialize an XML binder (previously created) from the Signet Assignment
	 * @param signetAssignment The Signet Assignment
	 */
	public void setValues(AssignmentImpl signetAssignment)
	{
		super.setValues(signetAssignment);

		AssignmentImplXb xmlAssignment = (AssignmentImplXb)xmlEntity;

//  private TreeNode			scope;
		TreeNodeImpl treeNode = signetAssignment.getScope();
		xmlAssignment.setScope(Util.getScopePath(treeNode));

//  private FunctionImpl		function;
		FunctionImpl function = (FunctionImpl)signetAssignment.getFunction();
		if (null != function)
		{
			xmlAssignment.setFunction(function.getId());
			xmlAssignment.setSubsystem(function.getSubsystem().getId());
		}

//  private Set				limitValues;
		// get the (empty) list from the xmlAssignment
		List<LimitValueXb> xmlLimits = xmlAssignment.getLimitValue();
		// for each signet limitvalue, create an xml limitvalue
		for (LimitValue sigLimit : (Set<LimitValue>)signetAssignment.getLimitValues())
			xmlLimits.add(new LimitValueXa(sigLimit, signet).getXmlLimitValue());
		
//  private boolean			canGrant;
		xmlAssignment.setCanGrant(signetAssignment.canGrant());

//  private boolean			canUse;
		xmlAssignment.setCanUse(signetAssignment.canUse());
	}


	/**
	 * @return The XML binder from this adapter
	 */
	public AssignmentImplXb getXmlAssignment()
	{
		return ((AssignmentImplXb)xmlEntity);
	}

	/**
	 * Initialize a Signet Assignment (previously created) from the XML binder
	 * @param xmlAssignment The XML binder Assignment
	 * @param signet An instance of Signet
	 */
	public void setValues(AssignmentImplXb xmlAssignment, Signet signet)
	{
		this.signet = signet;
		setValues(xmlAssignment);
	}

	/**
	 * Initialize a Signet Assignment (previously created) from the XML binder
	 * @param xmlAssignment The XML binder Assignment
	 */
	public void setValues(AssignmentImplXb xmlAssignment)
	{
		// if the id is valid, can possibly retieve signet assignment from DB
		AssignmentImpl tmpAssignment = null;
		String idStr = xmlAssignment.getId();
		if ((null != idStr) && (0 < idStr.length()))
		{
			try
			{
				int id = Integer.parseInt(idStr);
				tmpAssignment = (AssignmentImpl)signet.getPersistentDB().getAssignment(id);
			}
			catch (NumberFormatException nfe)
			{
log.info("AssignmentImplXa.setValues(AssignmentImplXb): Caught NumberFormatException - Object not found for id=" + idStr);
				nfe.printStackTrace();
			}
			catch (ObjectNotFoundException onfe)
			{
log.info("AssignmentImplXa.setValues(AssignmentImplXb): Caught ObjectNotFoundException - Object not found for id=" + idStr);
				/* do nothing */
			}
			catch (Exception e)
			{
log.info("AssignmentImplXa.setValues(AssignmentImplXb): Caught general Exception - Object not found for id=" + idStr);
			}
		}

		// found assignment in DB, done
		if (null != tmpAssignment)
			signetEntity = tmpAssignment;

		// either no id specified, or not found in DB
		else
		{
			super.setValues(xmlAssignment);
	
			AssignmentImpl signetAssignment = (AssignmentImpl)signetEntity;
	
//  private TreeNode			scope;
			HibernateDB hibr = signet.getPersistentDB();
			Session hs = hibr.openSession();
			String[] path = Util.parseScopePath(xmlAssignment.getScope());
			Tree tree = hibr.getTreeById(hs, path[0]);
			TreeNodeImpl node = (TreeNodeImpl)tree.getNode(path[1]);
			signetAssignment.setScope(node);
			hibr.closeSession(hs);

//  private FunctionImpl		function;
			String functionId = xmlAssignment.getFunction();
			String subsysId = xmlAssignment.getSubsystem();
			try
			{
				Function func = signet.getPersistentDB().getFunction(functionId, subsysId);
				signetAssignment.setFunction(func);
			}
			catch (ObjectNotFoundException e1)
			{
				e1.printStackTrace();
			}
	
//  private Set				limitValues;
			// Note: this must execute _after_ call to super.setValues(GrantableXb)
			Set<LimitValue> signetLimitSet = new HashSet<LimitValue>();
			for (LimitValueXb xmlLimit : xmlAssignment.getLimitValue())
			{
				LimitValueXa adapter = new LimitValueXa(xmlLimit, signet);
				signetLimitSet.add(adapter.getSignetLimitValue());
			}
			try
			{
				signetAssignment.setLimitValues(signetAssignment.getGrantor(), signetLimitSet, false);
			}
			catch (SignetAuthorityException e)
			{
				e.printStackTrace();
			}
	
//	private boolean			canGrant;
//	private boolean			canUse;
			// Note: this must execute _after_ call to super.setValues(GrantableXb)
			try
			{
				signetAssignment.setCanGrant(signetAssignment.getGrantor(), xmlAssignment.isCanGrant(), false);
				signetAssignment.setCanUse(signetAssignment.getGrantor(), xmlAssignment.isCanUse(), false);
			}
			catch (SignetAuthorityException e)
			{
				e.printStackTrace();
			}
		}
	}

}
