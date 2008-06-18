/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/SubsystemImplXa.java,v 1.4 2008-06-18 01:21:39 ddonn Exp $

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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import edu.internet2.middleware.signet.CategoryImpl;
import edu.internet2.middleware.signet.ChoiceSetImpl;
import edu.internet2.middleware.signet.FunctionImpl;
import edu.internet2.middleware.signet.LimitImpl;
import edu.internet2.middleware.signet.PermissionImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.TreeImpl;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.util.xml.binder.CategoryImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ChoiceSetImplXb;
import edu.internet2.middleware.signet.util.xml.binder.FunctionImplXb;
import edu.internet2.middleware.signet.util.xml.binder.LimitImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.PermissionImplXb;
import edu.internet2.middleware.signet.util.xml.binder.SubsystemImplXb;

/**
 * SubsystemImplXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a SubsystemImpl and a SubsystemImplXb.
 * @see SubsystemImpl
 * @see SubsystemImplXb
 */
public class SubsystemImplXa extends EntityImplXa
{
	/**
	 * Default constructor
	 */
	public SubsystemImplXa()
	{
		super();
	}

	/**
	 * Constructor - Initialize this adapter with the instance of Signet
	 * @param signet An instance of Signet
	 */
	public SubsystemImplXa(Signet signet)
	{
		super(signet);
	}

	/**
	 * Initialize this adapter with the given Signet Subsystem, and create an
	 * XML binder for it.
	 * @param signetSubsystem A Signet Subsystem
	 * @param signet An instance of Signet
	 */
	public SubsystemImplXa(SubsystemImpl signetSubsystem, Signet signet)
	{
		this(signet);
		signetEntity = signetSubsystem;
		xmlEntity = new ObjectFactory().createSubsystemImplXb();
		setValues(signetSubsystem, signet);
	}

	/**
	 * Initialize this adapter with the given XML binder, and create a
	 * Signet Subsystem for it.
	 * @param xmlSubsystem An XML binder
	 * @param signet An instance of Signet
	 */
	public SubsystemImplXa(SubsystemImplXb xmlSubsystem, Signet signet)
	{
		this(signet);
		xmlEntity = xmlSubsystem;
		signetEntity = new SubsystemImpl();
		setValues(xmlSubsystem, signet);
	}


	/**
	 * @return The Signet Subsystem for this adapter
	 */
	public SubsystemImpl getSignetSubsystem()
	{
		return ((SubsystemImpl)signetEntity);
	}

	/**
	 * Initialize the XML binder (previously created) with the values of the
	 * Signet Subsystem
	 * @param signetSubsystem A Signet Subsystem
	 * @param signet An instance of Signet
	 */
	public void setValues(SubsystemImpl signetSubsystem, Signet signet)
	{
		this.signet = signet;
		setValues(signetSubsystem);
	}

