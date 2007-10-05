/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/SignetSubjectXa.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.subjsrc.SignetSubjectAttr;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentImplRefXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.ProxyImplRefXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetSubjectAttrXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetSubjectXb;

/**
 * SignetSubjectXa - An adapter class for Signet's SignetSubject and an
 * XML binder (SignetSubjectXb).
 * @see edu.internet2.middleware.signet.subjsrc.SignetSubject SignetSubject
 * @see edu.internet2.middleware.signet.util.xml.binder.SignetSubjectXb SignetSubjectXb
 */
public class SignetSubjectXa extends SignetSubjectRefXa
{
	public SignetSubjectXa()
	{
	}

	public SignetSubjectXa(SignetSubject signetSubject, Signet signet)
	{
		super(signetSubject, signet);
		xmlSubject = new ObjectFactory().createSignetSubjectXb();
		setValues(signetSubject);
	}

	public SignetSubjectXa(SignetSubjectXb xmlSubject, Signet signet)
	{
		super(signet);
		// super(SignetSubjectRefXb, Signet) attempts to do a DB lookup for the
		// Subject, so just call super(signet) and do everything else here, manually.
		this.xmlSubject = xmlSubject;
		signetSubject = new SignetSubject();
		setValues(xmlSubject);
	}


	public SignetSubject getSignetSubject()
	{
		return (signetSubject);
	}


	public void setValues(SignetSubject signetSubject)
	{
		super.setValues(signetSubject);

		SignetSubjectXb xmlSubject = (SignetSubjectXb)this.xmlSubject;

//	protected SignetSubject			actingAs;
		SignetSubject tmpSubj = signetSubject.getActingAs();
		if (null != tmpSubj)
			xmlSubject.setActingAs(new SignetSubjectRefXa(tmpSubj, signet).getXmlSubject());
		else
			xmlSubject.setActingAs(null);

//	protected Set<AssignmentImpl>	assignmentsGranted;
		List<AssignmentImplRefXb> assignList = xmlSubject.getAssignmentsGranted();
		for (AssignmentImpl grant : signetSubject.getAssignmentsGranted())
			assignList.add(new AssignmentImplRefXa(grant, signet).getXmlAssignmentRef());

//	protected Set<AssignmentImpl>	assignmentsReceived;
		assignList = xmlSubject.getAssignmentsReceived();
		for (AssignmentImpl rcvd : signetSubject.getAssignmentsReceived())
			assignList.add(new AssignmentImplRefXa(rcvd, signet).getXmlAssignmentRef());

//	protected Set<ProxyImpl>		proxiesGranted;
		List<ProxyImplRefXb> proxyList = xmlSubject.getProxiesGranted();
		for (ProxyImpl grant : signetSubject.getProxiesGranted())
			proxyList.add(new ProxyImplRefXa(grant, signet).getXmlProxyRef());

//	protected Set<ProxyImpl>		proxiesReceived;
		proxyList = xmlSubject.getProxiesReceived();
		for (ProxyImpl rcvd : signetSubject.getProxiesReceived())
			proxyList.add(new ProxyImplRefXa(rcvd, signet).getXmlProxyRef());
	}


	public SignetSubjectXb getXmlSubject()
	{
		return ((SignetSubjectXb)xmlSubject);
	}


	public void setValues(SignetSubjectXb xmlSubject)
	{
		// super.setValues(xmlSubject) attempts to fetch the subject from the
		// DB, so update everything manually here
//	protected Long			subject_PK;
		signetSubject.setSubject_PK(xmlSubject.getSubjectPK());

//	protected String		subjectId;
		signetSubject.setId(xmlSubject.getSubjectId());

//	protected SignetSource	signetSource;
//	protected String		sourceId;
		signetSubject.setSourceId(xmlSubject.getSubjectId());
		signetSubject.refreshSource(signet);

//	protected String		subjectType;
		signetSubject.setSubjectType(xmlSubject.getSubjectType());

//	protected String		subjectName;
		signetSubject.setName(xmlSubject.getSubjectName());

//	protected Date			modifyDatetime;
		signetSubject.setModifyDatetime(Util.convertStringToDate(xmlSubject.getModifyDatetime()));

//	protected Date			synchDatetime;
		signetSubject.setSynchDatetime(Util.convertStringToDate(xmlSubject.getSynchDatetime()));

//	protected SignetSubject	actingAs;
		SignetSubject actingAs = null;
		if (null != xmlSubject.getActingAs())
		{
			SignetSubjectRefXa actAsAdapter = new SignetSubjectRefXa(xmlSubject.getActingAs(), signet);
			actingAs = actAsAdapter.getSignetSubject();
		}
		try { signetSubject.setActingAs(actingAs); }
		catch (SignetAuthorityException e) { log.warn(e); }

//	protected Set<AssignmentImpl>	assignmentsGranted;
		for (AssignmentImplRefXb xmlAssign : xmlSubject.getAssignmentsGranted())
		{
			AssignmentImpl sigAssign = new AssignmentImplRefXa(xmlAssign, signet).getSignetAssignment();
			signetSubject.getAssignmentsGranted().add(sigAssign);
		}

//	protected Set<AssignmentImpl>	assignmentsReceived;
		for (AssignmentImplRefXb xmlRcvd : xmlSubject.getAssignmentsReceived())
		{
			AssignmentImpl sigAssign = new AssignmentImplRefXa(xmlRcvd, signet).getSignetAssignment();
			signetSubject.getAssignmentsReceived().add(sigAssign);
		}

//	protected Set<ProxyImpl>		proxiesGranted;
		for (ProxyImplRefXb xmlGrant : xmlSubject.getProxiesGranted())
		{
			ProxyImpl sigProxy = new ProxyImplRefXa(xmlGrant, signet).getSignetProxy();
			signetSubject.getProxiesGranted().add(sigProxy);
		}

//	protected Set<ProxyImpl>		proxiesReceived;
		for (ProxyImplRefXb xmlRcvd : xmlSubject.getProxiesReceived())
		{
			ProxyImpl sigProxy = new ProxyImplRefXa(xmlRcvd, signet).getSignetProxy();
			signetSubject.getProxiesReceived().add(sigProxy);
		}

//	protected Set<SignetSubjectAttr>	signetSubjectAttrs;
		for (SignetSubjectAttrXb attr : xmlSubject.getSignetSubjectAttrs())
		{
			SignetSubjectAttr sigAttr = new SignetSubjectAttrXa(attr).getSignetSubjectAttr();
			sigAttr.setParent(signetSubject);
			signetSubject.addAttribute(sigAttr);
		}
	}

}
