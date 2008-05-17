/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/XmlUtil.java,v 1.2 2008-05-17 20:54:09 ddonn Exp $

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.commons.logging.Log;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;

/**
 * XmlUtil 
 * 
 */
public abstract class XmlUtil
{
	public static final String JAXB_CONTEXT_PATH = "edu.internet2.middleware.signet.util.xml.binder";

	protected Log		log;
	protected Signet	signet;


	//////////////////////////////
	// utility methods
	//////////////////////////////

	protected void marshalXml(SignetXb signetXb, OutputStream fos)
	{
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CONTEXT_PATH);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

			ObjectFactory factory = new ObjectFactory();
			JAXBElement<SignetXb> element = factory.createSignet(signetXb);
			marshaller.marshal(element, fos);
			fos.flush();
		}
		catch (JAXBException ejax) { ejax.printStackTrace(); }
		catch (FileNotFoundException efnf) { efnf.printStackTrace(); }
		catch (IOException eio) { eio.printStackTrace(); }
	}


	/** parse a comma-separated list into an String array */
	protected String[] parseList(String str)
	{
		String[] retval;

		if (null != str)
			retval = str.split(",");
		else
			retval = new String[0];

		for (int i = 0; i < retval.length; i++)
			retval[i] = retval[i].trim();

		return (retval);
	}

	/** parse a colon-separated pair of words into a String array */
	protected String[] parsePair(String str)
	{
		String[] retval;

		if (null != str)
		{
			retval = str.split(":");
			if (1 == retval.length)
				retval = new String[] {null, retval[1]};
		}
		else
			retval = new String[] {null, null};

		for (int i = 0; i < retval.length; i++)
			retval[i] = retval[i].trim();

		return (retval);
	}


	/////////////////////////
	// static methods
	/////////////////////////

	/** Split a command of the form xxx=yyy into a string array */
	public static String[] expandCommand(String cmd, String defValue)
	{
		String[] retval = new String[] {null, null};
		if (null == cmd)
			return (retval);

		String[] expandedCmd = cmd.split(Command.EQUALS);
		if (null != expandedCmd)
		{
			retval[0] = expandedCmd[0];
			if (2 <= expandedCmd.length)
				retval[1] = expandedCmd[1];
			else
				retval[1] = defValue;
		}
		return (retval);
	}



}
