dd-dataverse-authenticator
==========================

Service that authenticates Dataverse users using account credentials

Purpose
-------

Provide a way for a front-end service (such as [dd-sword2](https://dans-knaw.github.io/dans-datastation-architecture/#dd-sword2){:target=_blank}) to
authenticate users against Dataverse user accounts. This services is basically an extension of the Dataverse API.

Interfaces
----------

The interface is very simple: a `POST` request with either:

* basic authentication with the user's username and password, OR
* an `X-Dataverse-key with` the user's API token

If the user is successfully authenticated a `200 OK` response is returned with a message body containing the authenticated user
ID in a simple json doc:

```json
{
  "userId": "user001"
}
```

If the credentials were absent or incorrect `401 Unauthorized` is returned.

If both the basic auth and the API-token are sent a `400 Bad Request` is returned and the credentials are **not** verified.


Examples
--------

```
curl -v -u username:password -X POST http://localhost:20340/
curl -v -H 'X-Dataverse-key: 123e4567-e89b-12d3-a456-426614174000' \
     -X POST http://localhost:20340/ 
```

