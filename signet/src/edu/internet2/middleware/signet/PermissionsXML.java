/*--
$Id: PermissionsXML.java,v 1.6 2006-04-10 06:28:11 ddonn Exp $
$Date: 2006-04-10 06:28:11 $

Copyright 2006 Internet2, Stanford University

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

package edu.internet2.middleware.signet;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import edu.internet2.middleware.signet.tree.TreeNode;

public class PermissionsXML
{
   /**
   * Default constructor.
   *
   */
   public PermissionsXML() throws Exception {
      // this.logger = Logger.getLogger(this.toString());
   }

   public void generateXML(PrivilegedSubject privSubject, OutputStream outStream)
      throws Exception {
   
      // ================== Produce document =================
      //
      // Get an output factory
      //
      XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
   
      //
      // Instantiate a writer
      //
      XMLStreamWriter xmlw = xmlof.createXMLStreamWriter(outStream);
   
      //
      // Generate the XML
      //
      // Write the default XML declaration
      xmlw.writeStartDocument("UTF-8", "1.0");
      xmlw.writeCharacters("\n");
   
      // Write a comment
      xmlw.writeComment("DOCTYPE Permissions SYSTEM \"http://middleware.internet2.edu/xml/Permissions.dtd\"");
      xmlw.writeCharacters("\n");
   
      // Write the root element "Permissions"
      xmlw.writeStartElement("Permissions");
      xmlw.writeCharacters("\n");
   
      // -------- Subject --------
      String privSubjectID = privSubject.getSubjectId();

      xmlw.writeCharacters("   ");
      xmlw.writeStartElement("Subject");
      xmlw.writeAttribute("id",privSubjectID);
      xmlw.writeCharacters("\n");
   
      // -------- SubjectType --------
      String privSubjectTypeID = privSubject.getSubjectTypeId();
   
      xmlw.writeCharacters("      ");
      xmlw.writeStartElement("SubjectType");
      xmlw.writeCharacters(privSubjectTypeID);
      xmlw.writeEndElement();
      xmlw.writeCharacters("\n");
   
      // -------- SubjectName --------
      String privSubjectName = privSubject.getName();
   
      xmlw.writeCharacters("      ");
      xmlw.writeStartElement("SubjectName");
      xmlw.writeCharacters(privSubjectName);
      xmlw.writeEndElement();
      xmlw.writeCharacters("\n");
   
      // -------- End Subject --------
      xmlw.writeCharacters("   ");
      xmlw.writeEndElement();
      xmlw.writeCharacters("\n");
   
      Set privileges = privSubject.getPrivileges();
      Iterator privilegesIter = privileges.iterator();
   
      while (privilegesIter.hasNext()) {
         Privilege privilege = (Privilege) privilegesIter.next();
         Permission permission = privilege.getPermission();
         String permissionID = permission.getId();

         Subsystem permissionSubsystem = permission.getSubsystem();
         String permissionSubsystemID = permissionSubsystem.getId();
   
         // -------- Permission --------
         xmlw.writeCharacters("   ");
         xmlw.writeStartElement("Permission");
         xmlw.writeAttribute("subsystemId",permissionSubsystemID);
         xmlw.writeAttribute("id",permissionID);
         xmlw.writeCharacters("\n");
    
         // -------- Scope --------
         TreeNode scope = privilege.getScope();
         String scopeId = scope.getId();
         String scopeName = scope.getName();
    
         xmlw.writeCharacters("      ");
         xmlw.writeStartElement("Scope");
         xmlw.writeAttribute("id",scopeId);
         xmlw.writeCharacters("\n");
      
         xmlw.writeCharacters("         ");
         xmlw.writeStartElement("ScopeName");
         xmlw.writeCharacters(scopeName);
         xmlw.writeEndElement();
         xmlw.writeCharacters("\n");
    
         // -------- End Scope --------
         xmlw.writeCharacters("      ");
         xmlw.writeEndElement();
         xmlw.writeCharacters("\n");
      
         // -------- Limits --------
         Set limitValuesSet = privilege.getLimitValues();
         Iterator limitValuesSetIter = limitValuesSet.iterator();

         TreeSet limitValues = new TreeSet();
         while (limitValuesSetIter.hasNext()) {
            LimitValue limitValue = (LimitValue) limitValuesSetIter.next();
            limitValues.add(limitValue);
         }
    
         Iterator limitValuesIter = limitValues.iterator();
         String currLimitId = ""; 
         while (limitValuesIter.hasNext()) {
            LimitValue limitValue = (LimitValue) limitValuesIter.next();
            Limit limit = limitValue.getLimit();
            String limitId = limit.getId();
            String limitChoice = limitValue.getValue();
    
            if (limitId != currLimitId) {
      
                if (currLimitId != "") {
                    // -------- End Limit --------
                    xmlw.writeCharacters("      ");
                    xmlw.writeEndElement();
                    xmlw.writeCharacters("\n");
                }

                // -------- Limit --------
                xmlw.writeCharacters("      ");
                xmlw.writeStartElement("Limit");
                xmlw.writeAttribute("id",limitId);
                xmlw.writeCharacters("\n");
                
                currLimitId = limitId;
            }
      
            // -------- LimitValue --------
            xmlw.writeCharacters("         ");
            xmlw.writeStartElement("LimitValue");
            xmlw.writeCharacters(limitChoice);
            xmlw.writeEndElement();
            xmlw.writeCharacters("\n");
      
         }
 
         // -------- End Last limit -------- 
         if (currLimitId != "") {
             // -------- End Limit --------
             xmlw.writeCharacters("      ");
             xmlw.writeEndElement();
             xmlw.writeCharacters("\n");
         }
      
         // -------- End Permission -------- 
         xmlw.writeCharacters("   ");
         xmlw.writeEndElement();
         xmlw.writeCharacters("\n");
      
      }
      
      // -------- End Permissions --------
      xmlw.writeEndElement();
      xmlw.writeCharacters("\n");
      
      // End the XML document with a comment
      xmlw.writeEndDocument();

//      Date date = new Date();
      DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");

      StringBuffer buf = new StringBuffer();
      buf.append("datastore='" + Signet.getConfiguration().getProperty("hibernate.connection.url") + "' ");
      buf.append("version='" + Signet.getVersion() + "' ");
      buf.append("timestamp='" + df.format(new Date()) + "'");
      buf.append("\n");
      xmlw.writeComment(buf.toString());
//      xmlw.writeComment("datastore='jdbc:sybase:Tds:renoir.stanford.edu:1025/dsignet' version='1.0' timestamp='" + df.format(date) + "'\n");
      
      // Close the XMLStreamWriter to free up resources
      xmlw.close();
  
   }
   
   public static void main(String args[]) throws Exception {
   
      try {
         if (args.length < 2) {
            System.err.println("Usage: PermissionsXML <subjectType> <subjectID>");
            return;
         }
      
      String subjectType = args[0];
      String subjectID = args[1];
      
      Signet signet = new Signet();
      
      PrivilegedSubject privSubject = signet.getPrivilegedSubject(subjectType, subjectID);
   
      // Create the XML file
      PermissionsXML processor = new PermissionsXML();
      processor.generateXML(privSubject, System.out);

      System.out.println("\n");

      } catch (Exception e) {
         e.printStackTrace();
      }
   }  
}
