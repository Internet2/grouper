/**
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
 */
/*
 * @author mchyzer
 * $Id: XmlAuditType.java,v 1.1 2009-03-31 06:58:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.Dom4JReader;

import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;


/**
 * type of audit
 */
@SuppressWarnings("serial")
public class XmlExportAuditType {

  /**
   * 
   */
  public XmlExportAuditType() {
    super();
  }

  /** id of this type */
  private String id;

  /** friendly label for the audit type */
  private String auditCategory;
  
  /** friendly label for the action in the category */
  private String actionName;
  
  /** when this record was last updated */
  private String lastUpdated;
  
  /** when this record was created */
  private String createdOn;
  
  /** label for the string01 field */
  private String labelString01;
  
  /** label for the string02 field */
  private String labelString02;
  
  /** label for the string03 field */
  private String labelString03;
  
  /** label for the string04 field */
  private String labelString04;
  
  /** label for the string05 field */
  private String labelString05;
  
  /** label for the string06 field */
  private String labelString06;
  
  /** label for the string07 field */
  private String labelString07;
  
  /** label for the string08 field */
  private String labelString08;
  
  /** context id ties multiple db changes  */
  private String contextId;
  
  /** label for the int01 field */
  private String labelInt01;
  
  /** label for the int02 field */
  private String labelInt02;

  /** label for the int03 field */
  private String labelInt03;
  
  /** label for the int04 field */
  private String labelInt04;
  
