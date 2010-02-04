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

import edu.internet2.middleware.grouper.GroupTypeTuple;
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
public class XmlExportGroupTypeTuple {

  /** uuid */
  private String uuid;
  
  /** hibernateVersionNumber */
  private long hibernateVersionNumber;

  /** contextId */
  private String contextId;

  /** group id */
  private String groupId;
  
  /** type id */
  private String typeId;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportGroup.class);
  
  
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
   * type id
   * @return type id
   */
  public String getTypeId() {
    return this.typeId;
  }

  /**
   * type id
   * @param typeId1
   */
  public void setTypeId(String typeId1) {
    this.typeId = typeId1;
  }

  /**
   * 
   */
  public XmlExportGroupTypeTuple() {
    
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
   * convert to group
   * @return the group
   */
  public GroupTypeTuple toGroupTypeTuple() {
    GroupTypeTuple groupTypeTuple = new GroupTypeTuple();
    
    groupTypeTuple.setContextId(this.contextId);
    groupTypeTuple.setGroupUuid(this.groupId);
    groupTypeTuple.setHibernateVersionNumber(this.hibernateVersionNumber);
    groupTypeTuple.setTypeUuid(this.typeId);
    groupTypeTuple.setId(this.uuid);
    
    return groupTypeTuple;
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
    xmlImportMain.getReader().addHandler( "/grouperExport/groupTypeTuples", 
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
  
    xmlImportMain.getReader().addHandler( "/grouperExport/groupTypeTuples/XmlExportGroupTypeTuple", 
        new ElementHandler() {
            public void onStart(ElementPath path) {
                // do nothing here...    
            }
            public void onEnd(ElementPath path) {
              try {
                // process a ROW element
                Element row = path.getCurrent();
  
                // prune the tree
                row.detach();
  
                XmlExportGroupTypeTuple xmlExportGroupTypeTupleFromFile = (XmlExportGroupTypeTuple)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                GroupTypeTuple groupTypeTuple = xmlExportGroupTypeTupleFromFile.toGroupTypeTuple();
                
                XmlExportUtils.syncImportable(groupTypeTuple, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing groupTypeTuples", re);
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
    long result = HibernateSession.byHqlStatic().createQuery("select count(*) from GroupTypeTuple").uniqueResult(Long.class);
    return result;
  }
  

  /**
   * 
   * @param writer
   * @param xmlExportMain settings
   */
  public static void exportGroupTypeTuples(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        Session session = hibernateHandlerBean.getHibernateSession().getSession();
  
        //select all members in order
        Query query = session.createQuery(
            "select theGroupTypeTuple from GroupTypeTuple as theGroupTypeTuple " +
            "order by theGroupTypeTuple.groupUuid, theGroupTypeTuple.typeUuid");
  
        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <groupTypeTuples>\n");
  
          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final GroupTypeTuple groupTypeTuple = (GroupTypeTuple)object;
              
              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments()) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!-- ");
                      XmlExportUtils.toStringGroup(null, writer, groupTypeTuple.getGroupUuid(), true);
                      XmlExportUtils.toStringType(writer, groupTypeTuple.getTypeUuid(), false);
                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }
              
              XmlExportGroupTypeTuple xmlExportGroupTypeTuple = groupTypeTuple.xmlToExportGroup(grouperVersion);
              writer.write("    ");
              xmlExportGroupTypeTuple.toXml(grouperVersion, writer);
              writer.write("\n");
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }
          
          //end the members element 
          writer.write("  </groupTypeTuples>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming group type tuples", ioe);
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
  public static XmlExportGroupTypeTuple fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportGroupTypeTuple xmlExportGroupTypeTuple = (XmlExportGroupTypeTuple)xStream.unmarshal(hierarchicalStreamReader);
  
    return xmlExportGroupTypeTuple;
  }

  /**
   * 
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportGroupTypeTuple fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportGroupTypeTuple xmlExportGroupTypeTuple = (XmlExportGroupTypeTuple)xStream.fromXML(xml);
  
    return xmlExportGroupTypeTuple;
  }

  /**
   * parse the xml file for group type tuples
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( "/grouperExport/groupTypeTuples", 
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

    xmlImportMain.getReader().addHandler( "/grouperExport/groupTypeTuples/XmlExportGroupTypeTuple", 
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
