/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

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

package edu.internet2.middleware.grouper.ui.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;

/**
 * Class that reads an XML configuration file and hands off actual data import
 * to an implementation associated with the selected import format. The configuration 
 * file is determined by looking up the media.properties key 'membership-import.config'.
 * The base Grouper UI distribution does not have a value set. If you want to allow membership
 * import you must configure one or more formats appropriate to your site. 
 * <pre>
&lt;membership-import&gt;
    &lt;format name=&quot;Tab separated&quot; separator=&quot;\t&quot; id-field=&quot;1&quot;  field-type=&quot;id&quot;
               ignore-existing=&quot;true&quot; 
               class=&quot;edu.internet2.middleware.grouper.ui.util.DefaultMembershipImporter&quot;/&gt;
    &lt;format name=&quot;Comma separated&quot; separator=&quot;,&quot; id-field=&quot;1&quot;  field-type=&quot;id&quot;
               ignore-existing=&quot;true&quot; 
               class=&quot;edu.internet2.middleware.grouper.ui.util.DefaultMembershipImporter&quot;/&gt;
&lt;/membership-import&gt;  </pre>
<p><strong>ignore-existing=true</strong> indicates that no error should be reported if an imported Subject is already a member of the group<br/>
<strong>class</strong> specifies the implementation for this format<br/>
<strong>name</strong> is the text that users will see and select - unless there is only one format, in
which case it will be used as a default.</p>
<p>Other attributes are interpreted by the implementation class. There is no formal DTD/Schema.</p> 
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: MembershipImportManager.java,v 1.6 2009-10-16 10:30:08 isgwb Exp $
 */
public class MembershipImportManager implements Serializable{
	private boolean active=false;
	//MCH 20090811 XXX take this out of session it is not serializable
	private transient Document configXml;
	private transient Map formatCache = new HashMap();

	
	/**
	 * @param config - media.properties. The key 'membership-import.config' defines XML configuratoin file
	 * @param nav - nav.properties. Provides localized messages
	 * @throws Exception
	 */
	public MembershipImportManager() throws Exception{
		super();
		init();
		
	}
	
	private void init() throws Exception{
		if(configXml != null) {
			return;
		}
		String configResource = null;
		try {
			configResource=GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("membership-import.config");
		}catch(MissingResourceException e){
			return;
		}
		try {
			configXml = DOMHelper.getDomFromResourceOnClassPath(configResource);
		}catch(Exception e){}
		List available = getAvailableFormats();
		if(!available.isEmpty()) active=true;
	}
	
	/**
	 * Returns true if the XML configuration file exists and there is at least
	 * one import format defined. 
	 * @return whether the UI should present import controls
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Returns the configured import formats
	 * @return list of import format names
	 */
	public List getAvailableFormats() throws Exception{
		List res = new ArrayList();
		init();
		NodeList nl = configXml.getElementsByTagName("format");
		Element el =null;
		for (int i=0; i<nl.getLength();i++) {
			el = (Element) nl.item(i);
			res.add(el.getAttribute("name"));
			formatCache.put(el.getAttribute("name"),el);
		}
		return res;
	}
	
	/**
	 * Looks up the configuration for the supplied format, instantiates the
	 * configured implemetation class and calls its 'load' method
	 * @param format
	 * @param group
	 * @param input
	 * @param output
	 * @param field
	 * @return count of errors encountered during the load
	 * @throws IOException
	 * @throws SchemaException
	 */
	public int load(String format,Group group, Reader input, PrintWriter output,Field field) throws Exception,IOException,SchemaException{
		init();
		MembershipImporter importer=null;
		Element fe = getFormat(format);
		try {
			importer = (MembershipImporter)Class.forName(fe.getAttribute("class")).newInstance();
		}catch(Exception e) {
			throw new IllegalArgumentException("Could not instantiate importer class [" + fe.getAttribute("class") + "]");
		}
		return importer.load(group,input,output,fe,field);
	}
	
	private Element getFormat(String name) throws Exception{
		init();
		return (Element)formatCache.get(name);
	}

}
