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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.importXml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;

import com.thoughtworks.xstream.XStream;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttribute;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeAssign;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeAssignAction;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeAssignActionSet;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeAssignValue;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeDef;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeDefName;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeDefNameSet;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeDefScope;
import edu.internet2.middleware.grouper.xml.export.XmlExportAuditEntry;
import edu.internet2.middleware.grouper.xml.export.XmlExportAuditType;
import edu.internet2.middleware.grouper.xml.export.XmlExportComposite;
import edu.internet2.middleware.grouper.xml.export.XmlExportField;
import edu.internet2.middleware.grouper.xml.export.XmlExportGroup;
import edu.internet2.middleware.grouper.xml.export.XmlExportGroupType;
import edu.internet2.middleware.grouper.xml.export.XmlExportGroupTypeTuple;
import edu.internet2.middleware.grouper.xml.export.XmlExportMain;
import edu.internet2.middleware.grouper.xml.export.XmlExportMember;
import edu.internet2.middleware.grouper.xml.export.XmlExportMembership;
import edu.internet2.middleware.grouper.xml.export.XmlExportRoleSet;
import edu.internet2.middleware.grouper.xml.export.XmlExportStem;
import edu.internet2.middleware.grouper.xml.export.XmlExportUtils;


/**
 *
 */
public class XmlImportMain {

  /**
   * if readonly put a report here
   */
  private Writer recordReportWriter = null;
  
  /**
   * if readonly put a report here
   */
  private String recordReportFileCanonicalPath = null;
  
  /** if doing a readonly report of registry */
  private boolean recordReport;

  /**
   * @param args
   */
  public static void main(String[] args) {
    
  }

  /** record number we are on */
  private long currentRecordIndex = 0;
  
  /**
   * version of grouper which exported the export file
   */
  private GrouperVersion importFileVersion = null;

  /** map from file uuid to registry uuid if there was a change for any and all uuids */
  private Map<String, String> uuidTranslation = new HashMap<String, String>();
  
  /**
   * uuid translation when uuids are changed
   * @return the uuidTranslation
   */
  public Map<String, String> getUuidTranslation() {
    return this.uuidTranslation;
  }

  /** count of objects in the db */
  private long originalDbCount = 0;
  
  /** insert count */
  private long insertCount = 0;
  
  /** update count */
  private long updateCount = 0;
  
  /** skip count */
  private long skipCount = 0;
  
  
  /**
   * @return the currentCount
   */
  public long getCurrentRecordIndex() {
    return this.currentRecordIndex;
  }

  
  /**
   * @return the insertCount
   */
  public long getInsertCount() {
    return this.insertCount;
  }

  
  /**
   * @return the updateCount
   */
  public long getUpdateCount() {
    return this.updateCount;
  }

  
  /**
   * @return the skipCount
   */
  public long getSkipCount() {
    return this.skipCount;
  }

  /** count of members/audits/types in import file */
  private long totalImportFileCount = 0;
  
  /** xstream object which does the object conversion */
  private XStream xstream = null;

  /** for multiple assigned, ignore these ides which are further in the file */
  private Set<String> idsToIgnore = new HashSet<String>();

  /**
   * ids to ignore  
   * @return ids to ignore
   */
  public Set<String> getIdsToIgnore() {
    return this.idsToIgnore;
  }

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(XmlImportMain.class);

  /**
   * 
   * @return total count
   */
  public long getTotalImportFileCount() {
    return this.totalImportFileCount;
  }

  /**
   * 
   * @param importFile 
   */
  public void processXml(final File importFile) {
    String filePath = GrouperUtil.fileCanonicalPath(importFile);
    processXml(importFile, null, filePath);
  }
  
