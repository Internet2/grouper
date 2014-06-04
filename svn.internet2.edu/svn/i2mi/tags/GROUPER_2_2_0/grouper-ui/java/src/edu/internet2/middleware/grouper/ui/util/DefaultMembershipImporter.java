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
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import org.w3c.dom.Element;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Default implementation of the MembershipImporter interface. This class
 * expects single line records e.g. tab/comma separated fields where one
 * of the fields can be used to lookup a Subject by id or identifier.
 * <p>This class expects to find the following attributes in the config Element:<br/>
 * separator e.g. ',' or '\t', used to determine fields<br/>
 * id-field - an integer used to determine which field position to use to lookup a Subject>br/>
 * field-type - 'id' or 'identifier' specifies the type of lookup to do<br/>
 * ignore-existing - 'false' or 'true' determies whether trying to add an existing member is considered an error</p>
 * <p>Other implementations
 * can be as complex as necessary  
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: DefaultMembershipImporter.java,v 1.6 2009-08-12 04:52:14 mchyzer Exp $
 */

public class DefaultMembershipImporter implements MembershipImporter{

	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.util.MembershipImporter#load(edu.internet2.middleware.grouper.Group, java.io.Reader, java.io.PrintWriter, org.w3c.dom.Element, edu.internet2.middleware.grouper.Field, java.util.ResourceBundle)
	<p>Recognises the following config Element attributes:</p>
	  <table width="100%" border="1" cellspacing="5px">
  <tr> 
    <td><strong><font face="Arial, Helvetica, sans-serif">separator</font></strong></td>
    <td><font face="Arial, Helvetica, sans-serif">used to split input lines into 
      fields </font></td>
  </tr>
  <tr> 
    <td><strong><font face="Arial, Helvetica, sans-serif">id-field</font></strong></td>
    <td><font face="Arial, Helvetica, sans-serif">integer, starting at 1, which 
      indicates which field should be used to 'lookup' a subject</font></td>
  </tr>
  <tr> 
    <td><strong><font face="Arial, Helvetica, sans-serif">field-type</font></strong></td>
    <td><font face="Arial, Helvetica, sans-serif">=id or identifier. Specifies 
      how to lookup a subject</font></td>
  </tr>
</table>
	 *
	 **/
	public int load(Group group, Reader input, PrintWriter output,Element config,Field field)
			throws IOException,SchemaException {
	  ResourceBundle nav = GrouperUiFilter.retrieveSessionNavResourceBundle();
		int errorCount=0;
		String separator=config.getAttribute("separator");
		if(separator==null || separator.equals("")) throw new IllegalArgumentException(nav.getString("groups.import.message.no-separator"));
		if(separator.equals("\\t")) separator="\t";
		int idField = Integer.parseInt(config.getAttribute("id-field"))-1;
		String fieldType=config.getAttribute("field-type");
		boolean useId=false;
		if("id".equals(fieldType)) {
			useId=true;
		}else if(!"identifier".equals(fieldType)) {
			throw new IllegalArgumentException(nav.getString("groups.import.message.bad-field-type"));
		}
		boolean ignoreExisting = Boolean.parseBoolean(config.getAttribute("ignore-existing"));
		String line;
		String[] parts;
		String id;
		Subject subject;
		LineNumberReader lineInput = new LineNumberReader(input);
		int lineCount=0;
		while ((line=lineInput.readLine())!=null) {
			lineCount++;
			parts=line.split(separator);
			if(parts.length<idField) {
				output.println(nav.getString("groups.import.message.insufficient-fields") + separator + line);
				errorCount++;
			}else{
				id=parts[idField].trim();
				if(id.indexOf("\"")==0) {
					id = id.replaceAll("^\"(.*?)\"$","$1");
				}
				try {
					if(useId) {
						subject=SubjectFinder.findById(id, true);
					}else{
						subject=SubjectFinder.findByIdentifier(id, true);
					}
					if(group.hasImmediateMember(subject,field)) {
						if(!ignoreExisting) {
							printHtmlError(nav.getString("groups.import.message.existing-member") + separator + line,output);
							errorCount++;
						}
					}else{
						try {
							group.addMember(subject,field);
							output.println(nav.getString("groups.import.message.successful") + separator+ line);
						}catch(Exception ex) {
							printHtmlError(nav.getString("groups.import.message.error")+" " + ex.getMessage() + separator + line,output);
							errorCount++;
						}
					}
				}catch(SubjectNotFoundException e){
					printHtmlError(nav.getString("groups.import.message.no-subject") + separator + line,output);
					errorCount++;
				}catch(SubjectNotUniqueException e) {
					printHtmlError(nav.getString("groups.import.message.subject-not-unique") + separator + line,output);
					errorCount++;
				}
				output.flush();
			}
			
		}
		if(lineCount==0) {
			printHtmlError(nav.getString("groups.import.message.no-data"),output);
		}
		
		return errorCount;
	}
	
	private void printHtmlError(String error,PrintWriter writer) {
		writer.print("<span style='color:#ff0000'>" + error + "</span>\n");
	}
}
