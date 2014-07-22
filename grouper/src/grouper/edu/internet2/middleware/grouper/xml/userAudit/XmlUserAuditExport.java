/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author mchyzer
 * $Id: XmlUserAuditExport.java,v 1.2 2009-08-12 04:52:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml.userAudit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * export user audits
 */
public class XmlUserAuditExport {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(XmlUserAuditExport.class);

  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    writeUserAudits(new File("c:/temp/export.xml"));
    //readMembers();
  }
  
  /**
   * @param xmlExportFile the ending result file to put the results in
   */
  public static void writeUserAudits(File xmlExportFile)  {
    
    File xmlTempFileAudits = new File(GrouperUtil.fileCanonicalPath(xmlExportFile) + ".auditTemp.xml"); 
    
    if (xmlTempFileAudits.exists()) {
      xmlTempFileAudits.delete();
    }
    
    writeUserAudits(xmlExportFile, xmlTempFileAudits);
    
    
  }
  
  /**
   * @param xmlExportFile the ending result file to put the results in
   * @param xmlTempFileAudits temporary file for audit information
   */
  public static void writeUserAudits(final File xmlExportFile, final File xmlTempFileAudits) {
    try {
      GrouperUtil.deleteFile(xmlExportFile);
      GrouperUtil.deleteFile(xmlTempFileAudits);
      
      final XStream xStream = xstream();
      
      //this is the main file, just write the members, then when audit are done, append those.
      final FileWriter xmlExportFileWriter = new FileWriter(xmlExportFile);
      final CompactWriter xmlExportCompactWriter = new CompactWriter(xmlExportFileWriter);
  
      //this is a temp file for audits, write here, so we can get members in main file first, then append this and delete
      final FileWriter xmlAuditFileWriter = new FileWriter(xmlTempFileAudits);
      final CompactWriter xmlAuditCompactWriter = new CompactWriter(xmlAuditFileWriter);
  
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, 
          AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
              try {
                HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
                Session session = hibernateSession.getSession();
  
                xmlExportFileWriter.write("<userAudits version=\"" + GrouperVersion.GROUPER_VERSION + "\">\n");

                xmlExportFileWriter.write("<xmlMembers>\n");

                // map of member uuid to XmlMember
                Map<String, XmlMember> allMembersInRegistryNotExported = XmlMember.retrieveAllMembers(session);

                if (LOG.isInfoEnabled()) {
                  LOG.info("userAudit export: Read in " + allMembersInRegistryNotExported.size() + " members into cache");
                }

                //get the audit types (note, it is assumed there are no members here)
                {
                  xmlAuditFileWriter.write("<xmlAuditTypes>\n");
  
                  Query query = session.createQuery("from AuditType order by auditCategory, actionName");
                  
                  //this is an efficient low-memory way to iterate through a resultset
                  ScrollableResults results = query.scroll();
                  int typeCount = 0;
                  while(results.next()) {
                    Object object = results.get(0);
                    AuditType auditType = (AuditType)object;
                    XmlAuditType xmlAuditType = new XmlAuditType(auditType);
                    xStream.marshal(xmlAuditType, xmlAuditCompactWriter);
                    xmlAuditCompactWriter.flush();
                    xmlAuditFileWriter.write("\n");
                    
                    //check for members
                    XmlMember.exportMembers(allMembersInRegistryNotExported, auditType, 
                        xStream, xmlExportFileWriter, xmlExportCompactWriter);
                    typeCount++;
                  }
                  logInfoAndPrintToScreen("userAudit export: Exported " + typeCount + " auditTypes");
                  xmlAuditFileWriter.write("</xmlAuditTypes>\n\n");
                  
                }
  
                //get the audits
                {
                  xmlAuditFileWriter.write("<xmlAuditEntries>\n");
  
                  Query query = session.createQuery("from AuditEntry order by lastUpdatedDb desc");
                  
                  int size = hibernateSession.byHql().createQuery("select count(*) from AuditEntry")
                    .uniqueResult(Long.class).intValue();
  
                  int bucket = 1;
                  int currentRecordIndex = 0;
                  
                  //this is an efficient low-memory way to iterate through a resultset
                  ScrollableResults results = query.scroll();
                  while(results.next()) {
                    currentRecordIndex++;
                    Object object = results.get(0);
                    AuditEntry auditEntry = (AuditEntry)object;
                    XmlAuditEntry xmlAuditEntry = new XmlAuditEntry(auditEntry);
                    xStream.marshal(xmlAuditEntry, xmlAuditCompactWriter);
                    xmlAuditCompactWriter.flush();
                    xmlAuditFileWriter.write("\n");
                    
                    //check for members
                    XmlMember.exportMembers(allMembersInRegistryNotExported, auditEntry, 
                        xStream, xmlExportFileWriter, xmlExportCompactWriter);
                    
                    // see if we have done a certain percentage
                    if (shouldLog(size, bucket, currentRecordIndex)) {
                      logProgressMessage("userAudit export: Exported auditEntries", size, bucket, currentRecordIndex);
                      bucket++;
                    }
                    
                  }
                  logProgressMessage("userAudit export: Exported auditEntries", currentRecordIndex, 10, currentRecordIndex); 
                  
                  xmlAuditFileWriter.write("</xmlAuditEntries>\n\n");
                  xmlAuditFileWriter.flush();
                  
                }
                
                xmlAuditFileWriter.write("</userAudits>\n");
                xmlAuditFileWriter.flush();
                xmlAuditFileWriter.close();
  
                //end the members file
                xmlExportFileWriter.write("</xmlMembers>\n\n");
                xmlExportFileWriter.flush();
                xmlExportFileWriter.close();
  
                
                return null;
              } catch (Exception e) {
                throw new RuntimeException(e);
              } finally {
                GrouperUtil.closeQuietly(xmlAuditFileWriter);
                GrouperUtil.closeQuietly(xmlExportFileWriter);
              }
            }
  
      });
      //now open the audits and the main file, and append one to the other
      //xmlExportFile, 
      //xmlTempFileAudits
      OutputStream outputStream = null;
      InputStream inputStream = null;
      try {
        outputStream = new FileOutputStream(xmlExportFile, true);
        inputStream = new FileInputStream(xmlTempFileAudits);
        IOUtils.copy(inputStream, outputStream);
      } finally {
        GrouperUtil.closeQuietly(outputStream);
        GrouperUtil.closeQuietly(inputStream);
      }
      
      //ok, if we got this far with no errors, lets delete the temp file
      xmlTempFileAudits.delete();
    } catch (Exception e) {
      throw new RuntimeException("Problems exporting: ", e);
    }
  }

  /**
   * @return xstream
   */
  public static XStream xstream() {
    final XStream xStream = new XStream(new XppDriver());
    
    //do javabean properties, not fields
    xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()) {

      /**
       * @see com.thoughtworks.xstream.converters.javabean.JavaBeanConverter#canConvert(java.lang.Class)
       */
      @SuppressWarnings("unchecked")
      @Override
      public boolean canConvert(Class type) {
        //see if one of our beans
        return type.getName().startsWith("edu.internet2");
      }
      
    }); 
    
    xStream.alias("XmlMember", XmlMember.class);
    xStream.alias("XmlAuditType", XmlAuditType.class);
    xStream.alias("XmlAuditEntry", XmlAuditEntry.class);
    return xStream;
  }

  /**
   * log and print this progress message to screen
   * @param label to prefix
   * @param size total count
   * @param bucket e.g. 1 is the 10th percentile
   * @param currentRecordIndex current record we are on
   */
  private static void logProgressMessage(String label, int size, int bucket, int currentRecordIndex) {
    int padSize = Integer.toString(size).length();
    
    String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    
    String auditEntryProgress = label + ": " + StringUtils.leftPad(Integer.toString(currentRecordIndex), padSize) 
      + " / " + size + " (" + StringUtils.leftPad(Integer.toString(bucket*10), 3) + "%) " + timestamp;
    logInfoAndPrintToScreen(auditEntryProgress);
  }
  
  
  /**
   * @param size
   * @param bucket
   * @param recordCount
   * @return true if should log (10% more done)
   */
  private static boolean shouldLog(int size, int bucket, int recordCount) {
    return size * (bucket/10.0) < recordCount;
  }

  /**
   * @param auditEntryProgress
   */
  public static void logInfoAndPrintToScreen(String auditEntryProgress) {
    LOG.info(auditEntryProgress);
    if (!GrouperUtil.isPrintGrouperLogsToConsole() || !LOG.isInfoEnabled()) {
      System.out.println(auditEntryProgress);
    }
  }


}
