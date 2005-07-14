/*--
$Id: PrivilegesXML.java,v 1.3 2005-07-14 20:24:02 lmcrae Exp $
$Date: 2005-07-14 20:24:02 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.stream.*;

public class PrivilegesXML
{
   /**
   * Default constructor.
   *
   */
   public PrivilegesXML() throws Exception {
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
      xmlw.writeComment("DOCTYPE Privileges SYSTEM \"http://middleware.internet2.edu/xml/Privileges.dtd\"");
      xmlw.writeCharacters("\n");
   
      // Write the root element "Privileges"
      xmlw.writeStartElement("Privileges");
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
    
         // -------- Permission --------
         xmlw.writeCharacters("   ");
         xmlw.writeStartElement("Permission");
         xmlw.writeAttribute("id",permissionID);
         xmlw.writeCharacters("\n");
    
         Set limitValues = privilege.getLimitValues();
         Iterator limitValuesIter = limitValues.iterator();
    
         if (limitValuesIter.hasNext()) {
            LimitValue limitValue = (LimitValue) limitValuesIter.next();
            Limit limit = limitValue.getLimit();
            String limitId = limit.getId();
            String limitChoice = limitValue.getValue();
    
            // -------- Limit --------
            xmlw.writeCharacters("      ");
            xmlw.writeStartElement("Limit");
            xmlw.writeAttribute("id",limitId);
            xmlw.writeCharacters("\n");
      
            // -------- LimitValue --------
            xmlw.writeCharacters("         ");
            xmlw.writeStartElement("LimitValue");
            xmlw.writeCharacters(limitChoice);
            xmlw.writeEndElement();
            xmlw.writeCharacters("\n");
  
            while (limitValuesIter.hasNext()) {
               LimitValue limitValue2 = (LimitValue) limitValuesIter.next();
               String limitChoice2 = limitValue2.getValue();
      
               // -------- LimitValue --------
               xmlw.writeCharacters("         ");
               xmlw.writeStartElement("LimitValue");
               xmlw.writeCharacters(limitChoice2);
               xmlw.writeEndElement();
               xmlw.writeCharacters("\n");
      
            }
      
            // -------- End Limit --------
            xmlw.writeCharacters("      ");
            xmlw.writeEndElement();
            xmlw.writeCharacters("\n");
      
            // -------- End Permission -------- 
            xmlw.writeCharacters("   ");
            xmlw.writeEndElement();
            xmlw.writeCharacters("\n");
      
         }
      
         // -------- End Privileges --------
         xmlw.writeEndElement();
         xmlw.writeCharacters("\n");
      
         // End the XML document
         xmlw.writeEndDocument();

         Date date = new Date();
         DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");

         xmlw.writeComment("datastore='jdbc:sybase:Tds:renoir.stanford.edu:1025/dsignet' version='1.0' timestamp='" + df.format(date) + "'");
      
         // Close the XMLStreamWriter to free up resources
         xmlw.close();
      }
   }
   
   public static void main(String args[]) throws Exception {
   
      try {
         if (args.length < 2) {
            System.err.println("Usage: PrivilegesXML <subjectType> <subjectID>");
            return;
         }
      
      String subjectType = args[0];
      String subjectID = args[1];
      
      Signet signet = new Signet();
      
      PrivilegedSubject privSubject = signet.getPrivilegedSubject(subjectType, subjectID);
   
      // Create the XML file
      PrivilegesXML processor = new PrivilegesXML();
      processor.generateXML(privSubject, System.out);

      System.out.println("\n");

      } catch (Exception e) {
         e.printStackTrace();
      }
   }  
}