  /**
   * @param importString 
   * 
   */
  public void processXml(String importString) {
    processXml(null, importString, "string");
  }
  

  
  /**
   * @param string
   * @param file 
   * @param filePath 
   */
  private void processXml(final File file, final String string, final String filePath) {
    File recordReportFile = null;
    if (this.recordReport) {
      String recordReportFileName = "grouperImportRecordReport_" + GrouperUtil.timestampToFileString(new Date()) + ".txt";
      recordReportFile = new File(recordReportFileName);
      this.recordReportFileCanonicalPath = GrouperUtil.fileCanonicalPath(recordReportFile);
      try {
        this.recordReportWriter = new FileWriter(recordReportFile);
      } catch (IOException ioe) {
        throw new RuntimeException("Problem opening file: " 
            + this.recordReportFileCanonicalPath , ioe);
      }
    }
    Reader reader = null;
    try {
      
      if (file != null && string != null) {
        throw new RuntimeException("Cant have both not null");
      }
      if (file == null && string == null) {
        throw new RuntimeException("Cant have both null");
      }
      reader = file != null ? new FileReader(file) : new StringReader(string);
      
      processXmlFirstPass(reader, filePath);

      reader = file != null ? new FileReader(file) : new StringReader(string);
      
      processXmlSecondPass(reader, filePath);
    } catch (FileNotFoundException fnfe) {
      throw new RuntimeException("Cant find file: " + filePath);
    } finally {
      if (this.recordReport) {
        GrouperUtil.closeQuietly(this.recordReportWriter);
        if (!StringUtils.isBlank(this.recordReportFileCanonicalPath)) {
          if (recordReportFile.exists() && recordReportFile.length() > 0) {
            XmlImportMain.logInfoAndPrintToScreen("Wrote record report log to: " 
                + this.recordReportFileCanonicalPath);
          } else {
            recordReportFile.delete();
            XmlImportMain.logInfoAndPrintToScreen("There are no inserts or updates " +
            		"from XML file, registry is in sync");
          }
        }
      }
    }
  }
  
  
  /**
   * @return the readonlyFileCanonicalPath
   */
  public String getRecordReportFileCanonicalPath() {
    return this.recordReportFileCanonicalPath;
  }


  /**
   * @param entry
   */
  public void readonlyWriteLogEntry(String entry) {
    try {
      this.recordReportWriter.write(entry);
      if (entry == null || !entry.endsWith("\n")) {
        this.recordReportWriter.write("\n");
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Prblem writing entry to file: " + this.recordReportFileCanonicalPath);
    }
  }
  
  /** parser for file */
  private SAXReader reader;

  /** if we are done writing */
  private boolean done = false;
  
  /**
   * 
   * @return the reader
   */
  public SAXReader getReader() {
    return this.reader;
  }

  /**
   * increment the file count
   */
  public void incrementTotalImportFileCount() {
    this.totalImportFileCount++;
  }
  
  /**
   * 
   * @param reader 
   * @param filePath 
   */
  private void processXmlFirstPass(final Reader reader, final String filePath) {
  
    GrouperTransactionType grouperTransactionType = GrouperTransactionType.NONE;
    
    HibernateSession.callbackHibernateSession(grouperTransactionType, 
        AuditControl.WILL_AUDIT, new HibernateHandler() {
  
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
  
            SAXReader theReader = null;
            try {
              
              xstream = XmlExportUtils.xstream();
              
              theReader = new SAXReader();
              
              XmlImportMain.this.reader = theReader;
              
              theReader.addHandler( "/grouperExport", 
                  new ElementHandler() {
                      public void onStart(ElementPath path) {
                        Element grouperExport = path.getCurrent();
                        XmlImportMain.this.importFileVersion = new GrouperVersion(grouperExport.attributeValue("version"));
                        XmlImportMain.logInfoAndPrintToScreen(
                            "grouper import: reading document: " + filePath
                             + ", version: " + XmlImportMain.this.importFileVersion);
                      }
                      public void onEnd(ElementPath path) {
                        //we done!
                      }
                  }
              );
              
              XmlExportMember.processXmlFirstPass(XmlImportMain.this);
              XmlExportStem.processXmlFirstPass(XmlImportMain.this);
              XmlExportGroup.processXmlFirstPass(XmlImportMain.this);
              XmlExportGroupType.processXmlFirstPass(XmlImportMain.this);
              XmlExportField.processXmlFirstPass(XmlImportMain.this);
              XmlExportGroupTypeTuple.processXmlFirstPass(XmlImportMain.this);
              XmlExportComposite.processXmlFirstPass(XmlImportMain.this);
              XmlExportAttribute.processXmlFirstPass(XmlImportMain.this);
              XmlExportAttributeDef.processXmlFirstPass(XmlImportMain.this);
              XmlExportMembership.processXmlFirstPass(XmlImportMain.this);
              XmlExportAttributeDefName.processXmlFirstPass(XmlImportMain.this);
              XmlExportRoleSet.processXmlFirstPass(XmlImportMain.this);
              XmlExportAttributeAssignAction.processXmlFirstPass(XmlImportMain.this);
              XmlExportAttributeAssignActionSet.processXmlFirstPass(XmlImportMain.this);
              XmlExportAttributeAssign.processXmlFirstPass(XmlImportMain.this);
              XmlExportAttributeAssignValue.processXmlFirstPass(XmlImportMain.this);
              XmlExportAttributeDefNameSet.processXmlFirstPass(XmlImportMain.this);
              XmlExportAttributeDefScope.processXmlFirstPass(XmlImportMain.this);
              XmlExportAuditType.processXmlFirstPass(XmlImportMain.this);
              XmlExportAuditEntry.processXmlFirstPass(XmlImportMain.this);

              theReader.read(reader);
              
              logInfoAndPrintToScreen("XML file contains " + GrouperUtil.formatNumberWithCommas(XmlImportMain.this.totalImportFileCount) + " records");
              
              XmlImportMain.this.originalDbCount = XmlImportMain.dbCount(new XmlExportMain());
              
            } catch (DocumentException de) {
              throw new RuntimeException("Problem reading file: " + filePath, de);
            }
            return null;
          }
    });
  }
  
  
  /**
   * @return the record report
   */
  public boolean isRecordReport() {
    return this.recordReport;
  }


  
  /**
   * @param isRecordReport if record report to set
   */
  public void setRecordReport(boolean isRecordReport) {
    this.recordReport = isRecordReport;
  }


