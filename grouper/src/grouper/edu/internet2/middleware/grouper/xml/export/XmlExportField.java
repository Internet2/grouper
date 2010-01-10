/**
 * @author mchyzer
 * $Id$
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


/**
 *
 */
public class XmlExportField {

  /** uuid */
  private String uuid;
  
  /** name */
  private String name;

  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /** contextId */
  private String contextId;

  /** group type uuid */
  private String groupTypeUuid;
  
  /** nullable T/F */
  private String nullable;
  
  /**
   * nullable T/F
   * @return if nullable
   */
  public String getNullable() {
    return this.nullable;
  }

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
   * nullable T/F
   * @param nullable1
   */
  public void setNullable(String nullable1) {
    this.nullable = nullable1;
  }

  /**
   * group type uuid
   * @return group type uuid
   */
  public String getGroupTypeUuid() {
    return this.groupTypeUuid;
  }

  /**
   * group type uuid
   * @param groupTypeUuid1
   */
  public void setGroupTypeUuid(String groupTypeUuid1) {
    this.groupTypeUuid = groupTypeUuid1;
  }

  /**
   * 
   */
  public XmlExportField() {
    
  }

  /**
   * @param field
   * @param grouperVersion
   */
  public XmlExportField(GrouperVersion grouperVersion, Field field) {
    
    if (field == null) {
      throw new RuntimeException();
    }
    
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    this.contextId = field.getContextId();
    this.groupTypeUuid = field.getGroupTypeUuid();
    this.hibernateVersionNumber = field.getHibernateVersionNumber();
    
    this.name = field.getName();
    this.nullable = field.getIsNullable() ? "T" : "F";
    this.readPrivilege = field.getReadPrivilege();
    this.type = field.getTypeString();
    this.uuid = field.getUuid();
    this.writePrivilege = field.getWritePrivilege();
    
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
    field.setGroupTypeUuid(this.groupTypeUuid);
    field.setHibernateVersionNumber(this.hibernateVersionNumber);
    field.setName(this.name);
    field.setIsNullable(GrouperUtil.booleanValue(this.nullable));
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
   * 
   * @param writer
   */
  public static void exportFields(final Writer writer) {
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
              Field field = (Field)object;
              XmlExportField xmlExportField = new XmlExportField(grouperVersion, field);
              writer.write("    ");
              xmlExportField.toXml(grouperVersion, writer);
              writer.write("\n");
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          //end the members element 
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

}
