/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/GrantableImplXa.java,v 1.2 2007-10-19 23:27:11 ddonn Exp $

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

import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.GrantableImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.util.xml.binder.GrantableImplXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetSubjectRefXb;

/**
 * GrantableImplXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a GrantableImpl and a GrantableImplXb
 * @see GrantableImpl
 * @see GrantableImplXb
 */
public abstract class GrantableImplXa extends EntityImplXa
{

	protected GrantableImplXa()
	{
		super();
	}

	protected GrantableImplXa(Signet signet)
	{
		super(signet);
	}

	public void setValues(GrantableImpl signetGrantable)
	{
		super.setValues(signetGrantable);

		GrantableImplXb xmlGrantable = (GrantableImplXb)xmlEntity;

//	protected SignetSubject	grantor;
		SignetSubject subj;
		SignetSubjectRefXa subjectAdapter;
		if (null != (subj = signetGrantable.getGrantor()))
		{
			subjectAdapter = new SignetSubjectRefXa(subj, signet);
			xmlGrantable.setGrantor(subjectAdapter.getXmlSubject());
		}

//	protected SignetSubject	proxy;
		if (null != (subj = signetGrantable.getProxy()))
		{
			subjectAdapter = new SignetSubjectRefXa(subj, signet);
			xmlGrantable.setActingAs(subjectAdapter.getXmlSubject());
		}

//	protected SignetSubject	grantee;
		if (null != (subj = signetGrantable.getGrantee()))
		{
			subjectAdapter = new SignetSubjectRefXa(subj, signet);
			xmlGrantable.setGrantee(subjectAdapter.getXmlSubject());
		}

//	protected SignetSubject	revoker;
		if (null != (subj = signetGrantable.getRevoker()))
		{
			subjectAdapter = new SignetSubjectRefXa(subj, signet);
			xmlGrantable.setRevoker(subjectAdapter.getXmlSubject());
		}

//	protected Date		    effectiveDate;
		xmlGrantable.setEffectiveDate(Util.convertDateToString(signetGrantable.getEffectiveDate()));

//	protected Date    		expirationDate;
		xmlGrantable.setExpirationDate(Util.convertDateToString(signetGrantable.getExpirationDate()));

//	protected int			instanceNumber;
		xmlGrantable.setInstanceNumber(signetGrantable.getInstanceNumber());

//		private Integer			id;
		// a signet Grantable contains an Integer id, overriding the String id
		// in Entity. Only one id field can be used in these XML objects, so a
		// conversion is necessary. The xml.setId here must be called after the
		// call to super.setValues().
		xmlGrantable.setId(signetGrantable.getId().toString());
	}


	public void setValues(GrantableImplXb xmlGrantable)
	{
		super.setValues(xmlGrantable);

		GrantableImpl signetGrantable = (GrantableImpl)signetEntity;

//	protected SignetSubject	grantor;
		SignetSubjectRefXb xbSubject;
		SignetSubject subject;
		if (null != (xbSubject = xmlGrantable.getGrantor()))
		{
			subject = signet.getSubject(xbSubject.getSourceId(), xbSubject.getSubjectId());
			signetGrantable.setGrantor(subject);
		}

//	protected SignetSubject	grantee;
		if (null != (xbSubject = xmlGrantable.getGrantee()))
		{
			subject = signet.getSubject(xbSubject.getSourceId(), xbSubject.getSubjectId());
			signetGrantable.setGrantee(subject);
		}

//	protected SignetSubject	proxy;
		if (null != (xbSubject = xmlGrantable.getActingAs()))
		{
			subject = signet.getSubject(xbSubject.getSourceId(), xbSubject.getSubjectId());
			signetGrantable.setProxy(subject);
		}

//	protected SignetSubject	revoker;
		if (null != (xbSubject = xmlGrantable.getRevoker()))
		{
			subject = signet.getSubject(xbSubject.getSourceId(), xbSubject.getSubjectId());
			signetGrantable.setRevoker(subject);
		}

//	protected Date		    effectiveDate;
//	protected Date    		expirationDate;
		try
		{
			signetGrantable.setEffectiveDate(
					signetGrantable.getGrantor(),
					Util.convertStringToDate(xmlGrantable.getEffectiveDate()),
					false);
			signetGrantable.setExpirationDate(
					signetGrantable.getGrantor(),
					Util.convertStringToDate(xmlGrantable.getExpirationDate()),
					false);
		}
		catch (SignetAuthorityException e)
		{
			e.printStackTrace();
		}

//	protected int			instanceNumber;
		signetGrantable.setInstanceNumber(xmlGrantable.getInstanceNumber());

//		private Integet		id;
		// See comment regarding "id" field in this.setValues(GrantableImpl)
//		try
//		{
//			signetGrantable.setId(Integer.parseInt(xmlGrantable.getId()));
//		}
//		catch (NumberFormatException nfe)
//		{
//			nfe.printStackTrace();
//		}
	}

}
