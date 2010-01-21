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

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
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
public class XmlExportAttributeAssign {

  /** attributeAssignActionId */
  private String attributeAssignActionId;
  
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

  /**
   * 
   */
  public XmlExportAttributeAssign() {
    
  }

  
  
  /**
   * attributeAssignActionId
   * @return attributeAssignActionId
   */
  public String getAttributeAssignActionId() {
    return this.attributeAssignActionId;
  }



  /**
   * attributeAssignActionId
   * @param attributeAssignActionId1
   */
  public void setAttributeAssignActionId(String attributeAssignActionId1) {
    this.attributeAssignActionId = attributeAssignActionId1;
  }

  /**
   * attributeDefNameId
   */
  private String attributeDefNameId;

  /**
   * attributeDefNameId
   * @return attributeDefNameId
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }



  /**
   * attributeDefNameId
   * @param attributeDefNameId1
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
  }

  /**
   * disabledTime
   */
  private String disabledTime;

  
  
  /**
   * disabledTime
   * @return disabledTime
   */
  public String getDisabledTime() {
    return this.disabledTime;
  }



  /**
   * disabledTime
   * @param disabledTime1
   */
  public void setDisabledTime(String disabledTime1) {
    this.disabledTime = disabledTime1;
  }

  /** enabled */
  private String enabled;

  /**
   * enabled
   * @return enabled
   */
  public String getEnabled() {
    return this.enabled;
  }



  /**
   * enabled
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }

  /** enabledTime */
  private String enabledTime;
  

  /**
   * enabledTime
   * @return enabledTime
   */
  public String getEnabledTime() {
    return this.enabledTime;
  }



  /**
   * enabledTime
   * @param enabledTime1
   */
  public void setEnabledTime(String enabledTime1) {
    this.enabledTime = enabledTime1;
  }

  /** notes */
  private String notes;

  
  
  /**
   * notes
   * @return notes
   */
  public String getNotes() {
    return this.notes;
  }



  /**
   * notes
   * @param notes
   */
  public void setNotes(String notes) {
    this.notes = notes;
  }

  /** attributeAssignDelegatable */
  private String attributeAssignDelegatable;

  
  
  /**
   * attributeAssignDelegatable
   * @return attributeAssignDelegatable
   */
  public String getAttributeAssignDelegatable() {
    return this.attributeAssignDelegatable;
  }



  /**
   * attributeAssignDelegatable
   * @param attributeAssignDelegatable1
   */
  public void setAttributeAssignDelegatable(String attributeAssignDelegatable1) {
    this.attributeAssignDelegatable = attributeAssignDelegatable1;
  }

  /** attributeAssignType */
  private String attributeAssignType;
  
  
  
  /**
   * attributeAssignType
   * @return attributeAssignType
   */
  public String getAttributeAssignType() {
    return this.attributeAssignType;
  }



