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
 * $Id: XmlExportGroup.java 6216 2010-01-10 04:52:30Z mchyzer $
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

import edu.internet2.middleware.grouper.Attribute;
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
 *
 */
public class XmlExportAttribute {

  /**
   * 
   */
  private static final String XML_EXPORT_ATTRIBUTE_XPATH = "/grouperExport/attributes/XmlExportAttribute";

  /**
   * 
   */
  private static final String ATTRIBUTES_XPATH = "/grouperExport/attributes";

  /** uuid */
  private String uuid;
  
  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /** contextId */
  private String contextId;
  
  /** groupId */
  private String groupId;

  /** value */
  private String value;
  
  /**
   * value
   * @return value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * value
   * @param value1
   */
  public void setValue(String value1) {
    this.value = value1;
  }

  /**
   * group id
   * @return group id
   */
  public String getGroupId() {
    return this.groupId;
  }

  /**
   * group id
   * @param groupId1
   */
  public void setGroupId(String groupId1) {
    this.groupId = groupId1;
  }

  /**
   * field id
   */
  private String fieldId;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportAttribute.class);
  
  /**
   * field id  
   * @return field id
   */
  public String getFieldId() {
    return this.fieldId;
  }

  /**
   * field id
   * @param fieldId1
   */
  public void setFieldId(String fieldId1) {
    this.fieldId = fieldId1;
  }
  
  /**
   * 
   */
  public XmlExportAttribute() {
    
  }

  /**
   * uuid
   * @return uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
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

  /**
   * contextId
   * @return contextId
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * contextId
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }
  
  /**
   * convert to attribute
   * @return the attribute
   */
  public Attribute toAttribute() {
    Attribute attribute = new Attribute();
    
    attribute.setContextId(this.contextId);
    attribute.setFieldId(this.fieldId);
    attribute.setGroupUuid(this.groupId);
    attribute.setHibernateVersionNumber(this.hibernateVersionNumber);
    attribute.setId(this.uuid);
    attribute.setValue(this.value);
    
    return attribute;
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
   * parse the xml file for groups
   * @param xmlImportMain
   */
  public static void processXmlSecondPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( ATTRIBUTES_XPATH, 
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
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_XPATH, 
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
  
                XmlExportAttribute xmlExportAttributeFromFile = (XmlExportAttribute)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                Attribute attribute = xmlExportAttributeFromFile.toAttribute();
                
                XmlExportUtils.syncImportable(attribute, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing attributes: " + XmlExportUtils.toString(row), re);
                throw re;
              }
            }
        }
    );
  
  }

  /**
   * get db count
   * @param xmlExportMain 
   * @return db count
   */
  public static long dbCount(XmlExportMain xmlExportMain) {
    long result = HibernateSession.byHqlStatic().createQuery("select count(theAttribute) " 
        + exportFromOnQuery(xmlExportMain, false)).uniqueResult(Long.class);
    return result;
  }
  
  /**
   * get the query from the FROM clause on to the end for export
   * @param xmlExportMain
   * @param includeOrderBy 
   * @return the export query
   */
  private static String exportFromOnQuery(XmlExportMain xmlExportMain, boolean includeOrderBy) {
    //select all members in order
    StringBuilder queryBuilder = new StringBuilder();
    if (!xmlExportMain.filterStemsOrObjects()) {
      queryBuilder.append(" from Attribute as theAttribute ");
    } else {
      queryBuilder.append(
          " from Attribute as theAttribute where exists ( select theGroup from Group as theGroup " +
          " where theAttribute.groupUuid = theGroup.uuid and ( ");
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theGroup", "nameDb", false);
      queryBuilder.append(" ) ) ");
    }
    if (includeOrderBy) {
      queryBuilder.append(" order by theAttribute.groupUuid, theAttribute.fieldId ");
    }
    return queryBuilder.toString();
  }

  /**
   * 
   * @param writer
   * @param xmlExportMain
   */
  public static void exportAttributes(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        Query query = session.createQuery(
            "select distinct theAttribute " + exportFromOnQuery(xmlExportMain, true));
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <attributes>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final Attribute attribute = (Attribute)object;
              
              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments()) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!-- ");
                      XmlExportUtils.toStringGroup(null, writer, attribute.getGroupUuid(), true);
                      writer.write("attribute: ");
                      writer.write(attribute.getAttrName());
                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }
              
              XmlExportAttribute xmlExportAttribute = attribute.xmlToExportAttribute(grouperVersion);
              writer.write("    ");
              xmlExportAttribute.toXml(grouperVersion, writer);
              writer.write("\n");
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the members element 
          writer.write("  </attributes>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributes", ioe);
        }
        return null;
      }
    });
  }

  /**
   * take a reader (e.g. dom reader) and convert to an xml export group
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportAttribute fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttribute xmlExportAttribute = (XmlExportAttribute)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAttribute;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAttribute fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttribute xmlExportAttribute = (XmlExportAttribute)xStream.fromXML(xml);
  
    return xmlExportAttribute;
  }

  /**
   * parse the xml file for attributes
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( ATTRIBUTES_XPATH, 
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

    xmlImportMain.getReader().addHandler( XML_EXPORT_ATTRIBUTE_XPATH, 
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
}
