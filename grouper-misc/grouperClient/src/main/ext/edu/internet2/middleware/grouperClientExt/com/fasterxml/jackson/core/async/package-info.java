/**
 * Package that contains abstractions needed to support optional
 * non-blocking decoding (parsing) functionality.
 * Although parsers are constructed normally via
 * {@link edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.JsonFactory}
 * (and are, in fact, sub-types of {@link edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.JsonParser}),
 * the way input is provided differs.
 *
 * @since 2.9
 */

package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.async;
