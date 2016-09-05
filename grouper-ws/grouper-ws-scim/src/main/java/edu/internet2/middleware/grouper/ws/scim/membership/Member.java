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
public class Member extends KeyedResource implements Serializable{
  
  private static final long serialVersionUID = 8164207035185112067L;

  @ScimAttribute(required=true, description="any subject identifier")
  @ScimResourceIdReference
  @XmlElement
  String value;

  @ScimAttribute(description="The URI of the corresponding resource")
  @XmlElement(name = "$ref")
  String ref;

  @ScimAttribute(description="A human readable name, primarily used for display purposes.")
  @XmlElement
  String display;

}
