/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Result for finding a stem
 * 
 * @author mchyzer
 * 
 */
public class WsStem implements Comparable<WsStem> {

  /** extension is the right part of the name */
  private String extension;

  /** display extension is the right part of the display name */
  private String displayExtension;

  /**
   * no arg constructor
   */
  public WsStem() {
    // blank

  }

  /**
   * construct based on stem, assign all fields
   * 
   * @param stem is what to construct from
   */
  public WsStem(Stem stem) {
    this.setDescription(stem.getDescription());
    this.setDisplayName(stem.getDisplayName());
    this.setName(stem.getName());
    this.setUuid(stem.getUuid());
    this.setExtension(stem.getExtension());
    this.setDisplayExtension(stem.getDisplayExtension());
  }

  /**
   * construct based on stem, assign all fields
   * 
   * @param stemLookup is what to construct from
   */
  public WsStem(WsStemLookup stemLookup) {
    this.setName(stemLookup.getStemName());
    this.setUuid(stemLookup.getUuid());
    this.setExtension(GrouperUtil.extensionFromName(stemLookup.getStemName()));
  }

  /**
   * friendly description of this stem
   */
  private String description;

  /**
   * friendly extensions of stem and parent stems
   */
  private String displayName;

  /**
   * Full name of the stem (all extensions of parent stems, separated by
   * colons, and the extention of this stem
   */
  private String name;

  /**
   * universally unique identifier of this stem
   */
  private String uuid;

  /**
   * friendly description of this stem
   * 
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * friendly description of this stem
   * 
   * @param description1
   *            the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * friendly extensions of stem and parent stems
   * 
   * @return the displayName
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * friendly extensions of stem and parent stems
   * 
   * @param displayName1
   *            the displayName to set
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  /**
   * Full name of the stem (all extensions of parent stems, separated by
   * colons, and the extention of this stem
   * 
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Full name of the stem (all extensions of parent stems, separated by
   * colons, and the extention of this stem
   * 
   * @param name1
   *            the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * universally unique identifier of this stem
   * 
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * universally unique identifier of this stem
   * 
   * @param uuid1
   *            the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * @return the extension
   */
  public String getExtension() {
    return this.extension;
  }

  /**
   * @param extension1 the extension to set
   */
  public void setExtension(String extension1) {
    this.extension = extension1;
  }

  /**
   * @return the displayExtension
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }

  /**
   * @param displayExtension1 the displayExtension to set
   */
  public void setDisplayExtension(String displayExtension1) {
    this.displayExtension = displayExtension1;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsStem o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (this == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }
    return GrouperUtil.compare(this.getName(), o2.getName());
  }

}
