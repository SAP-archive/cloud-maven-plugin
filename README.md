SAP HANA Cloud Deployment Plugin for Maven
==========================================

What is it about?
-----------------

This is a plugin for Maven, which supports users in deploying web applications packaged in WAR files to SAP HANA Cloud, as well as starting/stopping instances of this web application there.


How is it done?
---------------

The Maven Plugin is a wrapped Ant build script (see `src/main/scripts/usage.build.xml`) which passes all needed parameters to "remote control" the [SAP HANA Cloud SDK](https://tools.hana.ondemand.com/) (Neo SDK) to execute the desired operations. This makes it easy for the user to stick on the console and just use Maven for building the web application as well as to deploy and run it on SAP HANA Cloud. All parameters concerning deployment target (e.g. host, account, user for login) are taken from a file `nwcloud.properties` which needs to be placed in the root folder of the Maven project. If also the SCN user's password is provided (can be done in an encrypted way), then all operations can be fully automated, whithout the need of the user having to type in his/her password. This is espacially valuable if you want to fully automate build and deployment processes of your web application.


Do you have a documented use case?
----------------------------------

Check out the [tutorial](http://sap.github.com/cloud-roo-addon/tutorial.html) of the SAP HANA Cloud Addon for Spring Roo to learn on how the plugin can be used to ease the deployment of web applications generated by Spring Roo. The usage is not restricted to Roo-generated web applications. The plugin can be used to deploy all web applications which are packaged as a WAR archive.


Building, installing and using the plugin
-----------------------------------------

In the following this document explains how to

1. build
2. install
3. use

the plugin.

If you don't want to build the addon by yourself, but just use it, then please refer to the tutorial mentioned above, where we also included a link to a download package with pre-compiled binaries and automated installer.


### Prerequisites ###

1. Building

	You need to have a recent version of [Apache Maven](http://maven.apache.org/) installed and available on the path, so that you can issue the command `mvn` on the commandline regardless of the directory you're currently in.

2. Using

	The addintional commands provided by the plugin are available after installation, but to be able to successfully execute these commands, the plugin relies on an installed version of the [SAP HANA Cloud SDK](https://tools.hana.ondemand.com/) (Neo SDK). Download and extract it to a directory of your choice.


### Building the plugin ###

Clone the plugin project from the git repository to a directory of your choice. To do a fresh build of the plugin, issue the following command on the commandline in the root of the project dir:

	mvn clean package


### Installing the plugin ###

After having built the plugin you just need to issue the following command to install it to your local Maven repository:

	mvn install

To make sure that the plugin is found when Maven searches for plugins, we now enable the group of our plugin as a group containing plugins.

Figure out where your currently used Maven configuration file `settings.xml` is located. Usually this is in the `conf` directory of your local Maven installation or in the `~/.m2` directory of your user (using Windows this would be something like `c:\Users\YourUser\.m2`).

In `settings.xml` search for the `pluginGroups` tag and insert `com.sap.research` as an artifact group that contains plugins, like this:

	<pluginGroups>
		<!--pluginGroup
		 | Specifies a further group identifier to use for plugin lookup.
		-->
		<pluginGroup>com.sap.research</pluginGroup>
	</pluginGroups>


### Using the plugin ###

The easiest way to use the plugin with your Maven managed web application is to include the plugin to the build plugins of the project. To do this, open the project file `pom.xml` and insert the following lines at the end of the build plugins section:

	<build>
		<plugins>
			...
            <plugin>
                <groupId>com.sap.research</groupId>
                <artifactId>nwcloud-maven-plugin</artifactId>
				<version>1.0.1.RELEASE</version>
				<executions>
					<execution>
						<id>after.package</id>
						<phase>package</phase>
						<goals>
							<goal>hint</goal>
						</goals>
					</execution>
				</executions>
            </plugin>
		</plugins>
	</build>

For actual deployment, the plugin needs some additional information (e.g. on which host to deploy, which account and user to use, or which Neo SDK to use). You provide such information to the plugin by placing a file called `nwcloud.properties` to the root directory of your Maven project. Please find a template for such a file, and further instructions on how to setup the basic settings at the end of this document in the appendix.

If you created a `nwcloud.properties` file and you now package your web application using `mvn package`, you will see that the plugin provides you with instructions on how to deploy your web application - similar to this:

	...
	[echo]
	[echo] WAR file created
	[echo] ================
	[echo]
	[echo] Your application has been packaged to a WAR file:
	[echo] c:/demo/target/voting-0.1.0.BUILD-SNAPSHOT.war
	[echo]
	[echo] What to do now?
	[echo] ---------------
	[echo]
	[echo] You could use the NWCloud-Maven-Plugin now to deploy your
	[echo] app to SAP HANA Cloud, or test it on a local instance.
	[echo]
	[echo] To use the NWCloud-Maven-Plugin, type
	[echo]
	[echo] mvn nwcloud:<goal>
	[echo]
	[echo] where <goal> is one of the following actions:
	[echo]
	[echo] deploy        ->  Deploy WAR as app to cloud
	[echo] start         ->  Start app instance on cloud
	[echo] stop          ->  Stop app instance on cloud
	[echo] undeploy      ->  Undeploy app from cloud
	[echo] status        ->  Show status of app on cloud
	[echo] apps          ->  Show apps deployed on cloud account
	[echo] comps         ->  Show components of deployed app on cloud
	[echo]
	[echo] deploy-local  ->  Deploy WAR to local cloud server
	[echo] start-local   ->  Start local cloud server
	[echo] stop-local    ->  Stop local cloud server
	[echo] clean-local   ->  Delete WAR from local cloud server
	[echo]
	[echo] usage         ->  Show usage info and currently used settings
	[echo]
	...

You can now perform the actual deployment as explained by the help text. We can use the Maven commands `nwcloud:deploy` for deployment to HANA Cloud and then use `nwcloud:start` to start up an instance in HANA Cloud to run our web application. And as Maven allows to write sequences of commands we can do this using one line:

	mvn nwcloud:deploy nwcloud:start

Depending on the speed of your network connection, the process of deploying and starting up the instance may take a few minutes. Finally, after the instance has successfully been started, you should see lines like the following ones:

	...
	[java] web: STARTED
	[java]
	[java] URL: https://votingmyaccount.hana.ondemand.com
	[java]
	[java] Access points:
	[java]   https://votingmyaccount.hana.ondemand.com
	...

After successful deployment and startup of the instance, we can now access our web application in the SAP HANA Cloud. To do this, we need to append a slash `/` and the name of the web application at the end of the URL that is shown on the console after the startup of the instance. As the web application in our example is called `voting` we use the following URL in our favorite web browser to access it:

	https://votingmyaccount.hana.ondemand.com/voting





Additional information
----------------------

### License ###

This project is copyrighted by [SAP AG](http://www.sap.com/) and made available under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html). Please also confer to the text files "LICENSE" and "NOTICE" included with the project sources.


### Contributions ###

Contributions to this project are very welcome, but can only be accepted if the contributions themselves are given to the project under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html). Contributions other than those given under Apache License 2.0 will be rejected.

Appendix
========

Template for nwcloud.properties file
------------------------------------

	# =============================================================================
	# SAP HANA Cloud - Deployment Configuration
	# =============================================================================

	# -----------------------------------------------------------------------------
	# Location of Neo SDK
	# -----------------------------------------------------------------------------
	#  - Download Neo SDK from: https://tools.hana.ondemand.com/
	#  - Extract Neo SDK to a directory and specify its path in sdk.dir setting.
	#     - Windows users: Please use double backslash instead of single backslash (e.g. c:\\Program Files\\Neo-SDK)
	#     - Linux/Mac users: Just use normal slash as usual (e.g. /home/myuser/bin/neo-sdk)
	sdk.dir=c:\\Program Files\\Neo-SDK

	# Proxy settings for Java Virtual Machine
	# - Proxy settings are tried to be autodetected from environment variables and Maven settings.
	# - If you are behind a proxy and autodetect does not work or you need to override the settings, this can be done here.
	#sdk.proxy=-Dhttp.proxyHost=proxy -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy -Dhttps.proxyPort=8080 -Dhttp.nonProxyHosts="localhost|127.0.0.1" -Dhttps.nonProxyHosts="localhost|127.0.0.1" -Dhttp.proxyUser=proxyuser -Dhttp.proxyPassword=proxypassword -Dhttps.proxyUser=proxyuser -Dhttps.proxyPassword=proxypassword

	# -----------------------------------------------------------------------------
	# Target for deployment (host, account, application)
	# -----------------------------------------------------------------------------
	#  - host: The target platform to deploy to (e.g. https://hana.ondemand.com)
	#  - account: The account to deploy to
	#  - application:
	#     - Name/ID of the application to deploy (non-empty, alphanumeric, lowercase letters, starting with a letter, max. 30 characters).
	#     - If not specified, the maven project name in lowercase letters will be used.
	host=https://hana.ondemand.com
	account=myaccount
	#application=myapp

	# -----------------------------------------------------------------------------
	# SCN user (and password)
	# -----------------------------------------------------------------------------
	#  - user: SCN user (as registered at http://scn.sap.com/)
	#  - password: Password of SCN user. If not specified here or (encrypted) in Maven settings, it will be queried on the commandline each time needed.
	user=myscnuser
	#password=myscnpassword

	# -----------------------------------------------------------------------------
	# Optional settings
	# -----------------------------------------------------------------------------

	# Request specific URL prefix for deployed application component (optional).
	#url=myappurl

	# Define specific component name for deployed application (optional, defaults to "web").
	#component=myappcomp

	# Request specific Java runtime version for execution of deployed components (optional, defaults to Java 6).
	#java-version=7

	# The logging level of the server process(es) (optional, defaults to "error").
	# Allowed values are: error|warn|info|debug
	#severity=debug

	# Minimum count of server processes, on which application can be started (optional, defaults to 1).
	#minimum-processes=1

	# Maximum count of server processes, on which application can be started (optional, defaults to 1).
	#maximum-processes=1

	# Specifies whether to start/stop an application synchronously (optinal, defaults to false).
	# Allowed values are: true|false
	synchronous=true

	# -----------------------------------------------------------------------------
	# Manually define WAR file to deploy
	# -----------------------------------------------------------------------------
	# The WAR file to be deployed is autodetected from maven project settings, but can also be set manually.

	# Option 1: Specify name of WAR
	# - Define name of WAR file in "target" directory of project (without ".war").
	#war.name=myapp

	# Option 2: Specify full path and name of WAR (including ".war")
	#source=c:\\Demo\\MyAppProjectFolder\\target\\myapp.war


Setup the basics for deployment
-------------------------------

It is important that you adjust the file `nwcloud.properties` according to your needs, before you can actually do a real deployment. You need to define:
- your SCN user (to be able to access SAP HANA Cloud),
- the desired target of deployment, and
- the location of the SAP HANA Cloud SDK.

> **SCN user and free SAP HANA Cloud account**
>
> If you do not have access to SAP HANA Cloud yet, please register a user at [SAP Community Network (SCN)](http://scn.sap.com/) and follow the steps explained [here](http://scn.sap.com/docs/DOC-28197) to get a free developer account.

Now use the text editor of your choice to open `nwcloud.properties`, and set your SCN user (password is optional) in the following lines:

	# -----------------------------------------------------------------------------
	# SCN user (and password)
	# -----------------------------------------------------------------------------
	#  - user: SCN user (as registered at http://scn.sap.com/)
	#  - password: Password of SCN user. If not specified here or (encrypted) in
	#              Maven settings, it will be queried on the commandline each time needed.
	user=myscnuser
	#password=myscnpassword

Define where to deploy the web application by adjusting the following lines:

	# -----------------------------------------------------------------------------
	# Target for deployment (host, account, application)
	# -----------------------------------------------------------------------------
	#  - host: The target platform to deploy to (e.g. https://hana.ondemand.com)
	#  - account: The account to deploy to
	#  - application:
	#     - Name/ID of the application to deploy (non-empty, alphanumeric, lowercase
	#       letters, starting with a letter, max. 30 characters).
	#     - If not specified, the maven project name in lowercase letters will be used.
	host=https://hana.ondemand.com
	account=myaccount
	#application=myapp

> **Hint**: If you are using the free SAP HANA Cloud developer account then the host is `https://hanatrial.ondemand.com`

Finally make sure that the following line is pointing to the directory where you have installed the [SAP HANA Cloud SDK](https://tools.hana.ondemand.com/) (Neo SDK).

	# -----------------------------------------------------------------------------
	# Location of Neo SDK
	# -----------------------------------------------------------------------------
	#  - Download Neo SDK from: https://tools.hana.ondemand.com/
	#  - Extract Neo SDK to a directory and specify its path in sdk.dir setting.
	#     - Windows users: Please use double backslash instead of single backslash (e.g. c:\\Program Files\\Neo-SDK)
	#     - Linux/Mac users: Just use normal slash as usual (e.g. /home/myuser/bin/neo-sdk)
	sdk.dir=c:\\Program Files\\Neo-SDK


Using encrypted passwords
-------------------------

Instead of storing the password of your SCN user as plain text in `nwcloud.properties` or to type the password to the console each time it is needed, a new feature to use encrypted password storage in Maven’s configuration file was added in version 1.0.1.RELEASE of the plugin.

As a first step to use this feature, add a server with your encrypted SCN user password to Maven’s configuration file `settings.xml` as described here:

[http://maven.apache.org/guides/mini/guide-encryption.html](http://maven.apache.org/guides/mini/guide-encryption.html)

This server will represent a SAP HANA Cloud landscape we want to deploy to. To be able to identify the correct server definition (and according SCN user's password) later on, we further describe the server using a `configuration` tag, with one, two or three of the following sub tags:
- `host`
- `account`
- `user`

A server definition could look like this:

	<servers>
		[...]
		<server>
			<id>nwcloud-01</id>
			<password>{COQLCE6DU6GtcS5P=}</password>
			<configuration>
				<host>https://hanatrial.ondemand.com</host>
				<account>myaccount</account>
				<user>myuser</user>
			</configuration>
		</server>
		[...]
	</servers>

Now, whenever the nwcloud-maven-plugin needs a password, it will try to get the password in the following order:

1. Read clear text password from `nwcloud.properties`.
2. If not found in step 1, try to read password from a matching server in Maven’s `settings.xml`.
3. If not found in step 1 and 2, query user for password on console.

To find the best matching server in Maven’s `settings.xml`, the plugin will go through the list of all defined servers that have a password and a configuration defined. For each of these servers the plugin will compare the attributes `host`, `account`, and `user` to the ones defined in `nwcloud.properties`. The server with the most matching attributes "wins" and its (decrypted) password will be used as your SCN user password when performing operations.

> **Hint**: This allows you to e.g. define a server that has just the attribute `user` defined, and thus will match all requests for passwords for this user as long as there is no more specific server (with more matching attributes) defined.

For testing purposes you can use the following command to see which server is matched by the plugin for certain values of `host`, `account`, or `user` (at least one parameter needs to be defined):

	mvn nwcloud:getpwd -Dhost=abc -Daccount=def -Duser=ghi

Please be aware that in this case of manual testing (and ONLY in this case) the matched server as well as its decrypted password will be printed to the console in clear text.


Version history
---------------

**1.0.1.RELEASE**
- Added support for parameter `java-version` in `nwcloud.properties`.
- Added support for encrypted password storage in Maven’s `settings.xml` (see section "Using encrypted passwords" in this document for details).
- Improved autodetection of proxy settings by taking Maven settings into account.

**1.0.0.RELEASE**

- Initial release
