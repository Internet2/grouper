@Grab('org.apache.commons:commons-csv:1.2')
import org.apache.commons.csv.CSVParser
import static org.apache.commons.csv.CSVFormat.*

import java.nio.file.Paths

Paths.get('/Users/jj/Documents/workspace/microsoft/office365-and-azure-ad-grouper-provisioner/src/test/resources/users_6-3-2016 1-40-02 AM.csv').withReader { reader ->
    CSVParser csv = new CSVParser(reader, DEFAULT.withHeader())

    for (record in csv.iterator()) {
        println """dn: uid=${record.UserPrincipalName.split('@')[0]},ou=People,dc=example,dc=edu
objectClass: organizationalPerson
objectClass: person
objectClass: top
objectClass: inetOrgPerson
givenName: ${record.FirstName}
uid: ${record.UserPrincipalName.split('@')[0]}
sn: ${record.LastName}
cn: ${record.DisplayName}
userPassword: password
"""
    }
}