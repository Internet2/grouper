/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.util.ArrayList;
import java.util.List;


/**
 * metadata about a section in a config file
 */
public class ConfigSectionMetadata {

  /**
   * 
   */
  public ConfigSectionMetadata() {
  }

  /**
   * title of section
   */
  private String title;

  /**
   * title of section
   * @return
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * title of section
   * @param title1
   */
  public void setTitle(String title1) {
    this.title = title1;
  }

  /**
   * comment for section
   */
  private String comment;
  
  /**
   * items in this section
   */
  private List<ConfigItemMetadata> configItemMetadataList = new ArrayList<ConfigItemMetadata>();
  
  /**
   * comment for section
   * @return the comment
   */
  public String getComment() {
    return this.comment;
  }
  
  /**
   * comment for section
   * @param comment1 the comment to set
   */
  public void setComment(String comment1) {
    this.comment = comment1;
  }
  
  /**
   * items in this section
   * @return the configSectionMetadataList
   */
  public List<ConfigItemMetadata> getConfigItemMetadataList() {
    return this.configItemMetadataList;
  }
  
  /**
   * items in this section
   * @param configSectionMetadataList1 the configSectionMetadataList to set
   */
  public void setConfigItemMetadataList(List<ConfigItemMetadata> configSectionMetadataList1) {
    this.configItemMetadataList = configSectionMetadataList1;
  }
  
}
