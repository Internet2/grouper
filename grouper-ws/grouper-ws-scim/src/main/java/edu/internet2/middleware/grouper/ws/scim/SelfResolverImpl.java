/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim;

import java.security.Principal;

import javax.ejb.Stateless;

import edu.psu.swe.scim.server.exception.UnableToResolveIdException;
import edu.psu.swe.scim.server.provider.SelfIdResolver;

/**
 * @author vsachdeva
 *
 */
@Stateless
public class SelfResolverImpl implements SelfIdResolver {

	@Override
	public String resolveToInternalId(Principal principal) throws UnableToResolveIdException {
		return "";
	}

}
