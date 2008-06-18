/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/XmlUtil.java,v 1.3 2008-06-18 01:21:39 ddonn Exp $

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
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;

/**
 * XmlUtil - A utility super class to support Signet XML processing
 * 
 */
public abstract class XmlUtil
{
	/////////////////////////
	// statics
	/////////////////////////

	/** context path used by JAXB */
	public static final String JAXB_CONTEXT_PATH;

	/** static initializer for JAXB_CONTEXT_PATH */
	static
	{
		JAXB_CONTEXT_PATH = ResLoaderApp.getString("Signet.jaxb.context.path");
	}

	//////////////////////////
	// members
	//////////////////////////

	/** logging */
	protected Log		log;
	/** An instance of Signet */
	protected Signet	signet;
	/** An instance of SignetXa */
	protected SignetXa	signetXmlAdapter;


	//////////////////////////////
	// utility methods
	//////////////////////////////

	/**
	 * Marshal (output) the XML to the output stream
	 * @param signetXb An instance of SignetXb
	 * @param fos The output destination
	 */
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


	/**
	 * Parse a comma-separated list into an String array
	 * @param str 
	 * @return An array of Strings
	 */
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

	/**
	 * Parse a colon-separated pair of words into a String array
	 * @param str The colon-separated pair to parse
	 * @return An array of two Strings
	 */
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

}
