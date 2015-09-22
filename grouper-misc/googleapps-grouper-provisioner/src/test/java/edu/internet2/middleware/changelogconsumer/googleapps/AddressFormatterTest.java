/*******************************************************************************
 * Copyright 2015 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.changelogconsumer.googleapps;

import edu.internet2.middleware.changelogconsumer.googleapps.utils.AddressFormatter;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class AddressFormatterTest {

    @Test
    public void testQualifyAddressSimple() {
        AddressFormatter addressFormatter = new AddressFormatter();
        addressFormatter
                .setGroupIdentifierExpression("crs-${groupPath}-test")
                .setDomain("test.edu");

        String expected = "crs-abc1-abc2-test@test.edu";
        String result = addressFormatter.qualifyGroupAddress("abc1:abc2");
        assertEquals(expected, result);
    }

    @Test
    public void testQualifyAddressComplex() {
        AddressFormatter addressFormatter = new AddressFormatter();
            addressFormatter
                    .setGroupIdentifierExpression("crs-${groupPath.replace(\"abc1:\", \"\")}-test")
                    .setDomain("test.edu");

        String expected = "crs-abc2-test@test.edu";
        String result = addressFormatter.qualifyGroupAddress("abc1:abc2");
        assertEquals(expected, result);
    }

}