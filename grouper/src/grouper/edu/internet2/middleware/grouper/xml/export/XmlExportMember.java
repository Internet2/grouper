/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
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

import edu.internet2.middleware.grouper.Member;
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
 * bean to hold xml for export / import
 */
public class XmlExportMember {

  /**
   * 
   */
  private static final String XML_EXPORT_MEMBER_XPATH = "/grouperExport/members/XmlExportMember";

  /**
   * 
   */
  private static final String MEMBERS_XPATH = "/grouperExport/members";

  /**
   * parse the xml file for members
   * @param xmlImportMain
   */
  public static void processXmlFirstPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( MEMBERS_XPATH, 
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

    xmlImportMain.getReader().addHandler( XML_EXPORT_MEMBER_XPATH, 
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
  
  /**
   * get db count
   * @param xmlExportMain 
   * @return db count
   */
  public static long dbCount(XmlExportMain xmlExportMain) {
    long result = HibernateSession.byHqlStatic().createQuery("select count(theMember) " 
        + exportFromOnQuery(xmlExportMain, false)).uniqueResult(Long.class);
    return result;
  }
  
  /**
   * get the query from the FROM clause on to the end for export
   * @param xmlExportMain
   * @param includeOrderBy 
   * @return the export query
   */
  private static String exportFromOnQuery(XmlExportMain xmlExportMain, boolean includeOrderBy) {
    //select all members in order
    StringBuilder queryBuilder = new StringBuilder();
    if (!xmlExportMain.filterStemsOrObjects()) {
      queryBuilder.append(" from Member as theMember ");
    } else {
      queryBuilder.append(
          " from Member as theMember where theMember.subjectSourceIdDb <> 'g:gsa'" +
          " or (theMember.subjectSourceIdDb = 'g:gsa' and exists " +
          " (select theGroup from Group as theGroup where theGroup.uuid = theMember.subjectIdDb and ( ");
      
      xmlExportMain.appendHqlStemLikeOrObjectEquals(queryBuilder, "theGroup", "nameDb", false);
      
      queryBuilder.append(" ) ) ) ");
      
    }
    if (includeOrderBy) {
      queryBuilder.append(" order by theMember.subjectSourceIdDb, theMember.subjectIdDb ");

    }
    return queryBuilder.toString();
  }
  
  /**
   * 
   * @param writer
   * @param xmlExportMain 
   */
  public static void exportMembers(final Writer writer, final XmlExportMain xmlExportMain) {
    //get the members
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {

        Session session = hibernateHandlerBean.getHibernateSession().getSession();

        
        Query query = session.createQuery("select distinct theMember " + exportFromOnQuery(xmlExportMain, true));

        GrouperVersion grouperVersion = new GrouperVersion(GrouperVersion.GROUPER_VERSION);
        try {
          writer.write("  <members>\n");

          //this is an efficient low-memory way to iterate through a resultset
          ScrollableResults results = null;
          try {
            results = query.scroll();
            while(results.next()) {
              Object object = results.get(0);
              final Member member = (Member)object;
              
              //comments to dereference the foreign keys
              if (xmlExportMain.isIncludeComments() && StringUtils.equals("g:gsa", member.getSubjectSourceId())) {
                HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
                  
                  public Object callback(HibernateHandlerBean hibernateHandlerBean)
                      throws GrouperDAOException {
                    try {
                      writer.write("\n    <!-- ");

                      XmlExportUtils.toStringGroup(null, writer, member.getSubjectId(), false);

                      writer.write(" -->\n");
                      return null;
                    } catch (IOException ioe) {
                      throw new RuntimeException(ioe);
                    }
                  }
                });
              }

              
              XmlExportMember xmlExportMember = member.xmlToExportMember(grouperVersion);
              writer.write("    ");
              xmlExportMember.toXml(grouperVersion, writer);
              writer.write("\n");
              xmlExportMain.incrementRecordCount();
            }
          } finally {
            HibUtils.closeQuietly(results);
          }
          
          if (xmlExportMain.isIncludeComments()) {
            writer.write("\n");
          }

          //end the members element 
          writer.write("  </members>\n");
        } catch (IOException ioe) {
          throw new RuntimeException("Problem with streaming members", ioe);
        }
        return null;
      }
    });
  }
  
  /**
   * 
   */
  public XmlExportMember() {
    
  }
  
  /**
   * convert to member
   * @return the member
   */
  public Member toMember() {
    Member member = new Member();
    
    member.setContextId(this.contextId);
    member.setHibernateVersionNumber(this.hibernateVersionNumber);
    member.setSubjectId(this.subjectId);
    member.setSubjectSourceId(this.sourceId);
    member.setSubjectTypeId(this.subjectType);
    member.setUuid(this.uuid);
    member.setSortString0(this.sortString0);
    member.setSortString1(this.sortString1);
    member.setSortString2(this.sortString2);
    member.setSortString3(this.sortString3);
    member.setSortString4(this.sortString4);
    member.setSearchString0(this.searchString0);
    member.setSearchString1(this.searchString1);
    member.setSearchString2(this.searchString2);
    member.setSearchString3(this.searchString3);
    member.setSearchString4(this.searchString4);
    member.setName(this.name);
    member.setDescription(this.description);
    
    return member;
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
   * @param exportVersion
   * @param xml
   * @return the object from xml
   */
  public static XmlExportMember fromXml(
      @SuppressWarnings("unused") GrouperVersion exportVersion, String xml) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportMember xmlExportMember = (XmlExportMember)xStream.fromXML(xml);

    return xmlExportMember;
  }

  /**
   * take a reader (e.g. dom reader) and convert to an xml export member
   * @param exportVersion
   * @param hierarchicalStreamReader
   * @return the bean
   */
  public static XmlExportMember fromXml(@SuppressWarnings("unused") GrouperVersion exportVersion, 
      HierarchicalStreamReader hierarchicalStreamReader) {
    XStream xStream = XmlExportUtils.xstream();
    
    XmlExportMember xmlExportMember = (XmlExportMember)xStream.unmarshal(hierarchicalStreamReader);

    return xmlExportMember;
  }
  
  /** subjectType */
  private String subjectType;
  
  /** contextId */
  private String contextId;
  
  /** string that can be used to sort results */
  private String sortString0;
  
  /** string that can be used to sort results */
  private String sortString1;
  
  /** string that can be used to sort results */
  private String sortString2;
  
  /** string that can be used to sort results */
  private String sortString3;
  
  /** string that can be used to sort results */
  private String sortString4;
  
  /** string that can be used to filter results */
  private String searchString0;
  
  /** string that can be used to filter results */
  private String searchString1;
  
  /** string that can be used to filter results */
  private String searchString2;
  
  /** string that can be used to filter results */
  private String searchString3;
  
  /** string that can be used to filter results */
  private String searchString4;
  
  /** name of member -- helpful for unresolvable subjects */
  private String name;
  
  /** description of member -- helpful for unresolvable subjects */
  private String description;


  /**
   * hibernateVersionNumber
   */
  private long hibernateVersionNumber;
  
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
   * subjectType
   * @return subjectType
   */
  public String getSubjectType() {
    return this.subjectType;
  }

  /**
   * subjectType
   * @param subjectType1
   */
  public void setSubjectType(String subjectType1) {
    this.subjectType = subjectType1;
  }

  /** sourceId */
  private String sourceId;
  
  /** subjectId */
  private String subjectId;
  
  /** uuid of member */
  private String uuid;

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(XmlExportMember.class);

  /**
   * source id
   * @return source id
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * 
   * @return subject id
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * 
   * @return uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * 
   * @param sourceId1
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }

  /**
   * 
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * 
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }
  
  

  
  /**
   * @return the sortString0
   */
  public String getSortString0() {
    return sortString0;
  }

  
  /**
   * @param sortString0 the sortString0 to set
   */
  public void setSortString0(String sortString0) {
    this.sortString0 = sortString0;
  }

  
  /**
   * @return the sortString1
   */
  public String getSortString1() {
    return sortString1;
  }

  
  /**
   * @param sortString1 the sortString1 to set
   */
  public void setSortString1(String sortString1) {
    this.sortString1 = sortString1;
  }

  
  /**
   * @return the sortString2
   */
  public String getSortString2() {
    return sortString2;
  }

  
  /**
   * @param sortString2 the sortString2 to set
   */
  public void setSortString2(String sortString2) {
    this.sortString2 = sortString2;
  }

  
  /**
   * @return the sortString3
   */
  public String getSortString3() {
    return sortString3;
  }

  
  /**
   * @param sortString3 the sortString3 to set
   */
  public void setSortString3(String sortString3) {
    this.sortString3 = sortString3;
  }

  
  /**
   * @return the sortString4
   */
  public String getSortString4() {
    return sortString4;
  }

  
  /**
   * @param sortString4 the sortString4 to set
   */
  public void setSortString4(String sortString4) {
    this.sortString4 = sortString4;
  }

  
  /**
   * @return the searchString0
   */
  public String getSearchString0() {
    return searchString0;
  }

  
  /**
   * @param searchString0 the searchString0 to set
   */
  public void setSearchString0(String searchString0) {
    this.searchString0 = searchString0;
  }

  
  /**
   * @return the searchString1
   */
  public String getSearchString1() {
    return searchString1;
  }

  
  /**
   * @param searchString1 the searchString1 to set
   */
  public void setSearchString1(String searchString1) {
    this.searchString1 = searchString1;
  }

  
  /**
   * @return the searchString2
   */
  public String getSearchString2() {
    return searchString2;
  }

  
  /**
   * @param searchString2 the searchString2 to set
   */
  public void setSearchString2(String searchString2) {
    this.searchString2 = searchString2;
  }

  
  /**
   * @return the searchString3
   */
  public String getSearchString3() {
    return searchString3;
  }

  
  /**
   * @param searchString3 the searchString3 to set
   */
  public void setSearchString3(String searchString3) {
    this.searchString3 = searchString3;
  }

  
  /**
   * @return the searchString4
   */
  public String getSearchString4() {
    return searchString4;
  }

  
  /**
   * @param searchString4 the searchString4 to set
   */
  public void setSearchString4(String searchString4) {
    this.searchString4 = searchString4;
  }

  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  
  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  
  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * parse the xml file for members
   * @param xmlImportMain
   */
  public static void processXmlSecondPass(final XmlImportMain xmlImportMain) {
    xmlImportMain.getReader().addHandler( MEMBERS_XPATH, 
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
  
    xmlImportMain.getReader().addHandler( XML_EXPORT_MEMBER_XPATH, 
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

                XmlExportMember xmlExportMemberFromFile = (XmlExportMember)xmlImportMain.getXstream().unmarshal(new Dom4JReader(row));
                
                Member member = xmlExportMemberFromFile.toMember();
                
                XmlExportUtils.syncImportable(member, xmlImportMain);
                
                xmlImportMain.incrementCurrentCount();
              } catch (RuntimeException re) {
                LOG.error("Problem importing member: " + XmlExportUtils.toString(row), re);
                throw re;
              }
            }
        }
    );
  
  }

}
