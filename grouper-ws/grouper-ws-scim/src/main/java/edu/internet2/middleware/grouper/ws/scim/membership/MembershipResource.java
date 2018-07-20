/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim.membership;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.psu.swe.scim.spec.adapter.LocalDateTimeAdapter;
import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimResourceType;
import edu.psu.swe.scim.spec.resources.ScimResource;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author vsachdeva
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@ScimResourceType(id = MembershipResource.SCHEMA_URI, name = MembershipResource.RESOURCE_NAME, schema = MembershipResource.SCHEMA_URI, description = "Resource for representing Membership schema data", endpoint = "/Memberships")
@Data
@EqualsAndHashCode(callSuper=false)
public class MembershipResource extends ScimResource implements Serializable {
  
  private static final long serialVersionUID = -8216989589743750130L;
  public static final String SCHEMA_URI = "urn:tier:params:scim:schemas:Membership";
  public static final String RESOURCE_NAME = "Membership";
  
  
  @ScimAttribute(description="membership enabled time")
  @XmlElement
  @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
  private LocalDateTime enabledTime;
  
  @ScimAttribute(description="membership disabled time")
  @XmlElement
  @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
  private LocalDateTime disabledTime;
  
  @ScimAttribute(description="membership enabled?")
  @XmlElement
  private Boolean enabled;
  
  @ScimAttribute(canonicalValueList={"immediate, effective"}, description="Membership Type")
  @XmlElement
  private String membershipType;
  
  @XmlElement
  @ScimAttribute(required=true, description="Owner group of this membership")
  private OwnerGroup owner;
  
  @XmlElement
  @ScimAttribute(required=true, description="Member (Group or User) of this membership")
  private Member member;

  public MembershipResource() {
    super(SCHEMA_URI);
  }

  @Override
  public String getResourceType() {
    return RESOURCE_NAME;
  }
  
  

}
