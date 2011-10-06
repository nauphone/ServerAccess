Overview
========

.. image:: https://github.com/w31rd0/ServerAccess/raw/master/screenshot-1-main.png

ServerAccess is a tool to provide easy and seamless access to multiple servers. Some feature highlights:

* Support for various protocols such as SSH2, HTTP, FTP, and RDP.
* Support for all major operating systems (Windows, Linux, Mac OS X).
* Accounts can be stored either in a file on disk or on a remote web server.
* Local port forwarding on demand or by predefined settings.
* Connections can be made through several intermediate hosts (automatically, using tunnels).
* Outstanding search feature.

.. image:: https://github.com/w31rd0/ServerAccess/raw/master/screenshot-2-search.png

.. Use cases
.. =========
..
.. Intranet web application behind two SSH hops
.. --------------------------------------------

Prerequisites
=============

* Requires JRE to run.
* Requires JDK and Apache Ant to build.

Build and install
=================

Windows
-------
Use the following command to build the installer:

    ``ant win32``

Run the resulting ``sa_install.exe`` (from ``nsis`` subdirectory) which will guide you through the rest of install process.

Mac OS X
--------
Use the following command to build the application bundle for 64-bit architecture:

    ``ant macosx-cocoa-x86_64``

Copy ``ServerAccess.app`` (from ``distr`` subdirectory) to your ``/Applications`` folder.

GNU Linux/Other
---------------
Use the following command to build .jar:

    ``ant jar``

Then launch it (you will probably need to download SWT binaries for your platform and add them to Java class path):

    ``java -jar distr/servacc.jar``


Configuration
=============

A typical data source for ServerAccess is an XML file with the following structure::

    --Accounts
      |
      +--Account
      |  |
      |  +--Param
      |  |
      |  …
      |  |
      |  +--Param
      |
      …
      |
      +--Group
      |  |
      |  +--Group
      |     |
      |     +--Group
      |        |
      |        +--Account
      |           |
      |           +--Param
      |           |
      |           …
      |           |
      |           +--Param
      …

* The data is stored inside ``Account`` nodes.
* Each account has it's own set of parameters as ``Param`` subnodes.
* Accounts may be combined into ``Group`` nodes.
* Groups themselves may contain other child groups.
* Any level of nesting is allowed.
* Accounts are allowed at any level of hierarchy.
* Accounts having optional ``id`` attribute may be referenced from other accounts via their ``through`` parameter (this is how multihop SSH tunnels may be set up).

Sample configuration file contents::

    <?xml version="1.0" encoding="utf-8" standalone="yes"?>
    <Accounts version="2">

        <Account type="ssh">
            <Param name="login" value="example0" />
            <Param name="password" value="example0" />
            <Param name="address" value="example.com:22" />
            <Param name="putty_options" value="-load utf" />
        </Account>

        <Group name="example.com">

            <Account type="ssh" id="1">
                <Param name="login" value="example1" />
                <Param name="password" value="example1" />
                <Param name="address" value="example.com:22" />
            </Account>

            <Group name="node 1">

                <Account type="ssh">
                    <Param name="login" value="example2" />
                    <Param name="password" value="example2" />
                    <Param name="address" value="node1.example.com:22" />
                    <Param name="through" value="1" />
                </Account>

                <Account type="http">
                    <Param name="login" value="example3" />
                    <Param name="password" value="example3" />
                    <Param name="url" value="http://node1.example.com" />
                    <Param name="through" value="1" />
                </Account>

            </Group>

        </Group>

    </Accounts>

On the first launch an empty configuration file named ``accounts.xml`` will be created at the following location:

* Windows: ``%APPDATA%\Server Access``
* Mac OS X: ``~/Library/Application Support/Server Access``
* GNU Linux: ``~/.serveraccess``

The location of ``accounts.xml`` may be customized, as well as other data sources added, in ``serveraccess.properties`` located at the same directory:

* Windows: ``%APPDATA%\Server Access\serveraccess.properties``
* Mac OS X: ``~/Library/Application Support/Server Access/serveraccess.properties``
* GNU Linux: ``~/.serveraccess/serveraccess.properties``

Sample ``serveraccess.properties`` contents::

    source=file:///Users/bob/Library/Application Support/Server Access/accounts.xml
    source1=https://example.com/1/accounts.xml
    source2=https://example.com/2/accounts.xml

