Configuration
=============

This module can be configured by editing the configuration file. This file is installed in `/etc/opt/dans.knaw.nl/dd-dataverse-authenticator/config.yml` when
using the RPM.
The settings are explained with comments in the file itself. An on-line version of the latest configuration file can be found
[here](https://github.com/DANS-KNAW/dd-dataverse-authenticator/blob/master/src/main/assembly/dist/cfg/config.yml){:target=_blank}.

This needs to have direct read access to the Dataverse database. It is recommended to use a dedicated PostGreSQL user for this with only `SELECT` permissions on
the tables: `builtinuser`, `apitoken` and `authenticateduser`.
