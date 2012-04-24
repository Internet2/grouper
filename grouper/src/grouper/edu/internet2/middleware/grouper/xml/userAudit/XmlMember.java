/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: XmlMember.java,v 1.1 2009-03-31 06:58:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml.userAudit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class XmlMember {

  /**
   * retrieve all members
   * @param session 
   * @return the map of members
   */
  public static Map<String, XmlMember> retrieveAllMembers(Session session) {
    // map of member uuid to XmlMember
    Map<String, XmlMember> allMembersInRegistryNotExported = new HashMap<String, XmlMember>();
    
    //get the members
    {
      Query query = session.createQuery("select theMember.uuid, theMember.subjectIdDb, " +
          "theMember.subjectSourceIdDb from Member as theMember");
      
      //this is an efficient low-memory way to iterate through a resultset
      ScrollableResults results = null;
      try {
        results = query.scroll();
        while(results.next()) {
          //Object object = results.get(0);
          //Member member = (Member)object;
          String uuid = results.getString(0);
          String subjectId = results.getString(1);
          String sourceId = results.getString(2);
          XmlMember xmlMember = new XmlMember(uuid, subjectId, sourceId);
          allMembersInRegistryNotExported.put(uuid, xmlMember);
          
        }
      } finally {
        HibUtils.closeQuietly(results);
      }
    }
    return allMembersInRegistryNotExported;
  }
  
  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(XmlMember.class);

  /**
   * 
   */
  public XmlMember() {
    super();
  }

  /**
   * @param uuid1
   * @param subjectId1
   * @param sourceId1
   */
  public XmlMember(String uuid1, String subjectId1, String sourceId1) {
    this.uuid = uuid1;
    this.subjectId = subjectId1;
    this.sourceId = sourceId1;
  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return obj == null ? false : StringUtils.equals(this.uuid,((XmlMember)obj).uuid);
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return this.uuid == null ? -1 : this.uuid.hashCode();
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
  }
  
  /**
   * see if the object has string fields with members in them (by memberUuid), and if so, export that member
   * @param allMembersInRegistryNotExported
   * @param object
   * @param xStream 
   * @param fileWriter 
   * @param compactWriter 
   */
  public static void exportMembers(Map<String, XmlMember> allMembersInRegistryNotExported, 
      Object object, XStream xStream, FileWriter fileWriter, CompactWriter compactWriter) {

    Set<String> fieldNames = GrouperUtil.stringFieldNames(object.getClass());

    //go through all the fields in the object
    for (String fieldName : fieldNames) {
      String value = (String)GrouperUtil.fieldValue(object, fieldName);

      //see if the value is a member id which has not been exported
      if (allMembersInRegistryNotExported.containsKey(value)) {
        
        //export it
        XmlMember xmlMember = allMembersInRegistryNotExported.get(value);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Exporting member uuid from " + object.getClass().getSimpleName() + "." + fieldName + ", subjectId: " + xmlMember.getSubjectId());
        }
        
        //write it to the xml
        xStream.marshal(xmlMember, compactWriter);
        compactWriter.flush();
        try {
          fileWriter.write("\n");
        } catch (IOException ioe) {
          throw new RuntimeException(ioe);
        }
        
        //take out of list (mark as exported)
        allMembersInRegistryNotExported.remove(value);
      }
    }
  }

  /** uuid of member */
  private String uuid;
  
  /** subjectId */
  private String subjectId;
  
  /** sourceId */
  private String sourceId;

  /**
   * 
   * @return uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * 
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
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
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * source id
   * @return source id
   */
  public String getSourceId() {
    return this.sourceId;
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
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "XmlMember: sourceId: " + this.sourceId + ", subjectId: " + this.subjectId + ", uuid: " + this.uuid;
  }

}
