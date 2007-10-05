/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/CategoryImplXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp

*/
package com.lidoservices.jaxbtest.stubs;

import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * CategoryImplXb 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CategoryImplXb", propOrder = {
})
public class CategoryImplXb extends EntityImplXb
{
  /** This field is a simple synthetic key for this record in the database. */
	@XmlAttribute(name="category_PK", required=true)
	protected Integer				key;

}