  /** label for the int05 field */
  private String labelInt05;

  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportAuditType.class);

  /**
   * 
   */
  private static final String AUDIT_TYPES_XPATH = "/grouperExport/auditTypes";

  /**
   * 
   */
  private static final String XML_EXPORT_AUDIT_TYPE_XPATH = "/grouperExport/auditTypes/XmlExportAuditType";

  /**
   * uuid of row
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * uuid of row
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * category of audit
   * @return audit type
   */
  public String getAuditCategory() {
    return this.auditCategory;
  }

  /**
   * category of audit
   * @param auditType1
   */
  public void setAuditCategory(String auditType1) {
    this.auditCategory = auditType1;
  }

  /**
   * action within the audit category
   * @return the action name
   */
  public String getActionName() {
    return this.actionName;
  }

  /**
   * action within the audit category
   * @param actionName
   */
  public void setActionName(String actionName) {
    this.actionName = actionName;
  }

  /**
   * when last updated
   * @return timestamp
   */
  public String getLastUpdated() {
    return this.lastUpdated;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(String lastUpdated1) {
    this.lastUpdated = lastUpdated1;
  }

  /**
   * get db count
   * @return db count
   */
  public static long dbCount() {
    long result = HibernateSession.byHqlStatic().createQuery("select count(*) from AuditType").uniqueResult(Long.class);
    return result;
  }

  /**
   * when created
   * @return timestamp
   */
  public String getCreatedOn() {
    return this.createdOn;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(String createdOn1) {
    this.createdOn = createdOn1;
  }

  /**
   * label for string01
   * @return label
   */
  public String getLabelString01() {
    return this.labelString01;
  }

  /**
   * label for string01
   * @param labelString01a
   */
  public void setLabelString01(String labelString01a) {
    this.labelString01 = labelString01a;
  }

  /**
   * label for string02
   * @return label
   */
  public String getLabelString02() {
    return this.labelString02;
  }

  /**
   * label for string02
   * @param labelString02a
   */
  public void setLabelString02(String labelString02a) {
    this.labelString02 = labelString02a;
  }

  /**
   * label for string03
   * @return label
   */
  public String getLabelString03() {
    return this.labelString03;
  }

  /**
   * label for string03
   * @param labelString03a
   */
  public void setLabelString03(String labelString03a) {
    this.labelString03 = labelString03a;
  }

  /**
   * label for string04
   * @return label
   */
  public String getLabelString04() {
    return this.labelString04;
  }

  /**
   * label for string04
   * @param labelString04a
   */
  public void setLabelString04(String labelString04a) {
    this.labelString04 = labelString04a;
  }

  /**
   * label for string05
   * @return label
   */
  public String getLabelString05() {
    return this.labelString05;
  }

  /**
   * label for string05
   * @param labelString05a
   */
  public void setLabelString05(String labelString05a) {
    this.labelString05 = labelString05a;
  }

  /**
   * context id ties multiple db changes
   * @return id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id ties multiple db changes
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * label for int01
   * @return label
   */
  public String getLabelInt01() {
    return this.labelInt01;
  }

  /**
   * label for int01
   * @param labelInt01a
   */
  public void setLabelInt01(String labelInt01a) {
    this.labelInt01 = labelInt01a;
  }

  /**
   * label for int02
   * @return label
   */
  public String getLabelInt02() {
    return this.labelInt02;
  }

  /**
   * label for int02
   * @param labelInt02a
   */
  public void setLabelInt02(String labelInt02a) {
    this.labelInt02 = labelInt02a;
  }
  
  /**
   * label for int03
   * @return label
   */
  public String getLabelInt03() {
    return this.labelInt03;
  }

  /**
   * label for int03
   * @param labelInt03a
   */
  public void setLabelInt03(String labelInt03a) {
    this.labelInt03 = labelInt03a;
  }

  /**
   * label for int04
   * @return label
   */
  public String getLabelInt04() {
    return this.labelInt04;
  }

  /**
   * label for int04
   * @param labelInt04a
   */
  public void setLabelInt04(String labelInt04a) {
    this.labelInt04 = labelInt04a;
  }

  /**
   * label for int05
   * @return label
   */
  public String getLabelInt05() {
    return this.labelInt05;
  }

  /**
   * label for int05
   * @param labelInt05
   */
  public void setLabelInt05(String labelInt05) {
    this.labelInt05 = labelInt05;
  }
  
  /**
   * label for the string06 field
   * @return label
   */
  public String getLabelString06() {
    return this.labelString06;
  }

  /**
   * label for the string06 field
   * @param labelString06a
   */
  public void setLabelString06(String labelString06a) {
    this.labelString06 = labelString06a;
  }

  /**
   * label for the string07 field
   * @return label
   */
  public String getLabelString07() {
    return this.labelString07;
  }

  /**
   * label for the string07 field
   * @param labelString07a
   */
  public void setLabelString07(String labelString07a) {
    this.labelString07 = labelString07a;
  }

  /**
   * label for the string08 field
   * @return label
   */
  public String getLabelString08() {
    return this.labelString08;
  }

  /**
   * label for the string08 field
   * @param labelString08a
   */
  public void setLabelString08(String labelString08a) {
    this.labelString08 = labelString08a;
  }

  /**
   * 
   * @param writer
   * @param xmlExportMain 
   */
  public static void exportAuditTypes(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the audittypes
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all auditTypes in order
        Query query = session.createQuery(
            "select theAuditType from AuditType as theAuditType order by theAuditType.auditCategory, theAuditType.actionName");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <auditTypes>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              AuditType auditType = (AuditType)object;
              XmlExportAuditType xmlExportAuditType = auditType.xmlToExportAuditType(grouperVersion);
              writer.write("    ");
              xmlExportAuditType.toXml(grouperVersion, writer);
              writer.write("\n");
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          //end the auditTypes element 
          writer.write("  </auditTypes>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming auditTypes", ioe);
        }
        return null;
      }
    });
  }

  /**
   * take a reader (e.g. dom reader) and convert to an xml export audit type
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportAuditType fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAuditType xmlExportAuditType = (XmlExportAuditType)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAuditType;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAuditType fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAuditType xmlExportAuditType = (XmlExportAuditType)xStream.fromXML(xml);
  
    return xmlExportAuditType;
  }

  /**
   * parse the xml file for groups
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( AUDIT_TYPES_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
            }
            public void onEnd(ElementPath path) {
                // process a ROW element
                Element row = path.getCurrent();
  
                // prune the tree
                row.detach();
            }
        }
    );
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_AUDIT_TYPE_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
                // do nothing here...    
            }
            public void onEnd(ElementPath path) {
                // process a ROW element
                Element row = path.getCurrent();
  
                // prune the tree
                row.detach();
  
                xmlImportMain.incrementTotalImportFileCount();
            }
        }
    );
  
  }

  /**
   * parse the xml file for groups
   * @param xmlImportMain
   */
  public static void processXmlSecondPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( AUDIT_TYPES_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
            }
            public void onEnd(ElementPath path) {
                // process a ROW element
                Element row = path.getCurrent();
  
                // prune the tree
                row.detach();
            }
        }
    );
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_AUDIT_TYPE_XPATH, 
        new ElementHandler() {
            public void onStart(ElementPath path) {
                // do nothing here...    
            }
            public void onEnd(ElementPath path) {
  
              Element row = null;
              try {
                // process a ROW element
                row = path.getCurrent();
  
                // prune the tree
                row.detach();
  
                XmlExportAuditType xmlExportAuditTypeFromFile = (XmlExportAuditType)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                AuditType auditType = xmlExportAuditTypeFromFile.toAuditType();
                
                XmlExportUtils.syncImportable(auditType, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing audittype: " + XmlExportUtils.toString(row), re);
                throw re;
              }
            }
        }
    );
  
  }

  /**
   * convert to audit type
   * @return the audit type
   */
  public AuditType toAuditType() {
    AuditType auditType = new AuditType();
    
    auditType.setActionName(this.getActionName());
    auditType.setAuditCategory(this.getAuditCategory());
    auditType.setContextId(this.contextId);
    auditType.setCreatedOnDb(GrouperUtil.dateLongValue(this.createdOn));
    auditType.setHibernateVersionNumber(this.hibernateVersionNumber);
    auditType.setId(this.id);
    auditType.setLabelInt01(this.labelInt01);
    auditType.setLabelInt02(this.labelInt02);
    auditType.setLabelInt03(this.labelInt03);
    auditType.setLabelInt04(this.labelInt04);
    auditType.setLabelInt05(this.labelInt05);
    auditType.setLabelString01(this.labelString01);
    auditType.setLabelString02(this.labelString02);
    auditType.setLabelString03(this.labelString03);
    auditType.setLabelString04(this.labelString04);
    auditType.setLabelString05(this.labelString05);
    auditType.setLabelString06(this.labelString06);
    auditType.setLabelString07(this.labelString07);
    auditType.setLabelString08(this.labelString08);
    auditType.setLastUpdatedDb(GrouperUtil.dateLongValue(this.lastUpdated));
    return auditType;
  }

  /**
   * @param exportVersion
   * @return the xml string
   */
  public String toXml(GrouperVersion exportVersion) {
    StringWriter stringWriter = new StringWriter();
    this.toXml(exportVersion, stringWriter);
    return stringWriter.toString();
  }

  /**
   * @param exportVersion 
   * @param writer
   */
  public void toXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, Writer writer) {
    XStream xStream = XmlExportUtils.xstream();
  
    CompactWriter compactWriter = new CompactWriter(writer);
    
    xStream.marshal(this, compactWriter);
  
  }

  /**
   * hibernateVersionNumber
   * @return hibernateVersionNumber
   */
  public long getHibernateVersionNumber() {
    return this.hibernateVersionNumber;
  }

  /**
   * hibernateVersionNumber
   * @param hibernateVersionNumber1
   */
  public void setHibernateVersionNumber(long hibernateVersionNumber1) {
    this.hibernateVersionNumber = hibernateVersionNumber1;
  }

}
