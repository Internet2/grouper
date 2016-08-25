package edu.internet2.middleware.grouper.ws.scim.membership;

import javax.ws.rs.Path;

import edu.psu.swe.scim.spec.protocol.BaseResourceTypeResource;
import io.swagger.annotations.Api;


/**
 * @author vsachdeva
 *
 */

@Path("Memberships")
@Api("ResourceType")
public interface MembershipRestResource extends BaseResourceTypeResource<MembershipResource> {

}

