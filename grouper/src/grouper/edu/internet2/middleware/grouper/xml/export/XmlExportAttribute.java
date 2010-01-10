/**
 * @author mchyzer
 * $Id: XmlExportGroup.java 6216 2010-01-10 04:52:30Z mchyzer $
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 *
 */
public class XmlExportAttribute {

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
   * @param attribute
   * @param grouperVersion
   */
  public XmlExportAttribute(GrouperVersion grouperVersion, Attribute attribute) {
    
    if (attribute == null) {
      throw new RuntimeException();
    }
    
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    this.contextId = attribute.getContextId();
    this.fieldId = attribute.getFieldId();
    this.groupId = attribute.getGroupUuid();
    this.hibernateVersionNumber = attribute.getHibernateVersionNumber();
    this.uuid = attribute.getId();
    this.value = attribute.getValue();
    
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
   * 
   * @param writer
   */
  public static void exportAttributes(final Writer writer) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        Query query = session.createQuery(
            "select theAttribute from Attribute as theAttribute order by theAttribute.groupUuid, theAttribute.fieldId");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <attributes>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              Attribute attribute = (Attribute)object;
              XmlExportAttribute xmlExportAttribute = new XmlExportAttribute(grouperVersion, attribute);
              writer.write("    ");
              xmlExportAttribute.toXml(grouperVersion, writer);
              writer.write("\n");
            }
          } finally {
            HibUtils.closeQuietly(results);
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

}
