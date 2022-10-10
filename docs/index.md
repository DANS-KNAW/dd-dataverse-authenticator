dd-dataverse-authenticator
==========================

Service that authenticates Dataverse users using account credentials


SYNOPSIS
--------

    dd-dataverse-authenticator { server | check }

DESCRIPTION
-----------

Service that authenticates Dataverse users by their account credentials. It is basically an extension of the Dataverse API. This service is
to be configured to have direct read access to the Dataverse database. The interface is very simple: a `POST` request with basic authentication.
If the user is successfully authenticated a `204 No Content` response is returned, if the authentication failed `403 Forbidden` and
sending no credentials results in `401 Unauthorized` with the `WWW-Authenticate` header set to `Basic`.

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
```

INSTALLATION AND CONFIGURATION
------------------------------
Currently this project is built as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
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


RUNNING POSTGRESQL LOCALLY
--------------------------

Make sure you have Docker installed. After that, run the following command in the root of the project (for example, `~/git/dd-dataverse-authenticator`):

```shell
docker run \
    --rm -it \
    -e POSTGRES_USER=dvnuser \
    -e POSTGRES_PASSWORD=password \
    -e POSTGRES_DB=dvndb \
    -p 5432 \
    --network host \
    --mount "type=bind,src=$PWD/src/test/resources/test-etc/init.sql,dst=/docker-entrypoint-initdb.d/init.sql" \
    postgres:13.7
```

Then update your `etc/config.yml` to have the following `dataverseDatabase` configuration:

```yaml
dataverseDatabase:
   driverClass: org.postgresql.Driver
   user: dvnuser
   password: password
   url: jdbc:postgresql://localhost:5432/dvndb
   properties:
      charSet: UTF-8
   maxWaitForConnection: 1s
   validationQuery: "/* dd-dataverse-authenticator Health Check */ SELECT 1"
   validationQueryTimeout: 3s
   minSize: 8
   maxSize: 32
   checkConnectionWhileIdle: true
   checkConnectionOnConnect: true
   checkConnectionOnReturn: true
   checkConnectionOnBorrow: true
   evictionInterval: 10s
   minIdleTime: 1 minute
```