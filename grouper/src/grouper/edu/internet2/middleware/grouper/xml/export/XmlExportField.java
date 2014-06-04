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

import edu.internet2.middleware.grouper.Field;
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
public class XmlExportField {

  /**
   * 
   */
  private static final String XML_EXPORT_FIELD_XPATH = "/grouperExport/fields/XmlExportField";

  /**
   * 
   */
  private static final String FIELDS_XPATH = "/grouperExport/fields";

  /** uuid */
  private String uuid;
  
  /** name */
  private String name;

  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /** contextId */
  private String contextId;
  
  /**
   * read privilege
   */
  private String readPrivilege;
  
  /**
   * read privilege
   * @return read privilege
   */
  public String getReadPrivilege() {
    return this.readPrivilege;
  }

  /**
   * read privilege
   * @param readPrivilege1
   */
  public void setReadPrivilege(String readPrivilege1) {
    this.readPrivilege = readPrivilege1;
  }

  /** write privilege */
  private String writePrivilege;

  /**
   * write privilege
   * @return write privilege
   */
  public String getWritePrivilege() {
    return this.writePrivilege;
  }

  /**
   * write privilege
   * @param writePrivilege1
   */
  public void setWritePrivilege(String writePrivilege1) {
    this.writePrivilege = writePrivilege1;
  }

  /**
   * type
   */
  private String type;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportField.class);
  
  
  /**
   * type
   * @return type
   */
  public String getType() {
    return this.type;
  }

  /**
   * type
   * @param type1
   */
  public void setType(String type1) {
    this.type = type1;
  }

  /**
   * 
   */
  public XmlExportField() {
    
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
   * name
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name
   * @param name
   */
  public void setName(String name) {
    this.name = name;
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
   * convert to group
   * @return the group
   */
  public Field toField() {
    Field field = new Field();
    
    field.setContextId(this.contextId);
    field.setHibernateVersionNumber(this.hibernateVersionNumber);
    field.setName(this.name);
    field.setReadPrivilege(this.readPrivilege);
    field.setTypeString(this.type);
    field.setUuid(this.uuid);
    field.setWritePrivilege(this.writePrivilege);
    
    return field;
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
    xmlImportMain.getReader().addHandler( FIELDS_XPATH, 
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
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_FIELD_XPATH, 
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
  
                XmlExportField xmlExportFieldFromFile = (XmlExportField)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                Field field = xmlExportFieldFromFile.toField();
                
                XmlExportUtils.syncImportable(field, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing field: " + XmlExportUtils.toString(row), re);
                throw re;
              }
            }
        }
    );
  
  }

  /**
   * get db count
   * @return db count
   */
  public static long dbCount() {
    long result = HibernateSession.byHqlStatic().createQuery("select count(*) from Field").uniqueResult(Long.class);
    return result;
  }
  

  /**
   * 
   * @param writer
   * @param xmlExportMain
   */
  public static void exportFields(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        Query query = session.createQuery(
            "select theField from Field as theField order by theField.name");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <fields>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final Field field = (Field)object;
              
              XmlExportField xmlExportField = field.xmlToExportField(grouperVersion);
              writer.write("    ");
              xmlExportField.toXml(grouperVersion, writer);
              writer.write("\n");
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the fields element 
          writer.write("  </fields>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming fields", ioe);
        }
        return null;
      }
    });
  }

  /**
   * take a reader (e.g. dom reader) and convert to an xml export field
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportField fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportField xmlExportField = (XmlExportField)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportField;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportField fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportField xmlExportField = (XmlExportField)xStream.fromXML(xml);
  
    return xmlExportField;
  }

  /**
   * parse the xml file for fields
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( FIELDS_XPATH, 
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

    xmlImportMain.getReader().addHandler( XML_EXPORT_FIELD_XPATH, 
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
