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

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
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
public class XmlExportAttributeAssignActionSet {

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

  /** depth */
  private long depth;
  
  /**
   * depth
   * @return depth
   */
  public long getDepth() {
    return this.depth;
  }

  /**
   * depth
   * @param depth1
   */
  public void setDepth(long depth1) {
    this.depth = depth1;
  }

  /** ifHasAttributeAssignActionId */
  private String ifHasAttributeAssignActionId;
  
  /**
   * ifHasAttributeAssignActionId
   * @return ifHasAttributeAssignActionId
   */
  public String getIfHasAttributeAssignActionId() {
    return this.ifHasAttributeAssignActionId;
  }

  /**
   * ifHasAttributeAssignActionId
   * @param ifHasAttributeAssignActionId1
   */
  public void setIfHasAttributeAssignActionId(String ifHasAttributeAssignActionId1) {
    this.ifHasAttributeAssignActionId = ifHasAttributeAssignActionId1;
  }

  /**
   * thenHasAttributeAssignActionId
   */
  private String thenHasAttributeAssignActionId;
  
  /**
   * thenHasAttributeAssignActionId
   * @return thenHasAttributeAssignActionId
   */
  public String getThenHasAttributeAssignActionId() {
    return this.thenHasAttributeAssignActionId;
  }

  /**
   * thenHasAttributeAssignActionId
   * @param thenHasAttributeAssignActionId
   */
  public void setThenHasAttributeAssignActionId(String thenHasAttributeAssignActionId) {
    this.thenHasAttributeAssignActionId = thenHasAttributeAssignActionId;
  }

  /**
   * parentAttributeAssignActionSetId
   */
  private String parentAttributeAssignActionSetId;
  
  /**
   * parentAttributeAssignActionSetId
   * @return parentAttributeAssignActionSetId
   */
  public String getParentAttributeAssignActionSetId() {
    return this.parentAttributeAssignActionSetId;
  }

  /**
   * parentAttributeAssignActionSetId
   * @param parentAttributeAssignActionSetId
   */
  public void setParentAttributeAssignActionSetId(String parentAttributeAssignActionSetId) {
    this.parentAttributeAssignActionSetId = parentAttributeAssignActionSetId;
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
   * 
   */
  public XmlExportAttributeAssignActionSet() {
    
  }

  /**
   * @param attributeAssignActionSet
   * @param grouperVersion
   */
  public XmlExportAttributeAssignActionSet(GrouperVersion grouperVersion, AttributeAssignActionSet attributeAssignActionSet) {
    
    if (attributeAssignActionSet == null) {
      throw new RuntimeException();
    }
    
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    this.contextId = attributeAssignActionSet.getContextId();
    this.createTime = GrouperUtil.dateStringValue(attributeAssignActionSet.getCreatedOnDb());
    this.depth = attributeAssignActionSet.getDepth();
    this.hibernateVersionNumber = attributeAssignActionSet.getHibernateVersionNumber();
    this.ifHasAttributeAssignActionId = attributeAssignActionSet.getIfHasAttrAssignActionId();
    this.modifierTime = GrouperUtil.dateStringValue(attributeAssignActionSet.getLastUpdatedDb());
    this.parentAttributeAssignActionSetId = attributeAssignActionSet.getParentAttrAssignActionSetId();
    this.thenHasAttributeAssignActionId = attributeAssignActionSet.getThenHasAttrAssignActionId();
    this.type = attributeAssignActionSet.getTypeDb();
    this.uuid = attributeAssignActionSet.getId();
    
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
   * convert to attributeAssignActionSet
   * @return the attributeAssignActionSet
   */
  public AttributeAssignActionSet toAttributeAssignActionSet() {
    AttributeAssignActionSet attributeAssignActionSet = new AttributeAssignActionSet();
    
    attributeAssignActionSet.setContextId(this.contextId);
    attributeAssignActionSet.setCreatedOnDb(GrouperUtil.dateLongValue(this.createTime));
    attributeAssignActionSet.setDepth((int)this.depth);
    attributeAssignActionSet.setHibernateVersionNumber(this.hibernateVersionNumber);
    attributeAssignActionSet.setIfHasAttrAssignActionId(this.ifHasAttributeAssignActionId);
    attributeAssignActionSet.setLastUpdatedDb(GrouperUtil.dateLongValue(this.modifierTime));
    attributeAssignActionSet.setParentAttrAssignActionSetId(this.parentAttributeAssignActionSetId);
    attributeAssignActionSet.setThenHasAttrAssignActionId(this.thenHasAttributeAssignActionId);
    attributeAssignActionSet.setTypeDb(this.type);
    attributeAssignActionSet.setId(this.uuid);
    
    return attributeAssignActionSet;
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
  public static void exportAttributeAssignActionSets(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all action sets (immediate is depth = 1)
        Query query = session.createQuery(
            "select theAttributeAssignActionSet from AttributeAssignActionSet as theAttributeAssignActionSet where theAttributeAssignActionSet.typeDb = 'immediate' order by theAttributeAssignActionSet.id");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <attributeAssignActionSets>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final AttributeAssignActionSet attributeAssignActionSet = (AttributeAssignActionSet)object;
              //TODO add in comments
              XmlExportAttributeAssignActionSet xmlExportAttributeAssignActionSet = new XmlExportAttributeAssignActionSet(grouperVersion, attributeAssignActionSet);
              writer.write("    ");
              xmlExportAttributeAssignActionSet.toXml(grouperVersion, writer);
              writer.write("\n");
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          //end the members element 
          writer.write("  </attributeAssignActionSets>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributeAssignActionSets", ioe);
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
  public static XmlExportAttributeAssignActionSet fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeAssignActionSet xmlExportAttributeAssignActionSet = (XmlExportAttributeAssignActionSet)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAttributeAssignActionSet;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAttributeAssignActionSet fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeAssignActionSet xmlExportAttributeAssignActionSet = (XmlExportAttributeAssignActionSet)xStream.fromXML(xml);
  
    return xmlExportAttributeAssignActionSet;
  }

}
