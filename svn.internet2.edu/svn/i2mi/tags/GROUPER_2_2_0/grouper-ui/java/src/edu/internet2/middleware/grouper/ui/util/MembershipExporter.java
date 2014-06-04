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
import java.io.Serializable;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Reads an XML configuration file and exports membership data as specified. The configuration 
 * file is determined by looking up the media.properties key 'membership-export.config'.
 * The base Grouper UI distribution does not have a value set. If you want to allow 
 * membership export you must configure one or more formats appropriate to your site. 
 * <pre>&lt;membership-export&gt;
    &lt;format  name=&quot;CSV (Open with Excel)&quot; separator=&quot;,&quot; 
            quote=&quot;true&quot; 
        extension=&quot;.csv&quot; 
        content-type=&quot;application/ms-excel&quot;
    &gt;<br>		&lt;headers&gt;<br>			&lt;header name=&quot;Id&quot;/&gt;<br>			&lt;header name=&quot;Name&quot;/&gt;<br>			&lt;header name=&quot;Type&quot;/&gt;<br>		&lt;/headers&gt;<br>		&lt;source id=&quot;g:gsa&quot;&gt;<br>			&lt;field name=&quot;id&quot;/&gt;<br>			&lt;field name=&quot;displayName&quot;/&gt;<br>			&lt;field value=&quot;group&quot;/&gt;<br>		&lt;/source&gt;<br>		&lt;source id=&quot;qsuob&quot;&gt;<br>			&lt;field name=&quot;id&quot;/&gt;<br>			&lt;field name=&quot;name&quot;/&gt;<br>			&lt;field value=&quot;person&quot;/&gt;<br>		&lt;/source&gt;<br>    &lt;/format&gt;
&lt;/membership-export&gt;  </pre>
<p>Currently only simple delimited files are supported</p>
<p><strong>format tag</strong></p>
<table width="100%" border="1" cellspacing="5px">
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif"><strong>name</strong></font></td>
    <td><font face="Arial, Helvetica, sans-serif">is the text seen by the user 
      to identify this format - unless there is only one format, in which case 
      it is used as the default</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif"><strong>quote=true</strong></font></td>
    <td><font face="Arial, Helvetica, sans-serif">indicates that double-quotes 
      will surround each exported field. </font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif"><strong>extension</strong></font></td>
    <td><font face="Arial, Helvetica, sans-serif">determines the file extension 
      that will be presented to the web browser - which helps the browser choose 
      the correct application to open, and in the case where an application recognizes 
      different formats, lets the application know the format to expect</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif"><strong>content-type</strong></font></td>
    <td><font face="Arial, Helvetica, sans-serif">if configured, is sent as an 
      HTTP header. This will determine how a web browser tries to handle the data. 
      If no content-type is specified, it will be displayed in the UI as a normal 
      page.</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif"><strong>separator</strong></font></td>
    <td><font face="Arial, Helvetica, sans-serif">String used to separate fields 
      - typically a comma or tab (\t)</font></td>
  </tr>
</table>
<p><strong>headers tag</strong></p>
<p>Optionally specifies column headings</p>
<p><strong>source tag</strong></p>
<p>Specifies which fields should be exported for Subjects with the specified source 
  id. If a Subject to be exported has a source which has no configuration it is 
  ignored. </p>
<p>Each source should specify the same number of fields, which should, if they 
  are present, match the number of header fields specified.</p>
<p><strong>field tag</strong></p>
<table width="100%" border="1" cellspacing="5px">
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif"><strong>name</strong></font></td>
    <td><font face="Arial, Helvetica, sans-serif">the name of the subject attribute, 
      the value of which will be exported</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif"><strong>value</strong></font></td>
    <td><font face="Arial, Helvetica, sans-serif">if name is not specified then 
      the text in the value attribute will be used, as is. This allows 'padding' 
      where Subjects from different sources may nnot always have equivalent fields</font></td>
  </tr>
</table>
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: MembershipExporter.java,v 1.7 2009-10-16 10:30:08 isgwb Exp $
 */

public class MembershipExporter implements Serializable{
	private boolean active=false;
	//MCH 20090811 XXX THIS IS NOT SERIALIZABLE, SHOULDNT BE IN SESSION!!!!
	private transient Document configXml;
	private transient Map formatCache = new HashMap();
	private transient Map fieldsCache = new HashMap();
	private String separator=",";
	private String contextType=null;
	private boolean quote=false;
	
	public static void main(String[] args) throws Exception{
		Subject gs = SubjectFinder.findById("GrouperSystem", true);
		GrouperSession s = GrouperSession.start(gs);
		Group g = GroupFinder.findByName(s,"qsuob:all", true);
		Set members=g.getMembers();
		PrintWriter writer = new PrintWriter(System.out);
		ResourceBundle bundle = ResourceBundle.getBundle("resources/grouper/media");
		MembershipExporter export= new MembershipExporter();
		export.export("Minimal",members,writer);
		writer.flush();
		s.stop();
	}
	
	/**
	 * 
	 */
	public MembershipExporter() throws Exception{
		super();
		init();
		
	}
	
