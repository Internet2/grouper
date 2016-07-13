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

@XmlRootElement( name = "TierGroupExtension", namespace = "http://www.internet2.edu/schemas/i2-scim" )
@XmlAccessorType(XmlAccessType.NONE)
@Data
@ScimExtensionType(id = TierGroupExtension.SCHEMA_URN, description="Tier Group Extension", name="TierGroupExtension")
public class TierGroupExtension implements ScimExtension {
  
  private static final long serialVersionUID = 1L;
  
  public static final String  SCHEMA_URN = "urn:grouper:params:scim:schemas:extension:TierGroupExtension";
  
  @ScimAttribute(description="description of the group", returned=Returned.DEFAULT)
  @XmlElement
  private String description;
  
  @ScimAttribute(description="idIndex of the group", returned=Returned.DEFAULT)
  @XmlElement
  private Long idIndex;
  
  @ScimAttribute(description="system name", returned=Returned.DEFAULT)
  @XmlElement
  private String systemName;
  
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