  /**
   * attributeAssignType
   * @param attributeAssignType1
   */
  public void setAttributeAssignType(String attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  /**
   * ownerAttributeAssignId
   */
  private String ownerAttributeAssignId;
  
  

  /**
   * ownerAttributeAssignId
   * @return ownerAttributeAssignId
   */
  public String getOwnerAttributeAssignId() {
    return this.ownerAttributeAssignId;
  }



  /**
   * ownerAttributeAssignId
   * @param ownerAttributeAssignId1
   */
  public void setOwnerAttributeAssignId(String ownerAttributeAssignId1) {
    this.ownerAttributeAssignId = ownerAttributeAssignId1;
  }

  /** ownerAttributeDefId */
  private String ownerAttributeDefId;
  
  
  
  /**
   * ownerAttributeDefId
   * @return ownerAttributeDefId
   */
  public String getOwnerAttributeDefId() {
    return this.ownerAttributeDefId;
  }



  /**
   * ownerAttributeDefId
   * @param ownerAttributeDefId1
   */
  public void setOwnerAttributeDefId(String ownerAttributeDefId1) {
    this.ownerAttributeDefId = ownerAttributeDefId1;
  }

  /** ownerGroupId */
  private String ownerGroupId;
  
  
  
  /**
   * ownerGroupId
   * @return ownerGroupId
   */
  public String getOwnerGroupId() {
    return this.ownerGroupId;
  }



  /**
   * ownerGroupId
   * @param ownerGroupId1
   */
  public void setOwnerGroupId(String ownerGroupId1) {
    this.ownerGroupId = ownerGroupId1;
  }

  /** ownerMemberId */
  private String ownerMemberId;
  
  
  
  /**
   * ownerMemberId
   * @return ownerMemberId
   */
  public String getOwnerMemberId() {
    return this.ownerMemberId;
  }



  /**
   * ownerMemberId
   * @param ownerMemberId1
   */
  public void setOwnerMemberId(String ownerMemberId1) {
    this.ownerMemberId = ownerMemberId1;
  }

  /** ownerMembershipId */
  private String ownerMembershipId;

  /** ownerStemId */
  private String ownerStemId;
  
  /**
   * ownerStemId
   * @return ownerStemId
   */
  public String getOwnerStemId() {
    return this.ownerStemId;
  }



  /**
   * ownerStemId
   * @param ownerStemId1
   */
  public void setOwnerStemId(String ownerStemId1) {
    this.ownerStemId = ownerStemId1;
  }



  /**
   * ownerMembershipId
   * @return ownerMembershipId
   */
  public String getOwnerMembershipId() {
    return this.ownerMembershipId;
  }



  /**
   * ownerMembershipId
   * @param ownerMembershipId1
   */
  public void setOwnerMembershipId(String ownerMembershipId1) {
    this.ownerMembershipId = ownerMembershipId1;
  }

  

  /**
   * @param attributeAssign
   * @param grouperVersion
   */
  public XmlExportAttributeAssign(GrouperVersion grouperVersion, AttributeAssign attributeAssign) {
    
    if (attributeAssign == null) {
      throw new RuntimeException();
    }
    
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    this.attributeAssignActionId = attributeAssign.getAttributeAssignActionId();
    this.attributeAssignDelegatable = attributeAssign.getAttributeAssignDelegatableDb();
    this.attributeAssignType = attributeAssign.getAttributeAssignTypeDb();
    this.attributeDefNameId = attributeAssign.getAttributeDefNameId();
    this.contextId = attributeAssign.getContextId();
    this.createTime = GrouperUtil.dateStringValue(attributeAssign.getCreatedOnDb());
    this.disabledTime = GrouperUtil.dateStringValue(attributeAssign.getDisabledTimeDb());
    this.enabled = attributeAssign.getEnabledDb();
    this.enabledTime = GrouperUtil.dateStringValue(attributeAssign.getEnabledTimeDb());
    this.hibernateVersionNumber = attributeAssign.getHibernateVersionNumber();
    this.modifierTime = GrouperUtil.dateStringValue(attributeAssign.getLastUpdatedDb());
    this.notes = attributeAssign.getNotes();
    this.ownerAttributeAssignId = attributeAssign.getOwnerAttributeAssignId();
    this.ownerAttributeDefId = attributeAssign.getOwnerAttributeDefId();
    this.ownerGroupId = attributeAssign.getOwnerGroupId();
    this.ownerMemberId = attributeAssign.getOwnerMemberId();
    this.ownerMembershipId = attributeAssign.getOwnerMembershipId();
    this.ownerStemId = attributeAssign.getOwnerStemId();
    this.uuid = attributeAssign.getId();
    
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
   * convert to AttributeAssign
   * @return the AttributeAssign
   */
  public AttributeAssign toAttributeAssign() {
    AttributeAssign attributeAssign = new AttributeAssign();
    
    attributeAssign.setAttributeAssignActionId(this.attributeAssignActionId);
    attributeAssign.setAttributeAssignDelegatableDb(this.attributeAssignDelegatable);
    attributeAssign.setAttributeAssignTypeDb(this.attributeAssignType);
    attributeAssign.setAttributeDefNameId(this.attributeDefNameId);
    attributeAssign.setContextId(this.contextId);
    attributeAssign.setCreatedOnDb(GrouperUtil.dateLongValue(this.createTime));
    attributeAssign.setDisabledTimeDb(GrouperUtil.dateLongValue(this.disabledTime));
    attributeAssign.setEnabledDb(this.enabled);
    attributeAssign.setEnabledTimeDb(GrouperUtil.dateLongValue(this.enabledTime));
    attributeAssign.setHibernateVersionNumber(this.hibernateVersionNumber);
    attributeAssign.setLastUpdatedDb(GrouperUtil.dateLongValue(this.modifierTime));
    attributeAssign.setId(this.uuid);
    attributeAssign.setNotes(this.notes);
    attributeAssign.setOwnerAttributeAssignId(this.ownerAttributeAssignId);
    attributeAssign.setOwnerAttributeDefId(this.ownerAttributeDefId);
    attributeAssign.setOwnerGroupId(this.ownerGroupId);
    attributeAssign.setOwnerMemberId(this.ownerMemberId);
    attributeAssign.setOwnerMembershipId(this.ownerMembershipId);
    attributeAssign.setOwnerStemId(this.ownerStemId);
    
    return attributeAssign;
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
   * @param xmlExportMain
   * @param writer
   */
  public static void exportAttributeAssigns(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        //order by attributeAssignId so the ones not about assigns are first.
        Query query = session.createQuery(
            "select theAttributeAssign from AttributeAssign as theAttributeAssign order by theAttributeAssign.ownerAttributeAssignId, theAttributeAssign.id");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <attributeAssigns>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final AttributeAssign attributeAssign = (AttributeAssign)object;
              
              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments()) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!--  ");
                      
                      XmlExportUtils.toStringAttributeAssign(null, writer, attributeAssign, false);

                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }
              
              XmlExportAttributeAssign xmlExportAttributeAssign = new XmlExportAttributeAssign(grouperVersion, attributeAssign);
              writer.write("    ");
              xmlExportAttributeAssign.toXml(grouperVersion, writer);
              writer.write("\n");
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the attribute assigns element 
          writer.write("  </attributeAssigns>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming attributeAssigns", ioe);
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
  public static XmlExportAttributeAssign fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeAssign xmlExportAttributeAssign = (XmlExportAttributeAssign)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportAttributeAssign;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportAttributeAssign fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportAttributeAssign xmlExportAttributeAssign = (XmlExportAttributeAssign)xStream.fromXML(xml);
  
    return xmlExportAttributeAssign;
  }

}
