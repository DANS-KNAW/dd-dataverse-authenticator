dd-dataverse-authenticator
==========================

Service that authenticates Dataverse users using account credentials


SYNOPSIS
--------

    dd-dataverse-authenticator { server | check }

DESCRIPTION
-----------

Service that authenticates Dataverse users by their account credentials. It is basically an extension of the Dataverse API. This
service is to be configured to have direct read access to the Dataverse database. It is recommended to use a dedicated PostGreSQL
user for this with only `SELECT` permissions on the tables: `builtinuser`, `apitoken` and `authenticateduser`.

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

ARGUMENTS
---------

        positional arguments:
        {server,check}         available commands
        
        named arguments:
        -h, --help             show this help message and exit
        -v, --version          show the application version and exit

EXAMPLES
--------

```
curl -v -u username:password -X POST http://localhost:20340/
curl -v -H 'X-Dataverse-key: 0571dee0-5e48-4439-aeef-94fd3dc380d1' -X POST http://localhost:20340/
```

INSTALLATION AND CONFIGURATION
------------------------------
Currently, this project is built as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/dd-dataverse-authenticator` and the configuration files to `/etc/opt/dans.knaw.nl/dd-dataverse-authenticator`.

For installation on systems that do no support RPM and/or systemd:

1. Build the tarball (see next section).
2. Extract it to some location on your system, for example `/opt/dans.knaw.nl/dd-dataverse-authenticator`.
3. Start the service with the following command
   ```
   /opt/dans.knaw.nl/dd-dataverse-authenticator/bin/dd-dataverse-authenticator server /opt/dans.knaw.nl/dd-dataverse-authenticator/cfg/config.yml 
   ```

BUILDING FROM SOURCE
--------------------
Prerequisites:

* Java 11 or higher
* Maven 3.3.3 or higher
* RPM

Steps:

    git clone https://github.com/DANS-KNAW/dd-dataverse-authenticator.git
    cd dd-dataverse-authenticator 
    mvn clean install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM
packaging will be activated. If `rpm` is available, but at a different path, then activate it by using
Maven's `-P` switch: `mvn -Pprm install`.

Alternatively, to build the tarball execute:

    mvn clean install assembly:single
