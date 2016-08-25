/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim.membership;

import javax.inject.Inject;

import edu.psu.swe.scim.server.provider.Provider;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.server.rest.BaseResourceTypeResourceImpl;

/**
 * @author vsachdeva
 *
 */
public class MembershipRestResourceImpl extends BaseResourceTypeResourceImpl<MembershipResource> implements MembershipRestResource {
  
  @Inject
  ProviderRegistry providerRegistry;

  @Override
  public Provider<MembershipResource> getProvider() {
    return providerRegistry.getProvider(MembershipResource.class);
  }

}
