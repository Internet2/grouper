/**
 * @author mchyzer
 * $Id: XmlExportGroup.java 6216 2010-01-10 04:52:30Z mchyzer $
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.importXml.XmlImportMain;


/**
 *
 */
public class XmlExportRoleSet {

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

  /** ifHasRoleId */
  private String ifHasRoleId;
  
  /**
   * ifHasRoleId
   * @return ifHasRoleId
   */
  public String getIfHasRoleId() {
    return this.ifHasRoleId;
  }

  /**
   * ifHasRoleId
   * @param ifHasRoleId1
   */
  public void setIfHasRoleId(String ifHasRoleId1) {
    this.ifHasRoleId = ifHasRoleId1;
  }

  /**
   * thenHasRoleId
   */
  private String thenHasRoleId;
  
  /**
   * thenHasRoleId
   * @return thenHasRoleId
   */
  public String getThenHasRoleId() {
    return this.thenHasRoleId;
  }

  /**
   * thenHasRoleId
   * @param thenHasRoleId1
   */
  public void setThenHasRoleId(String thenHasRoleId1) {
    this.thenHasRoleId = thenHasRoleId1;
  }

  /**
   * parentRoleSetId
   */
  private String parentRoleSetId;
  
  /**
   * parentRoleSetId
   * @return parentRoleSetId
   */
  public String getParentRoleSetId() {
    return this.parentRoleSetId;
  }

  /**
   * parentRoleSetId
   * @param parentRoleSetId1
   */
  public void setParentRoleSetId(String parentRoleSetId1) {
    this.parentRoleSetId = parentRoleSetId1;
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
  public XmlExportRoleSet() {
    
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
   * convert to roleSet
   * @return the roleSet
   */
  public RoleSet toRoleSet() {
    RoleSet roleSet = new RoleSet();
    
    roleSet.setContextId(this.contextId);
    roleSet.setCreatedOnDb(GrouperUtil.dateLongValue(this.createTime));
    roleSet.setDepth((int)this.depth);
    roleSet.setHibernateVersionNumber(this.hibernateVersionNumber);
    roleSet.setIfHasRoleId(this.ifHasRoleId);
    roleSet.setLastUpdatedDb(GrouperUtil.dateLongValue(this.modifierTime));
    roleSet.setParentRoleSetId(this.parentRoleSetId);
    roleSet.setThenHasRoleId(this.thenHasRoleId);
    roleSet.setTypeDb(this.type);
    roleSet.setId(this.uuid);
    
    return roleSet;
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
   * get db count
   * @return db count
   */
  public static long dbCount() {
    long result = HibernateSession.byHqlStatic().createQuery("select count(*) from RoleSet as theRoleSet where theRoleSet.typeDb = 'immediate'").uniqueResult(Long.class);
    return result;
  }
  

  /**
   * 
   * @param writer
   * @param xmlExportMain 
   */
  public static void exportRoleSets(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all role sets (immediate is depth = 1)
        Query query = session.createQuery(
            "select theRoleSet from RoleSet as theRoleSet where theRoleSet.typeDb = 'immediate' order by theRoleSet.id");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <roleSets>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final RoleSet roleSet = (RoleSet)object;
              
              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments()) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!-- ");
                      XmlExportUtils.toStringRole("ifHas", writer, roleSet.getIfHasRoleId(), true);
                      XmlExportUtils.toStringRole("thenHas", writer, roleSet.getThenHasRoleId(), false);
                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }
              
              XmlExportRoleSet xmlExportRoleSet = roleSet.xmlToExportRoleSet(grouperVersion);
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
          
          //end the members element 
          writer.write("  </roleSets>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming roleSets", ioe);
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
  public static XmlExportRoleSet fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportRoleSet xmlExportRoleSet = (XmlExportRoleSet)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportRoleSet;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportRoleSet fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportRoleSet xmlExportRoleSet = (XmlExportRoleSet)xStream.fromXML(xml);
  
    return xmlExportRoleSet;
  }

  /**
   * parse the xml file for roleSets
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( "/grouperExport/roleSets", 
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

    xmlImportMain.getReader().addHandler( "/grouperExport/roleSets/XmlExportRoleSet", 
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
