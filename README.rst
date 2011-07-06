Description
===========
Overview
-----------
ServerAccess is a tool to provide easy and seamless access to multiple servers.
 * supporting of various protocols such as SSH2, HTTP, FTP, and RDP;
 * supporting all major operating systems (Windows, Linux, Mac OS X);
 * storing accounts in either file system or on a remote web server;
 * local port forwarding on demand or by predefined settings;
 * making connections through several tunnels.

.. Use cases
.. =========
.. 
.. Intranet web application behind two ssh hop's
.. ---------------------------------------------

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