	/**
	 * Initialize the XML binder (previously created) with the values of the
	 * Signet Subsystem
	 * @param signetSubsystem A Signet Subsystem
	 */
	public void setValues(SubsystemImpl signetSubsystem)
	{
		super.setValues(signetSubsystem);

		SubsystemImplXb xmlSubsys = (SubsystemImplXb)xmlEntity;

//	private Tree	tree;
		TreeImpl tmpTree = (TreeImpl)signetSubsystem.getTree();
		if (null != tmpTree)
			xmlSubsys.setScopeTreeId(tmpTree.getId());

//  private String  helpText;
		xmlSubsys.setHelpText(signetSubsystem.getHelpText());

//  private Set     categories;
		List<CategoryImplXb> xmlCats = xmlSubsys.getCategory();
		for (CategoryImpl signetCat : (Set<CategoryImpl>)signetSubsystem.getCategories())
			xmlCats.add(new CategoryImplXa(signetCat, signet).getXmlCategory());

//  private Set     functions;
		List<FunctionImplXb> xmlFuncs = xmlSubsys.getFunction();
		for (FunctionImpl signetFunc : (Set<FunctionImpl>)signetSubsystem.getFunctions())
			xmlFuncs.add(new FunctionImplXa(signetFunc, signet).getXmlFunction());

//  private Set     choiceSets;
		List<ChoiceSetImplXb> xmlChoices = xmlSubsys.getChoiceSet();
		for (ChoiceSetImpl signetChoiceSet : (Set<ChoiceSetImpl>)signetSubsystem.getChoiceSets())
			xmlChoices.add(new ChoiceSetImplXa(signetChoiceSet, signet).getXmlChoiceSet());
		
//  private Map     limits;
		List<LimitImplXb> xmlLimits = xmlSubsys.getLimit();
		Map<String, LimitImpl> limits = signetSubsystem.getLimits();
		if (null != limits)
		{
			for (LimitImpl signetLimit : limits.values())
				xmlLimits.add(new LimitImplXa(signetLimit, signet).getXmlLimitImpl());
		}
//  private Map     permissions;
		List<PermissionImplXb> xmlPerms = xmlSubsys.getPermission();
		Map<String, PermissionImpl> permissions = signetSubsystem.getPermissions();
		if (null != permissions)
		{
			for (PermissionImpl signetPerm : permissions.values())
				xmlPerms.add(new PermissionImplXa(signetPerm, signet).getXmlPermission());
		}
	}


	/**
	 * @return The XML binder for this adapter
	 */
	public SubsystemImplXb getXmlSubsystem()
	{
		return ((SubsystemImplXb)xmlEntity);
	}

	/**
	 * Initialize the Signet Subsystem (previously created) with the values of
	 * the XML binder
	 * @param xmlSubsystem An XML binder
	 * @param signet An instance of Signet
	 */
	public void setValues(SubsystemImplXb xmlSubsystem, Signet signet)
	{
		this.signet = signet;
		setValues(xmlSubsystem);
	}
	
	/**
	 * Initialize the Signet Subsystem (previously created) with the values of
	 * the XML binder
	 * @param xmlSubsystem An XML binder
	 */
	public void setValues(SubsystemImplXb xmlSubsystem)
	{
		/* setting properties is order-dependend:
			1. scopeTreeId
			2. helpText
			3. choiceSets
			4. limits
			5. permissions
			6. categories
			7. functions
		*/
		super.setValues(xmlSubsystem);

		SubsystemImpl signetSubsys = (SubsystemImpl)signetEntity;

//  private Tree    tree;
		HibernateDB hibr = signet.getPersistentDB();
		Session hs = hibr.openSession();

		Tree dbTree = hibr.getTreeById(hs, xmlSubsystem.getScopeTreeId());
		signetSubsys.setTree(dbTree);

		hibr.closeSession(hs);

//  private String  helpText;
		signetSubsys.setHelpText(xmlSubsystem.getHelpText());

//  private Set     choiceSets;
		for (ChoiceSetImplXb xmlChoiceSet : xmlSubsystem.getChoiceSet())
		{
			ChoiceSetImpl sigChoiceSet = new ChoiceSetImplXa(xmlChoiceSet, signet).getSignetChoiceSet();
			sigChoiceSet.setSubsystem(signetSubsys);
			signetSubsys.add(sigChoiceSet);
		}

//  private Map     limits;
		for (LimitImplXb xmlLimit : xmlSubsystem.getLimit())
			signetSubsys.add(new LimitImplXa(xmlLimit, signet).getSignetLimitImpl());

//  private Map     permissions;
		for (PermissionImplXb xmlPermission : xmlSubsystem.getPermission())
			signetSubsys.add(new PermissionImplXa(xmlPermission, signet).getSignetPermission());

//  private Set     categories;
		for (CategoryImplXb xmlCat : xmlSubsystem.getCategory())
			signetSubsys.add(new CategoryImplXa(xmlCat, signet).getSignetCategory());

//  private Set     functions;
		for (FunctionImplXb xmlFunc : xmlSubsystem.getFunction())
			signetSubsys.add(new FunctionImplXa(xmlFunc, signet).getSignetFunction());

	}
	
}
