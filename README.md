GateIn Management
=============

A management framework to be used within [GateIn](http://www.gatein.org).  Main goal is to provide an extensible framework that plays nicely with the portal while allowing REST services and CLI commands to be automatically consumed.

Installation
-----------

`mvn clean install`

Restful API
-----------

Assuming GateIn is running @ http://localhost:8080 using the default portal container 'portal', the entry point of the REST API would be

    http://localhost:8080/rest/private/managed-components

From here every resource can be located by performing a GET request and following children links.

By default, a GET request to any managed resource will result in the operation 'read-resource'.  You can also specify the operation by adding a query parameter of 'op'.  For example

    http://localhost:8080/rest/private/managed-components?op=read-resource
is the same as

    http://localhost:8080/rest/private/managed-components

### Browser content negotiation
For convenience, browser content negotiation is supported by adding a 'format' query parameter to the URL.  This should only be used to customize content via a browser.  All REST clients should follow the standard of providing an Accept header in the request.  The following url:

    http://localhost:8080/rest/private/managed-components?format=xml
will return the result in XML format, instead of default JSON.

_Note_: Browser content defaults to JSON for ease of readability. (JSON addons/plugins for browsers are available).

### read-resource

Default operation for GET requests.  This request retrieves information about a managed resource, including children and operations.

Formats:

    JSON, XML

Http Methods:

    GET

Example GET Request @ http://localhost:8080/rest/private/managed-components:
    {

        description: "Lists registered managed components."
        children: [
            {
                name: "mop"
                link: {
                    rel: "child"
                    href: "http://localhost:8080/rest/private/managed-components/mop"
                }
            }
        ]
        operations: [
            {
                operation-name: "read-resource"
                operation-description: "Lists information about a managed resource, including available operations and children (sub-resources)."
                -
                link: {
                    rel: "self"
                    href: http://localhost:8080/rest/private/managed-components
                }
            }
        ]

    }

Command Line Interface (CLI)
-----------
The CLI is based on [CRaSH](http://code.google.com/p/crsh/) and is meant to run as a web application under GateIn.  It provides telnet and ssh access to perform management operations.  The default ports are 2000 for ssh and 5000 for telnet. To change the ports, edit the properties file

    cli/src/main/webapp/WEB-INF/crash/crash.properties

_Important_: gatein-management-cli.war must be added to GateIn as an exploded war file.  So to install this, copy the target/gatein-management-cli folder to

    $JBOSS_HOME/server/default/deploy/gatein-managmenet-cli.war

### SCP
An scp command is provided to be able to download/upload content to the management system.  To invoke the scp command run the following

    scp -P 2000 root@localhost:portal:/{address}
where root is the username, portal is the portal context, and address is the address/path of the managed resource.

For example to export the site classic from the mop managed component

    scp -P 2000 root@localhost:portal:/mop/portalsites/classic.zip

Developers
-----------
### SPI (extension)
The extension component is a means for registering a managed component for GateIn. The implementation uses the [Java 6 ServiceLoader](http://download.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html) to look up extensions. By adding a

    org.gatein.management.spi.ManagementExtension
file under

    /META-INF/services/
pointing to your implementation, the SPI framework will load your extension.