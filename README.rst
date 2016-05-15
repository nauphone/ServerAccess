|image0|_

.. |image0| image:: https://travis-ci.org/nauphone/ServerAccess.png
.. _image0: https://travis-ci.org/nauphone/ServerAccess

Overview
========

.. image:: https://github.com/nauphone/ServerAccess/raw/master/screenshot-1-main.png

ServerAccess is a tool to provide easy and seamless access to multiple servers. Some feature highlights:

* Support for various protocols such as SSH2, HTTP, FTP, and RDP.
* Support for all major operating systems (Windows, Linux, Mac OS X).
* Accounts can be stored either in a file on disk or on a remote web server.
* Local port forwarding on demand or by predefined settings.
* Connections can be made through several intermediate hosts (automatically, using tunnels).
* Outstanding search feature.

.. image:: https://github.com/nauphone/ServerAccess/raw/master/screenshot-2-search.png

.. Use cases
.. =========
..
.. Intranet web application behind two SSH hops
.. --------------------------------------------

Prerequisites
=============

* Requires JRE 1.8 to run.
* Requires JDK 1.8 and Gradle to build.
* Requires makensis to build Windows executables.

Build and install
=================

Windows
-------
Use one of the following commands to build the installer (depending of the target architecture):

    ``gradlew.bat win32``

or:

    ``gradlew.bat win64``

Run the resulting ``build/sa_install-<version>.exe`` which will guide you through the rest of install process.

Mac OS X
--------
Use the following command to build the application bundle for 64-bit architecture:

    ``./gradlew macosx``

Copy ``ServerAccess.app`` (from ``build/libs`` subdirectory) to your ``/Applications`` folder.

GNU Linux/Other
---------------
Use ``./gradlew prepareRelease`` command to build application distribution.

Then launch it (you will probably need to download SWT binaries for your platform and add them to Java class path):

    ``./build/libs/server-access``

To create deb-package for Debian/Ubuntu distributive, use following script:

    ``./resource/debpack.sh``


Static analysis
===============

You may want to launch static analysis upon ServerAccess code. The best way to do it is to use Sonar Qube. Download and install it altogether with Sonar Scanner, and then run following command:

    ``sonar-scanner``

For the scanner-related questions, please refer to the `official documentation <http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner>`_.

Configuration
=============

At start application creates or opens main configuration file called ``serveraccess.properties``. It is located in the following directory:

* Windows: ``%APPDATA%\Server Access\serveraccess.properties``
* Mac OS X: ``~/Library/Application Support/Server Access/serveraccess.properties``
* GNU Linux: ``~/.serveraccess/serveraccess.properties``

Sample ``serveraccess.properties`` contents::

    source=file:///Users/bob/Library/Application Support/Server Access/accounts.xml
    source1=https://example.com/1/accounts.xml
    source2=https://example.com/2/accounts.xml
    terminal=guake  -n  1  {options}  -e  telnet {host} {port}

Accounts file
-------------

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

                <Account type="ssh" comment="SSH account accessed by passwordless RSA key">
                    <Param name="login" value="cryptoguru" />
                    <Param name="rsaKey" value="id_rsa_nopass"/>
                    <Param name="rsaPassword" value=""/>
                    <Param name="address" value="node2.example.com:22" />
                </Account>

            </Group>

        </Group>

    </Accounts>

On the first launch an empty configuration file named ``accounts.xml`` will be created near the ``serveraccess.properties`` file.

Key authentication
------------------

At the current moment, you could use passwordless (due to limitations of used backend) RSA or DSA keys for authentication instead of password. To do this, please use either `rsaKey` or `dsaKey` params (with optional *empty* `rsaPassword` or `dsaPassword` params respectively). Params `rsaPassword` and `dsaPassword` are reserved for the forward compatibility.

Keys must be stored in your default SSH key directory. It usually depends on your Operating System defaults.

Launchers
=========

ServerAccess allows you define what application must be used to open SSH connection, FTP connection or Web Browser. You can apply your prefferable options and use named templates as placeholders for stuff that is being determined and applied in runtime. Please refer for following sections for more information.

You *must* use double whitespace to separate program arguments. This allows you use single whitespace inside these arguments, when needed. If you have troubles with custom launcher, you may use ``DEBUG`` logging level to see, what is being launched, in log file. If it doesn't help, please contact project maintainers.

Terminal launcher
-----------------

By default, ServerAccess uses ``xterm`` on Linux, ``putty`` on Windows, and ``open`` on MacOs X. But you can redefine this by using ``terminal`` option in the ``serveraccess.properties`` file. For example, you may type something like this::

    terminal=guake  -n  1  -T  {name}  -e  telnet {host} {port}

Words placed in braces are called "placeholders". You must use them to determine places where runtime data is being put. Supported placeholders are:

* host - here ServerAccess inserts host to connect to. Usually it is equal to 127.0.0.1, but you'd better use template for further compatibility.
* port - here ServerAccess inserts port number. Port number is being generated dynamically, hence you cannot skip this template.
* name - optional. When it is provided, ServerAccess inserts remote host name that you can use to distinguish between different terminals.
* options - optional template that is used for backward compatibility with older versions

**Important**: please do not forget about double whitespace between launcher options!

FTP launcher
------------

Sadly, we have troubles in compatibility with different FTP clients. GFTP proved to be working, but other clients may not. An example::

    ftp=gftp  ftp://anonymous@{host}:{port}

Supported placeholders are the same as in "Terminal launcher" section.

**Important**: please do not forget about double whitespace between launcher options!

Web browser launcher
--------------------

An example (you chould use such a string in your ``serveraccess.properties`` file)::

    browser=chromium-browser  {url}

The main and the only supported placeholder is ``url``. It is used to insert link to the given location.

**Important**: please do not forget about double whitespace between launcher options!

Encryption
==========

Local configuration files can be encrypted with a password. In order to do that you need to pick ``File`` → ``Encrypt Local Accounts`` from the application menu in Mac OS X or ``Encrypt Local Accounts`` from notification area menu in other OS's.

.. image:: https://github.com/nauphone/ServerAccess/raw/master/screenshot-3-menu.png

A popup will then prompt you for a new password for each local configuration file to be encrypted.

.. image:: https://github.com/nauphone/ServerAccess/raw/master/screenshot-4-encrypt.png

After that you will be prompted to enter your password to decrypt local configuration files each the application is started. You can consider it a "master password" to protect your accounts. In case you need to make changes, for example when you need to add a new account, your configuration files can be decrypted using ``File`` → ``Decrypt Local Accounts`` command. This will result in your configuration files written to disk in unencrypted way so you can edit them.
