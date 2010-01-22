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

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
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
public class XmlExportAttributeAssignAction {

  /** uuid */
  private String uuid;
  
  /** createTime */
  private String createTime;

  /** modifierTime */
  private String modifierTime;

  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /** contextId */
  private String contextId;

  /** attributeDefId */
  private String attributeDefId;

  /** name */
  private String name;
  
  /**
   * name
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * attributeDefId
   * @return attributeDefId
   */
  public String getAttributeDefId() {
    return this.attributeDefId;
  }

  /**
   * attributeDefId
   * @param attributeDefId1
   */
  public void setAttributeDefId(String attributeDefId1) {
    this.attributeDefId = attributeDefId1;
  }

  /**
   * 
   */
  public XmlExportAttributeAssignAction() {
    
  }

  /**
   * @param attributeAssignAction
   * @param grouperVersion
   */
  public XmlExportAttributeAssignAction(GrouperVersion grouperVersion, AttributeAssignAction attributeAssignAction) {
    
    if (attributeAssignAction == null) {
      throw new RuntimeException();
    }
    
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    this.attributeDefId = attributeAssignAction.getAttributeDefId();
    this.contextId = attributeAssignAction.getContextId();
    this.createTime = GrouperUtil.dateStringValue(attributeAssignAction.getCreatedOnDb());
    this.hibernateVersionNumber = attributeAssignAction.getHibernateVersionNumber();
    this.name = attributeAssignAction.getNameDb();
    this.modifierTime = GrouperUtil.dateStringValue(attributeAssignAction.getLastUpdatedDb());
    this.uuid = attributeAssignAction.getId();
    
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
   * createTime
   * @return createTime
   */
  public String getCreateTime() {
    return this.createTime;
  }

  /**
   * createTime
   * @param createTime1
   */
  public void setCreateTime(String createTime1) {
    this.createTime = createTime1;
  }

  /**
   * modifierTime
   * @return modifierTime
   */
  public String getModifierTime() {
    return this.modifierTime;
  }

  /**
   * modifierTime
   * @param modifierTime1
   */
  public void setModifierTime(String modifierTime1) {
    this.modifierTime = modifierTime1;
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
   * convert to AttributeAssignAction
   * @return the AttributeAssignAction
   */
  public AttributeAssignAction toAttributeAssignAction() {
    AttributeAssignAction attributeAssignAction = new AttributeAssignAction();
    
    attributeAssignAction.setAttributeDefId(this.attributeDefId);
    attributeAssignAction.setContextId(this.contextId);
    attributeAssignAction.setCreatedOnDb(GrouperUtil.dateLongValue(this.createTime));
    attributeAssignAction.setHibernateVersionNumber(this.hibernateVersionNumber);
    attributeAssignAction.setLastUpdatedDb(GrouperUtil.dateLongValue(this.modifierTime));
    attributeAssignAction.setNameDb(this.name);
    attributeAssignAction.setId(this.uuid);
    
    return attributeAssignAction;
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
   * @param xmlExportMain
   */
  public static void exportAttributeAssignActions(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all role sets (immediate is depth = 1)
        Query query = session.createQuery(
            "select theAttributeAssignAction from AttributeAssignAction as theAttributeAssignAction order by theAttributeAssignAction.attributeDefId, theAttributeAssignAction.nameDb");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <attributeAssignActions>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final AttributeAssignAction attributeAssignAction = (AttributeAssignAction)object;
              
              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments()) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!-- ");
                      
                      XmlExportUtils.toStringAttributeDef(null, writer, attributeAssignAction.getAttributeDefId(), true);

                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }
              
              XmlExportAttributeAssignAction xmlExportRoleSet = new XmlExportAttributeAssignAction(grouperVersion, attributeAssignAction);
              writer.write("    ");
              xmlExportRoleSet.toXml(grouperVersion, writer);
              writer.write("\n");
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the attribute assign actions element 
          writer.write("  </attributeAssignActions>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributeAssignActions", ioe);
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
  public static XmlExportAttributeAssignAction fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeAssignAction xmlExportAttributeAssignAction = (XmlExportAttributeAssignAction)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAttributeAssignAction;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAttributeAssignAction fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeAssignAction xmlExportAttributeAssignAction = (XmlExportAttributeAssignAction)xStream.fromXML(xml);
  
    return xmlExportAttributeAssignAction;
  }

}
