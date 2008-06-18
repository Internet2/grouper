/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/XmlImporter.java,v 1.2 2008-06-18 01:21:39 ddonn Exp $

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
package edu.internet2.middleware.signet.util.xml;

import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.logging.Log;
import org.hibernate.Session;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.TreeImpl;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.util.xml.adapter.AssignmentImplXa;
import edu.internet2.middleware.signet.util.xml.adapter.ProxyImplXa;
import edu.internet2.middleware.signet.util.xml.adapter.ScopeTreeXa;
import edu.internet2.middleware.signet.util.xml.adapter.SignetSubjectXa;
import edu.internet2.middleware.signet.util.xml.adapter.SubsystemImplXa;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentImplXb;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentSetXb;
import edu.internet2.middleware.signet.util.xml.binder.ProxyImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ScopeTreeSetXb;
import edu.internet2.middleware.signet.util.xml.binder.ScopeTreeXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetSubjectSetXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetSubjectXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;
import edu.internet2.middleware.signet.util.xml.binder.SubsystemImplXb;
import edu.internet2.middleware.signet.util.xml.binder.SubsystemSetXb;

/**
 * XmlImporter 
 * 
 */
public class XmlImporter
{
	protected Log			log;
	protected Signet		signet;
	protected InputStream	inFile;


	private XmlImporter()
	{
	}

	public XmlImporter(Signet signet, Log log, InputStream inFile)
	{
		this.log = log;
		this.signet = signet;
		this.inFile = inFile;
	}


	public void importXml(CommandArg cmd)
	{
		SignetXb signetXml = unmarshalSignet(cmd);
		if (null == signetXml)
			return;

		String cmdType = cmd.getType();
		if (cmdType.equals(CommandArg.IM_ADD) || cmdType.equals(CommandArg.IM_UPD))
		{
			updateXml(signetXml);
		}
		else if (cmdType.equals(CommandArg.IM_DEACT))
		{
log.info("Import+deactivate not supported yet");
		}
		else if (cmdType.equals(CommandArg.IM_DEL))
		{
log.info("Import+delete not supported yet");
		}
		else
			log.error("Unknown CommandArg Type: \"" + cmdType + "\"");
	}

	protected SignetXb unmarshalSignet(CommandArg cmd)
	{
		SignetXb signetXml = null;

		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(XmlUtil.JAXB_CONTEXT_PATH);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			signetXml =((JAXBElement<SignetXb>)unmarshaller.unmarshal(inFile)).getValue();
		}
		catch (JAXBException ejax)
		{
			ejax.printStackTrace();
		}

