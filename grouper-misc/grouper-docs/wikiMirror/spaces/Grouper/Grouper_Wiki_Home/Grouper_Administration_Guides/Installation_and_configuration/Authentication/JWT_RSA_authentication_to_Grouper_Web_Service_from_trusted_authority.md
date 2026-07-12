---
title: "JWT RSA authentication to Grouper Web Service from trusted authority"
space: Grouper
pageId: 28548057
version: 11
lastUpdated: 2026-07-01T05:45:27.150Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548057/JWT+RSA+authentication+to+Grouper+Web+Service+from+trusted+authority
---

> A trusted authority can call the Grouper Web Service on behalf of an end user by presenting a JWT it has signed with its RSA private key. Grouper verifies the signature against the authority's configured public key and authenticates the request as the subject named in the token. Introduced in **v2.6.0** (2021); present in all currently supported releases.

 > **Required privileges.** Configuring trusted JWT authorities is a server-side / sysadmin task: the keys live in `grouper.properties` (edit on the server and restart) or are managed through the Grouper configuration editor, which requires Grouper sysadmin (wheel/root). Because a trusted authority can act as any subject it puts in the token, grant trust only to authorities you fully control, scope it with `subjectSourceIds`, and set an `expirationSeconds` rather than letting tokens never expire.

 

## Overview

 A source group has fluctuating memberships, and a trusted external authority needs to make Grouper Web Service calls as individual users without holding each user's credentials. Grouper is configured with the authority's RSA public key(s). The authority signs a JWT (with the subject's identifier in a claim) using its private key and sends it as a bearer token. Grouper verifies the signature, checks expiration, resolves the subject, and authenticates the web service call as that subject.

 

 

## Configure trusted authorities

 Trusted authority keys are configured in `grouper.properties` (or through the configuration editor UI). Each authority has a config id of your choosing; the keys below replace `<configId>` with that id and `<i>` with the key index, starting at `0`.

 

| Config key (`grouper.jwt.trusted.<configId>.…`) | Required | Description |
| --- | --- | --- |
| `.enabled` | no (default `true`) | Enable or disable this trusted authority. |
| `.numberOfKeys` | no (default `0`) | Number of public keys configured for this authority (1–10). |
| `.key.<i>.publicKey` | yes | RSA public key of the trusted authority. May be stored as an encrypted Grouper config value like other secrets. |
| `.key.<i>.encryptionType` | yes | Signature algorithm: `RS-256`, `RS-384`, or `RS-512`. |
| `.key.<i>.expiresOn` | no | Optional key expiry, format `yyyy-mm-dd hh:mm:ss.SSS`. |
| `.expirationSeconds` | no | Maximum JWT lifetime in seconds (e.g. `600` is 10 minutes). `-1` means never expire (not recommended). |
| `.subjectSourceIds` | no | Restrict resolved subjects to these subject source ids. |
| `.subjectIdType` | yes | How the claim maps to a subject: `subjectId`, `subjectIdentifier`, or `subjectIdOrIdentifier`. |
| `.subjectIdClaimName` | no | JWT claim that holds the subject id. Optional — the claim can instead be labeled `subjectId`, `subjectIdentifier`, or `subjectIdOrIdentifier` (e.g. `employeeId`). |

 Example configuration:

 
```text
grouper.jwt.trusted.configId.numberOfKeys = 1

# encrypted public key of trusted authority
grouper.jwt.trusted.configId.key.0.publicKey = abc123

# RS-256, RS-384, RS-512
grouper.jwt.trusted.configId.key.0.encryptionType =

# optional: yyyy-mm-dd hh:mm:ss.SSS
grouper.jwt.trusted.configId.key.0.expiresOn = 2021-11-01 00:00:00.000

# JWTs only last for so long.  e.g. 600 is 10 minutes.  -1 means never expire (not recommended)
grouper.jwt.trusted.configId.expirationSeconds = -1

# optional, could be in claim as "subjectSourceId", e.g. myPeople
grouper.jwt.trusted.configId.subjectSourceIds =

# subjectId, subjectIdentifier, or subjectIdOrIdentifier (optional)
grouper.jwt.trusted.configId.subjectIdType =

# some claim name that has the subjectId in it.  optional, can just label claim name as "subjectId", "subjectIdentifier", or "subjectIdOrIdentifier".  e.g. employeeId
grouper.jwt.trusted.configId.subjectIdClaimName = subjectId
```

 

## Make a web service call

 The trusted authority sends the signed JWT as a bearer token, prefixed with `jwtTrusted_` and the config id:

 
```text
Authorization: Bearer jwtTrusted_configId_abc123def456
```

 The bearer token format is `jwtTrusted_<configId>_<jwt>`. Grouper verifies that the JWT is signed by a private key matching that config id's public key, and the web service authenticates as the user named in the token.
