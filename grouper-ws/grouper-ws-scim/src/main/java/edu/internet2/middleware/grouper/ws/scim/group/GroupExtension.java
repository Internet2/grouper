/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim.group;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimExtensionType;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;
import lombok.Data;

@XmlRootElement( name = "GroupExtension", namespace = "http://www.internet2.edu/schemas/i2-scim" )
@XmlAccessorType(XmlAccessType.NONE)
@Data
@ScimExtensionType(id = GroupExtension.SCHEMA_URN, description="Group Extension", name="GroupExtension", required=true)
public class GroupExtension implements ScimExtension {
  
  private static final long serialVersionUID = 1L;
  
  public static final String  SCHEMA_URN = "urn:grouper:params:scim:schemas:extension:GroupExtension";
  
  @ScimAttribute(description="description of the group", returned=Returned.DEFAULT, required=true)
  @XmlElement
  private String description;
  
  @ScimAttribute(canonicalValueList={"group, role, entity"}, description="Type of group. default value is group", returned=Returned.DEFAULT, required=true)
  @XmlElement
  private String typeOfGroup;
  
  @ScimAttribute(description="should everyone has read privilege on this group?", returned=Returned.DEFAULT, required=true)
  @XmlElement
  private Boolean assignReadPrivToAll = false;
  
  @ScimAttribute(description="should everyone has view privilege on this group?", returned=Returned.DEFAULT, required=true)
  @XmlElement
  private Boolean assignViewPrivToAll = false;
  
  @ScimAttribute(description="should everyone has opt-in privilege on this group?", returned=Returned.DEFAULT, required=true)
  @XmlElement
  private Boolean assignOptInPrivToAll = false;
  
  @ScimAttribute(description="should everyone has opt out privilege on this group?", returned=Returned.DEFAULT, required=true)
  @XmlElement
  private Boolean assignOptOutPrivToAll = false;
  
  @ScimAttribute(description="should everyone has attribute read privilege on this group?", returned=Returned.DEFAULT, required=true)
  @XmlElement
  private Boolean assignAttributeReadPrivToAll = false;
  

  /**
   * Provides the URN associated with this extension which, as defined by the
   * SCIM specification is the extension's unique identifier.
   * 
   * @return The extension's URN.
   */
  @Override
  public String getUrn() {
    return SCHEMA_URN;
  }

}
