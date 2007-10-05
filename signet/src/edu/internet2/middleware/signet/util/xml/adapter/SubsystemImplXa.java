/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/SubsystemImplXa.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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
import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import edu.internet2.middleware.signet.CategoryImpl;
import edu.internet2.middleware.signet.ChoiceSetImpl;
import edu.internet2.middleware.signet.FunctionImpl;
import edu.internet2.middleware.signet.LimitImpl;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PermissionImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.choice.ChoiceSet;
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
 * SubsystemImplXa 
 * 
 */
public class SubsystemImplXa extends EntityImplXa
{
	public SubsystemImplXa()
	{
	}

	public SubsystemImplXa(Signet signet)
	{
		super(signet);
	}

	public SubsystemImplXa(SubsystemImpl signetSubsystem, Signet signet)
	{
		signetEntity = signetSubsystem;
		xmlEntity = new ObjectFactory().createSubsystemImplXb();
		setValues(signetSubsystem, signet);
	}

	public SubsystemImplXa(SubsystemImplXb xmlSubsystem, Signet signet)
	{
		xmlEntity = xmlSubsystem;
		signetEntity = new SubsystemImpl();
		setValues(xmlSubsystem, signet);
	}


	public SubsystemImpl getSignetSubsystem()
	{
		return ((SubsystemImpl)signetEntity);
	}

	public void setValues(SubsystemImpl signetSubsystem, Signet signet)
	{
		this.signet = signet;
		setValues(signetSubsystem);
	}

	public void setValues(SubsystemImpl signetSubsystem)
	{
		super.setValues(signetSubsystem);

		SubsystemImplXb xmlSubsys = (SubsystemImplXb)xmlEntity;

//  private String  helpText;
		xmlSubsys.setHelpText(signetSubsystem.getHelpText());

//  private Set     categories;
		List<CategoryImplXb> xmlCats = xmlSubsys.getCategories();
		for (CategoryImpl signetCat : (Set<CategoryImpl>)signetSubsystem.getCategories())
			xmlCats.add(new CategoryImplXa(signetCat, signet).getXmlCategory());

//  private Set     functions;
		List<FunctionImplXb> xmlFuncs = xmlSubsys.getFunctions();
		for (FunctionImpl signetFunc : (Set<FunctionImpl>)signetSubsystem.getFunctions())
			xmlFuncs.add(new FunctionImplXa(signetFunc, signet).getXmlFunction());

//  private Set     choiceSets;
		List<ChoiceSetImplXb> xmlChoices = xmlSubsys.getChoiceSets();
		for (ChoiceSetImpl signetChoiceSet : (Set<ChoiceSetImpl>)signetSubsystem.getChoiceSets())
			xmlChoices.add(new ChoiceSetImplXa(signetChoiceSet, signet).getXmlChoiceSet());
		
//  private Map     limits;
		List<LimitImplXb> xmlLimits = xmlSubsys.getLimits();
		for (LimitImpl signetLimit : (Collection<LimitImpl>)signetSubsystem.getLimits().values())
			xmlLimits.add(new LimitImplXa(signetLimit, signet).getXmlLimitImpl());

//  private Map     permissions;
		List<PermissionImplXb> xmlPerms = xmlSubsys.getPermissions();
		for (PermissionImpl signetPerm : (Collection<PermissionImpl>)signetSubsystem.getPermissions().values())
			xmlPerms.add(new PermissionImplXa(signetPerm, signet).getXmlPermission());

//  private Tree    tree;
		xmlSubsys.setScopeTreeId(signetSubsystem.getTree().getId());
	}


	public SubsystemImplXb getXmlSubsystem()
	{
		return ((SubsystemImplXb)xmlEntity);
	}

	public void setValues(SubsystemImplXb xmlSubsystem, Signet signet)
	{
		this.signet = signet;
		setValues(xmlSubsystem);
	}
	
	public void setValues(SubsystemImplXb xmlSubsystem)
	{
		super.setValues(xmlSubsystem);

		SubsystemImpl signetSubsys = (SubsystemImpl)signetEntity;

//  private String  helpText;
		signetSubsys.setHelpText(xmlSubsystem.getHelpText());

//  private Set     categories;
		for (CategoryImplXb xmlCat : xmlSubsystem.getCategories())
			signetSubsys.add(new CategoryImplXa(xmlCat, signet).getSignetCategory());

//  private Set     functions;
		HashSet<FunctionImpl> sigFuncs = new HashSet<FunctionImpl>();
		for (FunctionImplXb xmlFunc : xmlSubsystem.getFunctions())
			sigFuncs.add(new FunctionImplXa(xmlFunc, signet).getSignetFunction());
		signetSubsys.setFunctions(sigFuncs);

//  private Set     choiceSets;
		HashSet<ChoiceSet> sigChoiceSets = new HashSet<ChoiceSet>();
		for (ChoiceSetImplXb xmlChoiceSet : xmlSubsystem.getChoiceSets())
			sigChoiceSets.add(new ChoiceSetImplXa(xmlChoiceSet, signet).getSignetChoiceSet());
		signetSubsys.setChoiceSets(sigChoiceSets);

//  private Map     limits;
		for (LimitImplXb xmlLimit : xmlSubsystem.getLimits())
			signetSubsys.add(new LimitImplXa(xmlLimit, signet).getSignetLimitImpl());

//  private Map     permissions;
		for (PermissionImplXb xmlPermission : xmlSubsystem.getPermissions())
			signetSubsys.add(new PermissionImplXa(xmlPermission, signet).getSignetPermission());

//  private Tree    tree;
		HibernateDB hibr = signet.getPersistentDB();
		Session hs = hibr.openSession();
		Tree dbTree = null;
		try
		{
			dbTree = hibr.getTree(hs, xmlSubsystem.getScopeTreeId());
		}
		catch (ObjectNotFoundException e) { e.printStackTrace(); }
		finally { hibr.closeSession(hs); }

		signetSubsys.setTree(dbTree);
	}
	
}
