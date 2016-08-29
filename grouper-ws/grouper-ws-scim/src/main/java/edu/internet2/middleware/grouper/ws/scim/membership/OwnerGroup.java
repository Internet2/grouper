/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim.membership;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimResourceIdReference;
import edu.psu.swe.scim.spec.resources.KeyedResource;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author vsachdeva
 *
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
@Data
@EqualsAndHashCode(callSuper=false)
public class OwnerGroup extends KeyedResource implements Serializable {
  
  private static final long serialVersionUID = 5710042052098130283L;

  @XmlElement
  @ScimAttribute(description="UUID of the group")
  @ScimResourceIdReference
  private String value;
  
  @XmlElement(name = "$ref")
  @ScimAttribute(description="The URI of the corresponding resource")
  private String ref;
  
  @XmlElement
  @ScimAttribute(description="A human readable name, primarily used for display purposes.")
  private String display;
  
  @XmlElement
  @ScimAttribute(description="system name of the group")
  private String systemName;

}
