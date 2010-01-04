/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import java.io.StringWriter;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.misc.GrouperVersion;


/**
 * bean to hold xml for export / import
 */
public class XmlExportMember {

  /**
   * 
   */
  public XmlExportMember() {
    
  }
  
  /**
   * @param member
   * @param grouperVersion
   */
  public XmlExportMember(GrouperVersion grouperVersion, Member member) {
    
    if (member == null) {
      throw new RuntimeException();
    }
    
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    this.contextId = member.getContextId();
    this.hibernateVersionNumber = member.getHibernateVersionNumber();
    this.sourceId = member.getSubjectSourceId();
    this.subjectId = member.getSubjectId();
    this.subjectType = member.getSubjectTypeId();
    this.uuid = member.getUuid();
    
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

}
