/*
	$Header: /home/hagleyj/i2mi/signet/util/SignetXml/xmlstubs/com/lidoservices/jaxbtest/stubs/ScopeTreeXb.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

Copyright (c) 2007 Lido Services Corp.
*/
package com.lidoservices.jaxbtest.stubs;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * ScopeTreeXb
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScopeTreeXb", propOrder = {
    "adapterClassName",
    "rootNodes"
})
public class ScopeTreeXb extends EntityImplXb
{
	@XmlElement(name="rootNodes", required=false)
	protected List<TreeNodeImplXb>	rootNodes;

	@XmlElement(name="adapterClassName", required=false)
	protected String				adapterClassName;

}
