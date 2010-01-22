/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/ChoiceImplXa.java,v 1.1 2008-05-17 20:54:09 ddonn Exp $

Copyright (c) 2008 Internet2, Stanford University

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

import edu.internet2.middleware.signet.ChoiceImpl;
import edu.internet2.middleware.signet.ChoiceSetImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.util.xml.binder.ChoiceImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * ChoiceImplXa 
 * 
 */
public class ChoiceImplXa
{
	protected Signet		signet;
	protected ChoiceImpl	signetChoiceImpl;
	protected ChoiceImplXb	xmlChoiceImpl;


	public ChoiceImplXa()
	{
	}

	public ChoiceImplXa(Signet signet)
	{
		this.signet = signet;
	}

	public ChoiceImplXa(ChoiceImpl signetChoice, Signet signet)
	{
		this(signet);
		signetChoiceImpl = signetChoice;
		xmlChoiceImpl = new ObjectFactory().createChoiceImplXb();
		setValues(signetChoice);
	}

	public ChoiceImplXa(ChoiceImplXb xmlChoice, Signet signet)
	{
		this(signet);
		xmlChoiceImpl = xmlChoice;
		signetChoiceImpl = new ChoiceImpl();
		setValues(xmlChoice);
	}

	
	public ChoiceImpl getSignetChoiceImpl()
	{
		return (signetChoiceImpl);
	}

	public void setValues(ChoiceImpl signetChoice)
	{
//	private Integer   key;
		xmlChoiceImpl.setKey(signetChoice.getKey().intValue());

//	private ChoiceSet choiceSet;
		xmlChoiceImpl.setChoiceSetKey(((ChoiceSetImpl)signetChoice.getChoiceSet()).getKey().intValue());

//	private int       displayOrder;
		xmlChoiceImpl.setDisplayOrder(signetChoice.getDisplayOrder());

//	private String    displayValue;
		xmlChoiceImpl.setDisplayValue(signetChoice.getDisplayValue());

//	private int       rank;
		xmlChoiceImpl.setRank(signetChoice.getRank());

//	private String    value;
		xmlChoiceImpl.setValue(signetChoice.getValue());

//	private Date	modifyDatetime;
		xmlChoiceImpl.setModifyDatetime(Util.convertDateToString(signetChoice.getModifyDatetime()));
	}


	public ChoiceImplXb getXmlChoiceImpl()
	{
		return (xmlChoiceImpl);
	}

	public void setValues(ChoiceImplXb xmlChoice)
	{
//	private Integer   key;
		signetChoiceImpl.setKey(new Integer(xmlChoice.getKey()));
//	private ChoiceSet choiceSet;
//TODO - Set the value of ChoiceSet
System.out.println("ChoiceImplXa.setValues(ChoiceImplXb) - setting ChoiceSet value not implemented yet");
		
//	private int       displayOrder;
		signetChoiceImpl.setDisplayOrder(xmlChoice.getDisplayOrder());

//	private String    displayValue;
		signetChoiceImpl.setDisplayValue(xmlChoice.getDisplayValue());

//	private int       rank;
		signetChoiceImpl.setRank(xmlChoice.getRank());

//	private String    value;
		signetChoiceImpl.setValue(xmlChoice.getValue());

//	private Date	modifyDatetime;
		signetChoiceImpl.setModifyDatetime(
				Util.convertStringToDate(xmlChoice.getModifyDatetime()));
	}

}
