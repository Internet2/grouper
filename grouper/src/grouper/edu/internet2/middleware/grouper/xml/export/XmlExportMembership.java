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

import edu.internet2.middleware.grouper.Membership;
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
public class XmlExportMembership {

  /** uuid */
  private String uuid;
  
  /** creatorId */
  private String creatorId;

  /** createTime */
  private String createTime;

  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /** contextId */
  private String contextId;

  /** member id */
  private String memberId;

  /** field id */
  private String fieldId;
  
  /** owner group id */
  private String ownerGroupId;
  
  /** owner stem id */
  private String ownerStemId;

  /** owner attr def id */
  private String ownerAttrDefId;
  
  /** owner via composite it */
  private String viaCompositeId;
  
  /** T or F */
  private String enabled;

  /** timestamp of when enabled */
  private String enabledTimestamp;

  /** timestamp of when disabled */
  private String disableTimestamp;
  
  /** mshipType */
  private String type;

  /**
   * member id
   * @return member id
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * member id
   * @param memberId1
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
  }

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
   * owner group id
   * @return owner group id
   */
  public String getOwnerGroupId() {
    return this.ownerGroupId;
  }

  /**
   * owner group id
   * @param ownerGroupId1
   */
  public void setOwnerGroupId(String ownerGroupId1) {
    this.ownerGroupId = ownerGroupId1;
  }

  /**
   * owner stem id
   * @return owner stem id
   */
  public String getOwnerStemId() {
    return this.ownerStemId;
  }

  /**
   * owner stem id
   * @param ownerStemId1
   */
  public void setOwnerStemId(String ownerStemId1) {
    this.ownerStemId = ownerStemId1;
  }

  /**
   * owner attr def id
   * @return attr def id
   */
  public String getOwnerAttrDefId() {
    return this.ownerAttrDefId;
  }

  /**
   * owner attr def id
   * @param ownerAttrDefId1
   */
  public void setOwnerAttrDefId(String ownerAttrDefId1) {
    this.ownerAttrDefId = ownerAttrDefId1;
  }

  /**
   * via composite id
   * @return via composite id
   */
  public String getViaCompositeId() {
    return this.viaCompositeId;
  }

  /**
   * via composite id
   * @param viaCompositeId1
   */
  public void setViaCompositeId(String viaCompositeId1) {
    this.viaCompositeId = viaCompositeId1;
  }

  /**
   * enabled T|F
   * @return enabled T|F
   */
  public String getEnabled() {
    return this.enabled;
  }

  /**
   * enabled T|F
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }

  /**
   * enabled timestamp
   * @return enabled timestamp
   */
  public String getEnabledTimestamp() {
    return this.enabledTimestamp;
  }

  /**
   * enabled timestamp
   * @param enabledTimestamp1
   */
  public void setEnabledTimestamp(String enabledTimestamp1) {
    this.enabledTimestamp = enabledTimestamp1;
  }

  /**
   * disabled timestamp
   * @return disabled timestamp
   */
  public String getDisableTimestamp() {
    return this.disableTimestamp;
  }

  /**
   * disabled timestamp
   * @param disableTimestamp1
   */
  public void setDisableTimestamp(String disableTimestamp1) {
    this.disableTimestamp = disableTimestamp1;
  }

  /**
   * mship type
   * @return mship type
   */
  public String getType() {
    return this.type;
  }

  /**
   * mship type
   * @param mshipType1
   */
  public void setType(String mshipType1) {
    this.type = mshipType1;
  }

  /**
   * 
   */
  public XmlExportMembership() {
    
  }

  /**
   * @param membership
   * @param grouperVersion
   */
  public XmlExportMembership(GrouperVersion grouperVersion, Membership membership) {
    
    if (membership == null) {
      throw new RuntimeException();
    }
    
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    this.contextId = membership.getContextId();
    this.createTime = GrouperUtil.dateStringValue(membership.getCreateTime());
    this.creatorId = membership.getCreatorUuid();
    //note assume dont need to export smaller measures than millis
    this.disableTimestamp = GrouperUtil.dateStringValue(membership.getDisabledTimeDb());
    this.enabled = GrouperUtil.booleanValue(membership.getEnabledDb(), true) ? "T" : "F";
    this.enabledTimestamp = GrouperUtil.dateStringValue(membership.getEnabledTimeDb());
    this.fieldId = membership.getFieldId();
    this.hibernateVersionNumber = membership.getHibernateVersionNumber();
    this.memberId = membership.getMemberUuid();
    this.ownerAttrDefId = membership.getOwnerAttrDefId();
    this.ownerGroupId = membership.getOwnerGroupId();
    this.ownerStemId = membership.getOwnerStemId();
    this.type = membership.getType();
    this.uuid = membership.getImmediateMembershipId();
    this.viaCompositeId = membership.getViaCompositeId();
    
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
   * creatorId
   * @return creatorId
   */
  public String getCreatorId() {
    return this.creatorId;
  }

  /**
   * creatorId
   * @param creatorId1
   */
  public void setCreatorId(String creatorId1) {
    this.creatorId = creatorId1;
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
  public Membership toMembership() {
    Membership membership = new Membership();
    
    membership.setContextId(this.contextId);
    membership.setCreateTimeLong(GrouperUtil.dateLongValue(this.createTime));
    membership.setCreatorUuid(this.creatorId);
    membership.setDisabledTimeDb(GrouperUtil.dateLongValue(this.disableTimestamp));
    membership.setEnabled(GrouperUtil.booleanValue(this.enabled, true));
    membership.setEnabledTimeDb(GrouperUtil.dateLongValue(this.enabledTimestamp));
    membership.setFieldId(this.fieldId);
    membership.setHibernateVersionNumber(this.hibernateVersionNumber);
    membership.setMemberUuid(this.memberId);
    membership.setOwnerAttrDefId(this.ownerAttrDefId);
    membership.setOwnerGroupId(this.ownerGroupId);
    membership.setOwnerStemId(this.ownerStemId);
    membership.setType(this.type);
    membership.setImmediateMembershipId(this.uuid);
    membership.setViaCompositeId(this.viaCompositeId);
    
    return membership;
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
  public static void exportMemberships(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        Query query = session.createQuery(
            "select theMembership from ImmediateMembershipEntry as theMembership where theMembership.type = 'immediate' order by theMembership.memberUuid, theMembership.ownerId, theMembership.fieldId, theMembership.immediateMembershipId");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <memberships>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final Membership membership = (Membership)object;
              
              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments()) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!-- ");
                      XmlExportUtils.toStringMembership(null, writer, membership, false);
                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }
              
              XmlExportMembership xmlExportMembership = new XmlExportMembership(grouperVersion, membership);
              writer.write("    ");
              xmlExportMembership.toXml(grouperVersion, writer);
              writer.write("\n");
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the members element 
          writer.write("  </memberships>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming memberships", ioe);
        }
        return null;
      }
    });
  }

  /**
   * take a reader (e.g. dom reader) and convert to an xml export membership
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportMembership fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportMembership xmlExportMembership = (XmlExportMembership)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportMembership;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportMembership fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportMembership xmlExportMembership = (XmlExportMembership)xStream.fromXML(xml);
  
    return xmlExportMembership;
  }

}
