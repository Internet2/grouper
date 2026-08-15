---
title: "Grouper Jamf provisioner developer notes"
space: GrIntDev
pageId: 107380739
version: 2
lastUpdated: 2026-08-05T18:52:11.082Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/107380739/Grouper+Jamf+provisioner+developer+notes
---

## Links

- (Log in) [https://upenn.jamfcloud.com](https://upenn.jamfcloud.com)
- (Classic API reference) [https://developer.jamf.com/jamf-pro/reference/classic-api](https://developer.jamf.com/jamf-pro/reference/classic-api)
- (Jamf Pro API reference) [https://developer.jamf.com/jamf-pro/reference/jamf-pro-api](https://developer.jamf.com/jamf-pro/reference/jamf-pro-api)
- (API roles and clients / client credentials) [https://developer.jamf.com/jamf-pro/docs/client-credentials](https://developer.jamf.com/jamf-pro/docs/client-credentials)

## External system

Uses a WsBearerToken external system. The Jamf Pro base URL `https://upenn.jamfcloud.com` is stored as the endpoint in the external system configuration. Authentication is OAuth 2.0 client credentials (Jamf "API roles and clients"): the provisioner POSTs the client id and client secret to the token endpoint and receives a short-lived bearer access token, which it caches and refreshes on expiry. Every request then carries an `Authorization: Bearer <token>` header.

The provisioner uses XML for both reads and writes. Read requests take the Classic API default XML response (no `Accept` override needed). Write requests (create account, update account group members) send an XML body with `Content-Type: application/xml` -- the Classic API write endpoints only accept XML, and reject a JSON body with 415 Unsupported Media Type. The Classic API can also render JSON on read (via `Accept: application/json`), but its JSON rendering is inconsistent (the same account came back differently from the userid vs username endpoints), so we stay on the canonical XML throughout. The one exception is the OAuth token endpoint, which is on the Jamf Pro API side and returns JSON.

The API client needs, at minimum, the privileges: Read Accounts / Read Account Groups, Create User, Update User (and Delete User only if account deletion is ever enabled), and Update Account Groups.

## APIs general

Jamf Pro exposes two APIs: the older Classic API under `/JSSResource/...` (XML) and the newer Jamf Pro API under `/api/...` (JSON, RSQL paging). Admin accounts and account groups live only in the Classic API, so this provisioner uses the Classic API accounts endpoints and speaks XML to them. The OAuth token endpoint is on the Jamf Pro API side (`/api/oauth/token`) and is the only JSON call.

Token TTL is short (about 5 minutes). The provisioner fetches a token, caches it, and re-fetches when it is near expiry.

Paging: the account and user list endpoints do NOT page -- the full list returns in a single call (accounts is a small set; inventory users is ~1500). The list returns only `id` and `name` per entry; full detail requires a per-object GET.

Membership: account groups have no atomic add/remove. The incremental `<user_additions>` / `<user_deletions>` syntax works only on inventory static user groups (`/JSSResource/usergroups`), NOT on account groups. Account group membership is therefore applied by PUT-ing the complete `<members>` list (replace mode, retrieve-modify-write).

Quirk: account-group write responses (POST/PUT to `/accounts/groupid/...`) come back wrapped in a `<user_group>` element even though the object itself is a `<group>`. The GET returns the proper `<group>`.

## Grouper provisioning design

Membership type is membershipObjects. In Jamf, privileges live on the account group (role); an account gains those privileges by being a member of the role. That maps directly onto Grouper:

- **Target group = Jamf account group (role): read only.** Jamf admins own the role and its privilege set. Grouper never creates, updates, deletes, or renames a role -- it only resolves the role by name and manages its membership. (A role carries hundreds of privilege strings; a partial PUT risks wiping them, and the name is the link key between the Grouper group and the Jamf role.)
- **Target entity = Jamf admin account,** matched and created by `name` = lowercased EPPN (`pennkey@example.com`). Grouper creates the account if it does not exist (access_level Group Access, a random password, enabled) so it can be added to a role. Grouper does not update or delete accounts.
- **Target membership = the account in a role's `<members>` list,** which Grouper fully owns (add on join, remove on leave). Removal removes the account from the role only; the account itself is left in place.

Safety: accounts and roles named in the ignore lists are never touched, and Grouper never modifies directory-linked accounts it did not create.

## OAuth token

POST /api/oauth/token

Client-credentials grant. The client id and secret go in the form body (NOT as a Basic auth header -- Jamf returns 401 invalid_client if they are sent as Basic). Body is `application/x-www-form-urlencoded`.

Content-Type: application/x-www-form-urlencoded POST https://upenn.jamfcloud.com/api/oauth/token client_id=<client-id>&client_secret=<client-secret>&grant_type=client_credentials 200 { "access_token": "eyJhbGci...", "expires_in": 300, "token_type": "Bearer" } 401 { "error": "invalid_client" }

## List accounts and account groups

GET /JSSResource/accounts

Returns both individual admin accounts (`users`) and account groups / roles (`groups`), each as id + name only. No paging.

Authorization: Bearer <token> GET https://upenn.jamfcloud.com/JSSResource/accounts 200 <accounts> <users> <user><id>165</id><name>aci2189@example.com</name></user> <user><id>190</id><name>acorbitt@example.com</name></user> </users> <groups> <group> <id>34</id> <name>LSP Service Admins</name> <site><id>-1</id><name>NONE</name></site> </group> </groups> </accounts>

## Get account by username (find by EPPN)

GET /JSSResource/accounts/username/{name}

The account `name` is the EPPN, so this is the "find by email" lookup -- one call, no enumeration. Names appear lowercase; the provisioner lowercases the EPPN before matching. There is no email-search endpoint for accounts (that exists only for inventory users).

Authorization: Bearer <token> GET https://upenn.jamfcloud.com/JSSResource/accounts/username/aci2189@example.com 200 <account> <id>165</id> <name>aci2189@example.com</name> <directory_user>false</directory_user> <full_name>Andrew Ioli</full_name> <email>aci2189@example.com</email> <email_address>aci2189@example.com</email_address> <enabled>Enabled</enabled> <access_level>Site Access</access_level> <privilege_set>Administrator</privilege_set> <site><id>17</id><name>University of Pennsylvania - Nursing</name></site> <privileges> <jss_objects>...</jss_objects> <jss_settings>...</jss_settings> <jss_actions>...</jss_actions> </privileges> </account> 404 (no such account)

## Get account by id

GET /JSSResource/accounts/userid/{id}

Same object as the username lookup. Note that an account whose access_level is "Group Access" renders its privileges via a `groups` block (the roles it belongs to) rather than an inline `privileges` block.

## Create account

POST /JSSResource/accounts/userid/0

The trailing `0` means "new". Body is XML. `password` is required even though console login is via SSO -- the provisioner generates a random strong value. Returns `<account><id>` with the new id. Grouper creates accounts with access_level Group Access and no inline privileges (privileges come from role membership).

Authorization: Bearer <token> Content-Type: application/xml POST https://upenn.jamfcloud.com/JSSResource/accounts/userid/0 <account> <name>jdoe@example.com</name> <full_name>Jane Doe</full_name> <email>jdoe@example.com</email> <email_address>jdoe@example.com</email_address> <password>&lt;random&gt;</password> <access_level>Group Access</access_level> <enabled>Enabled</enabled> </account> 201 <account> <id>211</id> </account> 409 (name already exists -- POST to an existing account conflicts; use PUT to update) 415 (body was not sent as application/xml)

## Update account

PUT /JSSResource/accounts/userid/{id}

Partial update -- only the supplied fields change. When changing privilege fields, restate `access_level` together with `privilege_set` (and `site` for Site Access) as a consistent block, or Jamf may return 409 Conflict. Grouper does not update accounts today; documented for completeness.

Authorization: Bearer <token> Content-Type: application/xml PUT https://upenn.jamfcloud.com/JSSResource/accounts/userid/211 <account> <full_name>Jane Q Doe</full_name> </account> 201 <account> <id>211</id> </account>

## Delete account

DELETE /JSSResource/accounts/userid/{id}

Returns 200 on delete, 404 if already gone. Grouper does NOT delete accounts -- on removal from a Grouper group it only removes the account from the role's members.

## Get account group (role) with members

GET /JSSResource/accounts/groupid/{id}

Returns the role, its (possibly auto-expanded) privilege set, and its `members` list. The list is empty as `<members/>`. Members echo back with both id and name.

Authorization: Bearer <token> GET https://upenn.jamfcloud.com/JSSResource/accounts/groupid/34 200 <group> <id>34</id> <name>LSP Service Admins</name> <access_level>Full Access</access_level> <privilege_set>Custom</privilege_set> <site><id>-1</id><name>NONE</name></site> <privileges> <jss_objects>...</jss_objects> <jss_settings>...</jss_settings> <jss_actions/> </privileges> <members> <user><id>165</id><name>aci2189@example.com</name></user> </members> </group>

## Update account group membership (replace)

PUT /JSSResource/accounts/groupid/{id}

The PUT replaces the entire `<members>` list -- there is no per-member add or remove on account groups. To add or remove one member, the provisioner GETs the current members, edits the list, and PUTs the whole list back (retrieve-modify-write). Restate `name`/`access_level`/`privilege_set` alongside members to avoid a 409. An empty `<members/>` clears the role. The write response is wrapped in `<user_group>`.

Authorization: Bearer <token> Content-Type: application/xml PUT https://upenn.jamfcloud.com/JSSResource/accounts/groupid/34 <group> <name>LSP Service Admins</name> <access_level>Full Access</access_level> <privilege_set>Custom</privilege_set> <members> <user><name>aci2189@example.com</name></user> <user><name>jdoe@example.com</name></user> </members> </group> 201 <user_group> <id>34</id> </user_group>Not supported on account groups (returns 201 but does NOT apply): `<user_additions>` / `<user_deletions>`. Those work only on inventory static user groups.