	private void init() throws Exception{
		if(configXml!=null) {
			return;
		}
		
		String configResource = null;
		try {
			configResource=GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("membership-export.config");
		}catch(MissingResourceException e){
			return;
		}
		configXml = DOMHelper.getDomFromResourceOnClassPath(configResource);
		List available = getAvailableFormats();
		if(!available.isEmpty()) active=true;
	}
	
	public boolean isActive() {
		return active;
	}
	
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
	
	public int getNumberOfAvailableFormats() throws Exception{
		return getAvailableFormats().size();
	}
	
	public void export(String name,Collection subjects,PrintWriter writer) throws Exception,IOException {
		init();
		if(getFormat(name)==null) throw new IllegalArgumentException(name  +" is not a valid format");
		String sep = getFormat(name).getAttribute("separator");
		quote=Boolean.parseBoolean(getFormat(name).getAttribute("quote"));
		if("".equals(sep) || sep==null) sep=separator;
		if("\\t".equals(sep)) sep="\t";
		printHeader(writer,name,sep);
		Iterator it= subjects.iterator();
		Subject subject = null;
		Object item;
		while(it.hasNext()) {
			item=it.next();
			try {
				if(item instanceof Subject) {
					subject = (Subject)item;
				}else if(item instanceof Membership) {
					
						subject = ((Membership)item).getMember().getSubject();
				}else if(item instanceof Member) {
					
						subject = ((Member)item).getSubject();
				}else{
					continue;
				}
			}catch(MemberNotFoundException mnfe) {
				continue;
			}catch(SubjectNotFoundException snfe) {
				continue;
			}
			printSubject(writer,name,subject,sep);
		}
		return;
	}
	
	private void printHeader(PrintWriter writer,String name,String sep) throws Exception,IOException {
		List headers = getHeaders(name);
		for(int i=0;i<headers.size();i++) {
			if(i>0) writer.print(sep);
			if(quote) writer.print('"');
			writer.print(headers.get(i));
			if(quote) writer.print('"');
		}
		if(!headers.isEmpty()) writer.print("\n");
	}
	
	private void printSubject(PrintWriter writer,String name,Subject subject,String sep) throws Exception,IOException {
		List fields = getFields(name,subject);
		if(fields==null) return;
		Element field;
		String val="???";
		String fieldName;
		for(int i=0;i<fields.size();i++) {
			field=(Element)fields.get(i);
			
			if(i>0) writer.print(sep);
			if(quote) writer.print('"');
			if(field.hasAttribute("value")) {
				writer.print(field.getAttribute("value"));
			}else{
				val="???";
				fieldName=field.getAttribute("name");
				if("name".equals(fieldName)) {
					val=subject.getName();
				}else if("description".equals(fieldName)) {
					val=subject.getDescription();
				}else if("id".equals(fieldName)) {
						val=subject.getId();
				}else {
					try {
						val=subject.getAttributeValue(fieldName);
					}catch(Exception e){}
				}
				if(val==null) val="???";
				if(quote && val.indexOf("\"") > -1) {
					writer.print(val.replaceAll("\"","\"\""));
				}else{
					writer.print(val);
				}
			}
			if(quote) writer.print('"');
		}
		writer.print("\n");
	}
	
	private Element getFormat(String name) throws Exception{
		init();
		return (Element)formatCache.get(name);
	}
	
	private List getFields(String format,Subject subject) throws Exception{
		String lookup = format + ":" + subject.getSource().getId();
		if("".equals(fieldsCache.get(lookup))) return null;
		List fields = (List)fieldsCache.get(lookup);
		if(fields==null) {
			boolean ok=false;
			Element formatElement = getFormat(format);
			NodeList nl = formatElement.getElementsByTagName("source");
			NodeList fieldList;
			Element fieldElement;
			Element sourceElement;
			
			for (int i=0;i<nl.getLength();i++) {
				sourceElement = (Element)nl.item(i);
				fieldList = sourceElement.getElementsByTagName("field");
				List sourceFields = new ArrayList();
				fieldsCache.put(format + ":" + sourceElement.getAttribute("id"),sourceFields);
				if(subject.getSource().getId().equals(sourceElement.getAttribute("id"))) {
					ok=true;
					fields = sourceFields;
				}
				for (int j=0;j<fieldList.getLength();j++) {
					sourceFields.add(fieldList.item(j));
				}
			}
			if(!ok) {
				fieldsCache.put(lookup,"");
				return null;
			}
		}
		return fields;
	}
	
	private List getHeaders(String format) throws Exception{
		List headers=new ArrayList();
		Element fe = getFormat(format);
		NodeList nl = fe.getElementsByTagName("header");
		Element he;
		for(int i=0;i<nl.getLength();i++) {
			he = (Element) nl.item(i);
			headers.add(he.getAttribute("name"));
		}
		return headers;
	}
	
	public String getContentType(String format) throws Exception{
		String ct = getFormat(format).getAttribute("content-type");
		if("".equals(ct)) ct=null;
		return ct;
	}
	
	public String getExtension(String format) throws Exception{
		String ext = getFormat(format).getAttribute("extension");
		if("".equals(ext)) ext=null;
		return ext;
	}
}