		return (signetXml);
	}

	protected void updateXml(SignetXb signetXml)
	{
		updateScopeTrees(signetXml.getScopeTreeSet());
		updateSubsystems(signetXml.getSubsystemSet());
		updateSubjects(signetXml.getSubjectSet());
//		updateProxies(signetXml.getProxy());
		updateAssignments(signetXml.getAssignmentSet());
	}

	protected void updateScopeTrees(ScopeTreeSetXb scopeTreeSet)
	{
		if (null == scopeTreeSet)
			return;

		List<TreeImpl> signetTrees = new Vector<TreeImpl>();
		for (ScopeTreeXb xmlTree : scopeTreeSet.getScopeTree())
		{
			TreeImpl signetTree = new ScopeTreeXa(xmlTree, signet).getSignetScopeTree();
			signetTrees.add(signetTree);
System.out.println("SignetXml.updateScopeTrees: ScopeTree=" + signetTree);
		}

		updateSignetObjects(signetTrees);
	}

	protected void updateSubsystems(SubsystemSetXb subsystemSet)
	{
		if (null == subsystemSet)
			return;

		List<SubsystemImpl> signetSubsystems = new Vector<SubsystemImpl>();
		for (SubsystemImplXb xmlSubsys : subsystemSet.getSubsystem())
		{
			SubsystemImpl sigSubsys = new SubsystemImplXa(xmlSubsys, signet).getSignetSubsystem();
			signetSubsystems.add(sigSubsys);
System.out.println("SignetXml.updateSubsystems: Subsystem=" + sigSubsys);
		}

		updateSignetObjects(signetSubsystems);
	}

	protected void updateSubjects(SignetSubjectSetXb subjectSet)
	{
		if (null == subjectSet)
			return;

		List<SignetSubject> sigSubjects = new Vector<SignetSubject>();
		for (SignetSubjectXb xmlSubject : subjectSet.getSubject())
		{
			SignetSubject sigSubject = new SignetSubjectXa(xmlSubject, signet).getSignetSubject();
			sigSubjects.add(sigSubject);
System.out.println("SignetXml.updateSubjects: Subject=" + sigSubject);
		}

		updateSignetObjects(sigSubjects);
	}

	protected void updateProxies(List<ProxyImplXb> proxies)
	{
		if (null == proxies)
			return;

		List<ProxyImpl> sigProxies = new Vector<ProxyImpl>();
		for (ProxyImplXb xmlProxy : proxies)
		{
			ProxyImpl sigProxy = new ProxyImplXa(xmlProxy, signet).getSignetProxy();
			sigProxies.add(sigProxy);
System.out.println("SignetXml.updateProxies: Proxy=" + sigProxy);
		}

		updateSignetObjects(sigProxies);
	}

	protected void updateAssignments(AssignmentSetXb assignSet)
	{
		if (null == assignSet)
			return;

		List<AssignmentImpl> signetAssigns = new Vector<AssignmentImpl>();
		for (AssignmentImplXb xmlAssign : assignSet.getAssignment())
		{
			AssignmentImpl signetAssign = 
				new AssignmentImplXa(xmlAssign, signet).getSignetAssignment();
			signetAssigns.add(signetAssign);
System.out.println("SignetXml.importAssignments: assignment=" + signetAssign);
		}

		updateSignetObjects(signetAssigns);
	}

	protected void updateSignetObjects(List signetObjects)
	{
		HibernateDB hibr = signet.getPersistentDB();
		Session hs = hibr.openSession();
		hibr.save(hs, signetObjects);
		hibr.closeSession(hs);
	}


	/**
	 * @param signetXml
	 * @param signet
	 */
	public void importSubjects(SignetXb signetXml, Signet signet)
	{
		SignetSubjectSetXb subjSet = signetXml.getSubjectSet();
		if (null == subjSet)
			return;

		for (SignetSubjectXb xmlSubj : subjSet.getSubject())
		{
			SignetSubjectXa adapter = new SignetSubjectXa(xmlSubj, signet);
System.out.println("SignetXml.importSubjects: subject=" + adapter.getSignetSubject().toString());
System.out.println("  importSubjects not implemented yet");
		}
	}


	/**
	 * @param signetXml
	 * @param signet
	 */
	public void importAssignments(SignetXb signetXml, Signet signet)
	{
		AssignmentSetXb xmlAssignSet = signetXml.getAssignmentSet();
		if (null == xmlAssignSet)
			return;

		List<AssignmentImpl> signetAssignments = new Vector<AssignmentImpl>();

		for (AssignmentImplXb xmlAssign : xmlAssignSet.getAssignment())
		{
			AssignmentImpl signetAssignment = 
				new AssignmentImplXa(xmlAssign, signet).getSignetAssignment();
			signetAssignments.add(signetAssignment);
System.out.println("SignetXml.importAssignments: assignment=" + signetAssignment);
		}

		HibernateDB hibr = signet.getPersistentDB();
		Session hs = hibr.openSession();
		hibr.save(hs, signetAssignments);
		hibr.closeSession(hs);
	}


	/**
	 * @param signetXml
	 */
	public void importProxies(SignetXb signetXml)
	{
System.out.println("SignetXml.importProxies: not implemented yet.");
//TODO Import proxies
//		for (ProxyImplXb xmlProxy : signetXml.getProxy())
//		{
//			ProxyImplXa adapter = new ProxyImplXa(xmlProxy);
//System.out.println("SignetXml.importProxies: proxy=" + adapter.getSignetProxy());
//		}
	}

}
