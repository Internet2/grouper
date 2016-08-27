/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim;

import static edu.psu.swe.scim.spec.schema.Schema.Attribute.Mutability.READ_ONLY;
import static edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned.DEFAULT;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimExtensionType;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

/**
 * @author vsachdeva
 */
@XmlRootElement( name = "TierMetaExtension", namespace = "http://www.internet2.edu/schemas/i2-scim" )
@XmlAccessorType(XmlAccessType.NONE)
@Data
@ScimExtensionType(id = TierMetaExtension.SCHEMA_URN, description="Common Tier Meta Extension", name="TierMetaExtension")
public class TierMetaExtension implements ScimExtension {

  private static final long serialVersionUID = 1L;
  
  public static final String  SCHEMA_URN = "urn:tier:params:scim:schemas:extension:TierMetaExtension";
  
  @ScimAttribute(description="result code", returned=DEFAULT, mutability=READ_ONLY)
  @XmlElement
  private String resultCode;
  
  @ScimAttribute(description="number of milliseconds taken to generate the response", returned=DEFAULT, mutability=READ_ONLY)
  @XmlElement
  @Setter(value=AccessLevel.NONE)
  private Long responseDurationMillis = System.currentTimeMillis() - TierFilter.retrieveRequestStartMillis();
  
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
