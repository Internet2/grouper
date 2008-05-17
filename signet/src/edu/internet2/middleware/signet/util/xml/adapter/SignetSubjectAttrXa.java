/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/SignetSubjectAttrXa.java,v 1.3 2008-05-17 20:54:09 ddonn Exp $

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

import edu.internet2.middleware.signet.subjsrc.SignetSubjectAttr;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.SignetSubjectAttrXb;

/**
 * SignetSubjectAttrXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a SignetSubjectAttr and a SignetSubjectAttrXb.
 * @see SignetSubjectAttr
 * @see SignetSubjectAttrXb
 */
public class SignetSubjectAttrXa
{
//	protected Signet					signet;
	protected SignetSubjectAttr			signetSubjectAttr;
//	protected Vector<SignetSubjectAttr> signetSubjectAttrs;
	protected SignetSubjectAttrXb		xmlSubjectAttr;


	public SignetSubjectAttrXa(SignetSubjectAttr signetSubjectAttr)
	{
		this.signetSubjectAttr = signetSubjectAttr;
		this.xmlSubjectAttr = new ObjectFactory().createSignetSubjectAttrXb();
		setValues(signetSubjectAttr);
	}

	public SignetSubjectAttrXa(SignetSubjectAttrXb xmlSubjectAttr)
	{
		this.xmlSubjectAttr = xmlSubjectAttr;
		signetSubjectAttr = new SignetSubjectAttr();
		setValues(xmlSubjectAttr);
	}

	public SignetSubjectAttr getSignetSubjectAttr()
	{
		return (signetSubjectAttr);
	}

	public SignetSubjectAttrXb getXmlSubjectAttr()
	{
		return (xmlSubjectAttr);
	}


	public void setValues(SignetSubjectAttr signetAttr)
	{
		xmlSubjectAttr.setKey(signetAttr.getSubjectAttr_PK().longValue());
		xmlSubjectAttr.setName(signetAttr.getMappedName());
		xmlSubjectAttr.setValue(signetAttr.getValue());
		xmlSubjectAttr.setType(signetAttr.getType());
		xmlSubjectAttr.setModifyDate(Util.convertDateToString(signetAttr.getModifyDate()));
		xmlSubjectAttr.setSequence(signetAttr.getSequence());
	}

	public void setValues(SignetSubjectAttrXb xmlAttr)
	{
		signetSubjectAttr.setSubjectAttr_PK(new Long(xmlAttr.getKey()));
		signetSubjectAttr.setMappedName(xmlAttr.getName());
		signetSubjectAttr.setValue(xmlAttr.getValue(), xmlAttr.getType());
		signetSubjectAttr.setModifyDate(Util.convertStringToDate(xmlAttr.getModifyDate()));
		signetSubjectAttr.setSequence(xmlAttr.getSequence());
	}

//	public SignetSubjectAttrXa(Vector<SignetSubjectAttr> signetSubjectAttrs)
//	{
//		this.signetSubjectAttrs = signetSubjectAttrs;
//		this.xmlSubjectAttr = new ObjectFactory().createSignetSubjectAttrXb();
//		setValues(signetSubjectAttrs);
//	}
//
//	public SignetSubjectAttrXa(SignetSubjectAttrXb xmlSubjectAttr)
//	{
//		this.xmlSubjectAttr = xmlSubjectAttr;
//		signetSubjectAttrs = new Vector<SignetSubjectAttr>();
//		setValues(xmlSubjectAttr);
//	}
//
//
//	public Vector<SignetSubjectAttr> getSignetSubjectAttrs()
//	{
//		return (signetSubjectAttrs);
//	}
//
//	public SignetSubjectAttrXb getXmlSubjectAttr()
//	{
//		return (xmlSubjectAttr);
//	}
//
//
//	public void setValues(Vector<SignetSubjectAttr> signetSubjectAttrs)
//	{
//		if (null == signetSubjectAttrs)
//			return;
//
//		// Assume all attributes in the Vector are of the same type, name, ...
//		SignetSubjectAttr firstAttr = signetSubjectAttrs.firstElement();
//		if (null == firstAttr)
//			return;
//
//		xmlSubjectAttr.setAttrType(firstAttr.getType());
//
//		xmlSubjectAttr.setMappedName(firstAttr.getMappedName());
//
//		xmlSubjectAttr.setModifyDate(Util.convertDateToString(firstAttr.getModifyDate()));
//
//		// Collapse multiple SignetSubjectAttr of the same type, name, etc
//		// into a single SignetSubjectAttrXb with multiple values
//		List<String> xmlValues = xmlSubjectAttr.getAttrValues();
//		for (SignetSubjectAttr attr : signetSubjectAttrs)
//		{
//			xmlValues.add(attr.getValue());
//		}
//	}
//
//	public void setValues(SignetSubjectAttrXb signetSubjectAttrXb)
//	{
//		// Assume all attributes derived from the XML attribute have the
//		// same type, name, etc. Only the values vary.
//		String type = signetSubjectAttrXb.getAttrType();
//		String mappedName = signetSubjectAttrXb.getMappedName();
//		Date modDate = Util.convertStringToDate(signetSubjectAttrXb.getModifyDate());
//		Long pk = signetSubjectAttrXb.getSubjectAttrPK();
//
//		List<String> values = signetSubjectAttrXb.getAttrValues();
//		for (Iterator<String> iter = values.iterator(); iter.hasNext(); )
//		{
//			SignetSubjectAttr sigAttr = new SignetSubjectAttr(
//		}
//
//	}

}
