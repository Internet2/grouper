/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.importXml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;

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
import edu.internet2.middleware.grouper.xml.export.XmlExportComposite;
import edu.internet2.middleware.grouper.xml.export.XmlExportField;
import edu.internet2.middleware.grouper.xml.export.XmlExportGroup;
import edu.internet2.middleware.grouper.xml.export.XmlExportGroupType;
import edu.internet2.middleware.grouper.xml.export.XmlExportGroupTypeTuple;
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
   * @param args
   */
  public static void main(String[] args) {
    
  }

  /** record number we are on */
  private long currentRecordIndex = 0;
  
  /** grouper's hibernate session */
  private HibernateSession hibernateSession = null;

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

  /** session (hib object) */
  private Session session = null;
  
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
    processXmlFirstPass(importFile);
    processXmlSecondPass(importFile);
  }
  
  /** parser for file */
  private SAXReader reader;
  
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
   * @param importFile 
   */
  public void processXmlFirstPass(final File importFile) {
  
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_AUDIT, new HibernateHandler() {
  
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
  
            SAXReader theReader = null;
            FileInputStream fileInputStream = null;
            try {
              
              xstream = XmlExportUtils.xstream();
              
              XmlImportMain.this.hibernateSession = hibernateHandlerBean.getHibernateSession();
              XmlImportMain.this.session = hibernateSession.getSession();
              
              theReader = new SAXReader();
              
              XmlImportMain.this.reader = theReader;
              
              theReader.addHandler( "/grouperExport", 
                  new ElementHandler() {
                      public void onStart(ElementPath path) {
                        Element grouperExport = path.getCurrent();
                        XmlImportMain.this.importFileVersion = new GrouperVersion(grouperExport.attributeValue("version"));
                        XmlImportMain.logInfoAndPrintToScreen(
                            "grouper import: reading document:            " + GrouperUtil.fileCanonicalPath(importFile) 
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

              fileInputStream = new FileInputStream(importFile);
              
              theReader.read(fileInputStream);
              
              logInfoAndPrintToScreen("XML file contains " + XmlImportMain.this.totalImportFileCount + " records");
              
              XmlImportMain.this.originalDbCount = XmlImportMain.this.dbCount();
              
              logInfoAndPrintToScreen("Beginning import: database contains " + XmlImportMain.this.originalDbCount + " records");
              
            } catch (FileNotFoundException fnfe) {
              throw new RuntimeException("Problem reading file: " + GrouperUtil.fileCanonicalPath(importFile), fnfe);
            } catch (DocumentException de) {
              throw new RuntimeException("Problem reading file: " + GrouperUtil.fileCanonicalPath(importFile), de);
            } finally {
              GrouperUtil.closeQuietly(fileInputStream);
            }
            return null;
          }
    });
  }
  
  /**
   * get a db count of exportable rows
   * @return db count
   */
  public int dbCount() {
    int total = 0;
    total += XmlExportMember.dbCount();
    total += XmlExportStem.dbCount();
    total += XmlExportGroup.dbCount();
    total += XmlExportGroupType.dbCount();
    total += XmlExportField.dbCount();
    total += XmlExportGroupTypeTuple.dbCount();
    total += XmlExportComposite.dbCount();
    total += XmlExportAttribute.dbCount();
    total += XmlExportAttributeDef.dbCount();
    total += XmlExportMembership.dbCount();
    total += XmlExportAttributeDefName.dbCount();
    total += XmlExportRoleSet.dbCount();
    total += XmlExportAttributeAssignAction.dbCount();
    total += XmlExportAttributeAssignActionSet.dbCount();
    total += XmlExportAttributeAssign.dbCount();
    total += XmlExportAttributeAssignValue.dbCount();
    total += XmlExportAttributeDefNameSet.dbCount();
    total += XmlExportAttributeDefScope.dbCount();
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
   * @param importFile 
   */
  public void processXmlSecondPass(final File importFile) {
  
    HibernateSession.callbackHibernateSession(GrouperTransactionType.NONE, 
        AuditControl.WILL_AUDIT, new HibernateHandler() {
  
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
  
            SAXReader theReader = null;
            FileInputStream fileInputStream = null;
            try {
              
              XmlImportMain.this.hibernateSession = hibernateHandlerBean.getHibernateSession();
              XmlImportMain.this.session = hibernateSession.getSession();
              
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
  
              fileInputStream = new FileInputStream(importFile);
              
              theReader.read(fileInputStream);
              
              logInfoAndPrintToScreen("Ending import: processed " + XmlImportMain.this.currentRecordIndex + " records");

              long finalDbCount = XmlImportMain.this.dbCount();
              
              logInfoAndPrintToScreen("Ending import: database contains " + finalDbCount + " records");
              logInfoAndPrintToScreen("Ending import: " + XmlImportMain.this.insertCount + " inserts, " 
                  + XmlImportMain.this.updateCount + " updates, and " + XmlImportMain.this.skipCount + " skipped records");
              
            } catch (FileNotFoundException fnfe) {
              throw new RuntimeException("Problem reading file: " + GrouperUtil.fileCanonicalPath(importFile), fnfe);
            } catch (DocumentException de) {
              throw new RuntimeException("Problem reading file: " + GrouperUtil.fileCanonicalPath(importFile), de);
            }
            return null;
          }
    });
  }

  /**
   * increment the file count
   */
  public void incrementCurrentCount() {
    this.currentRecordIndex++;
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
