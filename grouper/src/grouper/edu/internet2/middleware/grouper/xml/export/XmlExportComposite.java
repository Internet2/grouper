/**
 * @author mchyzer
 * $Id: XmlExportGroup.java 6216 2010-01-10 04:52:30Z mchyzer $
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import edu.internet2.middleware.grouper.Composite;
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
public class XmlExportComposite {

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
  
  /** owner */
  private String owner;
  
  /** leftFactor */
  private String leftFactor;
  
  /** rightFactor */
  private String rightFactor;
  
  /** type */
  private String type;

  /**
   * owner
   * @return owner
   */
  public String getOwner() {
    return this.owner;
  }

  /**
   * owner
   * @param owner1
   */
  public void setOwner(String owner1) {
    this.owner = owner1;
  }

  /**
   * left factor
   * @return left factor
   */
  public String getLeftFactor() {
    return this.leftFactor;
  }

  /**
   * left factor
   * @param leftFactor1
   */
  public void setLeftFactor(String leftFactor1) {
    this.leftFactor = leftFactor1;
  }

  /**
   * right factor
   * @return right factor
   */
  public String getRightFactor() {
    return this.rightFactor;
  }

  /**
   * right factor
   * @param rightFactor1
   */
  public void setRightFactor(String rightFactor1) {
    this.rightFactor = rightFactor1;
  }

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
  public XmlExportComposite() {
    
  }

  /**
   * @param composite
   * @param grouperVersion
   */
  public XmlExportComposite(GrouperVersion grouperVersion, Composite composite) {
    
    if (composite == null) {
      throw new RuntimeException();
    }
    
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    this.contextId = composite.getContextId();
    this.createTime = GrouperUtil.dateStringValue(new Date(composite.getCreateTime()));
    this.creatorId = composite.getCreatorUuid();
    this.hibernateVersionNumber = composite.getHibernateVersionNumber();
    this.leftFactor = composite.getLeftFactorUuid();
    this.owner = composite.getFactorOwnerUuid();
    this.rightFactor = composite.getRightFactorUuid();
    this.type = composite.getTypeDb();
    this.uuid = composite.getUuid();
    
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
   * convert to composite
   * @return the composite
   */
  public Composite toComposite() {
    Composite composite = new Composite();
    
    composite.setContextId(this.contextId);
    composite.setCreateTime(GrouperUtil.defaultIfNull(GrouperUtil.dateLongValue(this.createTime), 0L));
    composite.setCreatorUuid(this.creatorId);
    composite.setHibernateVersionNumber(this.hibernateVersionNumber);
    composite.setLeftFactorUuid(this.leftFactor);
    composite.setFactorOwnerUuid(this.owner);
    composite.setRightFactorUuid(this.rightFactor);
    composite.setTypeDb(this.type);
    composite.setUuid(this.uuid);
    
    return composite;
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
  public static void exportComposites(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        Query query = session.createQuery(
            "select theComposite from Composite as theComposite order by theComposite.factorOwnerUuid, theComposite.leftFactorUuid, theComposite.rightFactorUuid, theComposite.typeDb");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <composites>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final Composite composite = (Composite)object;

              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments()) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!-- ownerGroup: ");
                      writer.write(composite.getOwnerGroup().getName());
                      writer.write(", leftGroup: ");
                      writer.write(composite.getLeftGroup().getName());
                      writer.write(", rightGroup: ");
                      writer.write(composite.getRightGroup().getName());
                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }
              XmlExportComposite xmlExportComposite = new XmlExportComposite(grouperVersion, composite);
              writer.write("    ");
              xmlExportComposite.toXml(grouperVersion, writer);
              writer.write("\n");
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          //end the members element 
          writer.write("  </composites>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming composites", ioe);
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
  public static XmlExportComposite fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportComposite xmlExportGroupType = (XmlExportComposite)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportGroupType;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportComposite fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportComposite xmlExportGroupType = (XmlExportComposite)xStream.fromXML(xml);
  
    return xmlExportGroupType;
  }

}
