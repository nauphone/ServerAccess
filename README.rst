Description
===========

ServerAccess is a tool to provide easy and seamless access to multiple servers. It it aware of various protocols, such as SSH2, HTTP, FTP, and RDP. It uses accounts stored in either file system or on a remote web server. It can make local port forwarding on demand or by predefined settings. It can make connections through several tunnels.


Build and install
=================

Prerequisites: JRE, Apache Ant.

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