  /**
   * get a db count of exportable rows
   * @param xmlExportMain 
   * @return db count
   */
  public static int dbCount(XmlExportMain xmlExportMain) {
    int total = 0;
    total += XmlExportMember.dbCount(xmlExportMain);
    total += XmlExportStem.dbCount(xmlExportMain);
    total += XmlExportGroup.dbCount(xmlExportMain);
    total += XmlExportGroupType.dbCount(xmlExportMain);
    total += XmlExportField.dbCount();
    total += XmlExportGroupTypeTuple.dbCount(xmlExportMain);
    total += XmlExportComposite.dbCount(xmlExportMain);
    total += XmlExportAttribute.dbCount(xmlExportMain);
    total += XmlExportAttributeDef.dbCount(xmlExportMain);
    total += XmlExportMembership.dbCount(xmlExportMain);
    total += XmlExportAttributeDefName.dbCount(xmlExportMain);
    total += XmlExportRoleSet.dbCount(xmlExportMain);
    total += XmlExportAttributeAssignAction.dbCount(xmlExportMain);
    total += XmlExportAttributeAssignActionSet.dbCount(xmlExportMain);
    total += XmlExportAttributeAssign.dbCount(xmlExportMain);
    total += XmlExportAttributeAssignValue.dbCount();
    total += XmlExportAttributeDefNameSet.dbCount(xmlExportMain);
    total += XmlExportAttributeDefScope.dbCount(xmlExportMain);
    total += XmlExportAuditType.dbCount();
    total += XmlExportAuditEntry.dbCount();
    return total;
  }
  
  /**
   * 
   * @return original db count
   */
  public long getOriginalDbCount() {
    return this.originalDbCount;
  }

