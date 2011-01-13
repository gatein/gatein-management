
To build this project just run:
mvn clean install

NOTE: Until stax parsing is figured out, you will also need stax-builder which can be obtained from:
https://github.com/nscavell/stax-builder

Once that's done copy the ear from packaging/jbossas/ear to the JBoss Application Server deploy directory.

** pomdata **
pomdata currently represents the object model the management services use to 'manage' portal artifacts.  It may
make sense to define our own domain model, but who needs another domain model for the portal ?  :)

-- Restful Services --
Assuming the server is running @ http://localhost:8080

NOTE: These URL's are subject to change.  To obtain data from the server you should use the client API's.

Site Management Services
    http://localhost:8080/management/rest/pomdata/{portal-container-name}/sites/{ownerId}

    Examples:
    http://localhost:8080/management/rest/pomdata/portal/sites/classic

Page Management Services
    http://localhost:8080/management/rest/pomdata/{portal-container-name}/pages?ownerType={ownerType}&ownerId={ownerId}
    http://localhost:8080/management/rest/pomdata/{portal-container-name}/pages/{name}?ownerType={ownerType}&ownerId={ownerId}

    Note: ownerType is not required and will default to 'portal'.  If you want to specify a group or user page include
          the ownerType as such ownerType=group or ownerType=user as a URL parameter.

    Examples
    All portal pages:
        http://localhost:8080/management/rest/pomdata/portal/pages?ownerId=classic
    Portal homepage:
        http://localhost:8080/management/rest/pomdata/portal/pages/homepage?ownerId=classic
    Application Registry
        http://localhost:8080/management/rest/pomdata/portal/pages/registry?ownerType=group&ownerId=/platform/administrators

Navigation Management Services
    http://localhost:8080/management/rest/pomdata/{portal-container-name}/navigations?ownerType={ownerType}&ownerId={ownerId}
    http://localhost:8080/management/rest/pomdata/{portal-container-name}/navigations/{nav-uri}?ownerType={ownerType}&ownerId={ownerId}

    Note: ownerType is not required and will default to 'portal'.  If you want to specify a group or user page include
          the ownerType as such ownerType=group or ownerType=user as a URL parameter.

    Note: nav-uri is the uri of the navigation, so if you have a nav of 'home-1' under home the nav-uri would be home/home-1

    Examples
    All portal navigation:
        http://localhost:8080/management/rest/pomdata/portal/navigations?ownerId=classic
    Portal navigation homepage:
        http://localhost:8080/management/rest/pomdata/portal/navigations/home?ownerId=classic
    Application Registry navigation:
        http://localhost:8080/management/rest/pomdata/portal/navigations/administration/registry?ownerType=group&ownerId=/platform/administrators


-- Client API's --

PomDataClient is the client interface to 'manage' the portal artifacts of the portal.

To obtain an instance of the PomDataClient to communicate with a portal running @ http://localhost:8080/portal/
PomDataClient client = PomDataClient.Factory.create(InetAddress.getByName("localhost"), 8080, "portal");

To get the homepage
PageData page = client.getPage("portal", "classic", "homepage");

To get homepage navigation
NavigationNodeData navigation = client.getNavigationNode("portal", "classic", "home");