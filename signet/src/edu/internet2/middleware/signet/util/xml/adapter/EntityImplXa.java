/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/EntityImplXa.java,v 1.2 2007-10-19 23:27:11 ddonn Exp $

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.EntityImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.util.xml.binder.EntityImplXb;

/**
 * EntityImplXa<p>
 * Adapter class for Signet XML Binding.
 * Maps an EntityImpl and an EntityImplXb.
 * @see EntityImpl
 * @see EntityImplXb
 */
public abstract class EntityImplXa
{
	protected Signet		signet;
	protected EntityImpl	signetEntity;
	protected EntityImplXb	xmlEntity;
	protected Log			log;


	protected EntityImplXa()
	{
		initLog();
	}

	protected EntityImplXa(Signet signet)
	{
		initLog();
		this.signet = signet;
	}

	protected void initLog()
	{
		log = LogFactory.getLog(this.getClass());
	}

	public void setValues(EntityImpl signetEntity)
	{
//    protected String status;
		xmlEntity.setStatus(signetEntity.getStatus().toString());

//    protected String comment;
		xmlEntity.setComment(signetEntity.getComment());
		
//    protected XMLGregorianCalendar createDatetime;
		xmlEntity.setCreateDatetime(Util.convertDateToString(signetEntity.getCreateDatetime()));

//    protected XMLGregorianCalendar modifyDatetime;
		xmlEntity.setModifyDatetime(Util.convertDateToString(signetEntity.getModifyDatetime()));

//    protected String createDbAccount;
		xmlEntity.setCreateDbAccount(signetEntity.getCreateDbAccount());

//    protected String modifyDbAccount;
		xmlEntity.setModifyDbAccount(signetEntity.getModifyDbAccount());

//    protected String createContext;
		xmlEntity.setCreateContext(signetEntity.getCreateContext());

//    protected String modifyContext;
		xmlEntity.setModifyContext(signetEntity.getModifyContext());

//    protected String createUserID;
		xmlEntity.setCreateUserID(signetEntity.getCreateUserID());

//    protected String modifyUserID;
		xmlEntity.setModifyUserID(signetEntity.getModifyUserID());

//    protected String id;
		xmlEntity.setId(signetEntity.getStringId());

//    protected String name;
		xmlEntity.setName(signetEntity.getName());
	}


	public void setValues(EntityImplXb xmlEntity)
	{
//    protected String status;
		signetEntity.setStatus((Status)Status.getInstanceByName(xmlEntity.getStatus()));

//    protected String comment;
		signetEntity.setComment(xmlEntity.getComment());
		
//    protected XMLGregorianCalendar createDatetime;
		signetEntity.setCreateDatetime(Util.convertStringToDate(xmlEntity.getCreateDatetime()));

//    protected XMLGregorianCalendar modifyDatetime;
		signetEntity.setModifyDatetime(Util.convertStringToDate(xmlEntity.getModifyDatetime()));

//    protected String createDbAccount;
		signetEntity.setCreateDbAccount(xmlEntity.getCreateDbAccount());

//    protected String modifyDbAccount;
		signetEntity.setModifyDbAccount(xmlEntity.getModifyDbAccount());

//    protected String createContext;
		signetEntity.setCreateContext(xmlEntity.getCreateContext());

//    protected String modifyContext;
		signetEntity.setModifyContext(xmlEntity.getModifyContext());

//    protected String createUserID;
		signetEntity.setCreateUserID(xmlEntity.getCreateUserID());

//    protected String modifyUserID;
		signetEntity.setModifyUserID(xmlEntity.getModifyUserID());

//    protected String id;
		signetEntity.setStringId(xmlEntity.getId());

//    protected String name;
		signetEntity.setName(xmlEntity.getName());
	}

}