  /**
   * 
   * @param reader 
   * @param filePath 
   */
  private void processXmlSecondPass(final Reader reader, final String filePath) {
  
    this.done = false;
    this.currentRecordIndex = 0;
    Thread thread = null;
    final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    final SimpleDateFormat estFormat = new SimpleDateFormat("HH:mm");

    final long startTime = System.currentTimeMillis();
    
    thread = new Thread(new Runnable() {
      
      public void run() {
        while (true) {
          //sleep for thirty seconds
          for (int i=0;i<30;i++) {
            if (XmlImportMain.this.done) {
              return;
            }
            try {
              Thread.sleep(1000);
            } catch (InterruptedException ie) {
              //nothing
            }
          }
          if (XmlImportMain.this.done) {
            return;
          }
          
          //give a status
          long now = System.currentTimeMillis();
          double percent = ((double)XmlImportMain.this.currentRecordIndex*100D)/XmlImportMain.this.totalImportFileCount;
          if (percent == 0.0) {
            percent = 0.001;
          }
          long endTime = startTime + (long)((now-startTime) * (100D / percent));
          
          XmlImportMain.logInfoAndPrintToScreen(format.format(new Date(now)) + ": completed "
              + GrouperUtil.formatNumberWithCommas(XmlImportMain.this.currentRecordIndex) + " of " 
              + GrouperUtil.formatNumberWithCommas(XmlImportMain.this.totalImportFileCount) + " ("
              + Math.round(percent) + "%) estimated time done: " + estFormat.format(new Date(endTime)));
        }          
      }
    });
    
    thread.start();

    logInfoAndPrintToScreen(format.format(new Date()) + ": Beginning import: database contains " 
        + GrouperUtil.formatNumberWithCommas(XmlImportMain.this.originalDbCount) + " records");
    
    try {
      GrouperTransactionType grouperTransactionType = GrouperTransactionType.NONE;
      
      HibernateSession.callbackHibernateSession(grouperTransactionType, 
          AuditControl.WILL_AUDIT, new HibernateHandler() {
    
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws GrouperDAOException {
    
              SAXReader theReader = null;
              try {
                
                theReader = new SAXReader();
                
                XmlImportMain.this.reader = theReader;
                
                theReader.addHandler( "/grouperExport", 
                    new ElementHandler() {
                        public void onStart(ElementPath path) {
                          //not much to do
                        }
                        public void onEnd(ElementPath path) {
                          //we done!
                        }
                    }
                );
                
                XmlExportMember.processXmlSecondPass(XmlImportMain.this);
                XmlExportStem.processXmlSecondPass(XmlImportMain.this);
                XmlExportGroup.processXmlSecondPass(XmlImportMain.this);
                XmlExportGroupType.processXmlSecondPass(XmlImportMain.this);
                XmlExportField.processXmlSecondPass(XmlImportMain.this);
                XmlExportGroupTypeTuple.processXmlSecondPass(XmlImportMain.this);
                XmlExportComposite.processXmlSecondPass(XmlImportMain.this);
                XmlExportAttribute.processXmlSecondPass(XmlImportMain.this);
                XmlExportAttributeDef.processXmlSecondPass(XmlImportMain.this);
                XmlExportMembership.processXmlSecondPass(XmlImportMain.this);
                XmlExportAttributeDefName.processXmlSecondPass(XmlImportMain.this);
                XmlExportRoleSet.processXmlSecondPass(XmlImportMain.this);
                XmlExportAttributeAssignAction.processXmlSecondPass(XmlImportMain.this);
                XmlExportAttributeAssignActionSet.processXmlSecondPass(XmlImportMain.this);
                XmlExportAttributeAssign.processXmlSecondPass(XmlImportMain.this);
                XmlExportAttributeAssignValue.processXmlSecondPass(XmlImportMain.this);
                XmlExportAttributeDefNameSet.processXmlSecondPass(XmlImportMain.this);
                XmlExportAttributeDefScope.processXmlSecondPass(XmlImportMain.this);
                XmlExportAuditType.processXmlSecondPass(XmlImportMain.this);
                XmlExportAuditEntry.processXmlSecondPass(XmlImportMain.this);
    
                theReader.read(reader);
                
                logInfoAndPrintToScreen("Ending import: processed " + XmlImportMain.this.currentRecordIndex + " records");
  
                long finalDbCount = XmlImportMain.dbCount(new XmlExportMain());
                
                logInfoAndPrintToScreen("Ending import: database contains " + finalDbCount + " records");
                logInfoAndPrintToScreen("Ending import: " + XmlImportMain.this.insertCount + " inserts, " 
                    + XmlImportMain.this.updateCount + " updates, and " + XmlImportMain.this.skipCount + " skipped records");
                
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.XML_IMPORT, "fileName", 
                    filePath);
                auditEntry.setDescription("Imported xml: " + XmlImportMain.this.insertCount + " inserts, " 
                    + XmlImportMain.this.updateCount + " updates, and " + XmlImportMain.this.skipCount + " skipped records (" 
                    + GrouperUtil.formatNumberWithCommas(XmlImportMain.this.currentRecordIndex) + " total records), dbRecords: "  + finalDbCount);
                auditEntry.saveOrUpdate(true);

              } catch (DocumentException de) {
                throw new RuntimeException("Problem reading file: " + filePath, de);
              }
              
              
              return null;
            }
      });
    } finally {
      this.done = true;
      if (thread != null) {
        try {
          thread.join(2000);
        } catch (InterruptedException ie) {}
      }

    }
    XmlImportMain.logInfoAndPrintToScreen("DONE: " + format.format(new Date()) + ": imported "
        + GrouperUtil.formatNumberWithCommas(XmlImportMain.this.currentRecordIndex) + " records from: " 
        + filePath);
  }

  /**
   * increment the file count
   */
  public void incrementCurrentCount() {
    this.currentRecordIndex++;
    //uncomment this to see the status working
    //GrouperUtil.sleep(2000);
  }

  /**
   * increment the insert count
   */
  public void incrementInsertCount() {
    this.insertCount++;
  }

  /**
   * increment the file count
   */
  public void incrementSkipCount() {
    this.skipCount++;
  }

  /**
   * increment the update count
   */
  public void incrementUpdateCount() {
    this.updateCount++;
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

  /**
   * 
   * @return xstream
   */
  public XStream getXstream() {
    return this.xstream;
  }

}
