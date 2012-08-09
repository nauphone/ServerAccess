Name: naumen-server-access
Group: Applications/Internet
Version: 0.9.9.4
%define release %( svnversion -c %( pwd ) | sed "s/.*://" )
Release: %{release}
Summary: Nauphone servers(linux) installations access tools
License: Naumen
Requires: eclipse-swt
Requires: putty
%define install_path /opt/naumen/server-access
%define curr_dir %( pwd )

%description
tools for getting servers info from db and connecting to selected servers by ssh
or others tools (use ssh port mapping to local ports)

#%build
#todo:make or something...

%install
cd %{curr_dir}
[ -n "$RPM_BUILD_ROOT" -a "$RPM_BUILD_ROOT" != / ] && rm -rf "$RPM_BUILD_ROOT"
make INSTALLROOT=$RPM_BUILD_ROOT INSTALLPATH=%{install_path} install

%clean
[ -n "$RPM_BUILD_ROOT" -a "$RPM_BUILD_ROOT" != / ] && rm -rf "$RPM_BUILD_ROOT"

%files
%defattr(-,root,root)
%attr(644,root,root) %{install_path}/CHANGES.TXT
%attr(644,root,root) %{install_path}/MANIFEST.MF
%attr(644,root,root) %{install_path}/mindterm.jar
%attr(644,root,root) %{install_path}/prog.ico
%attr(644,root,root) %config %{install_path}/sa-source.xml
%attr(644,root,root) %config %{install_path}/sa.xml
%attr(644,root,root) %{install_path}/servacc.jar
%attr(755,root,root) %{install_path}/server-access
%attr(644,root,root) /usr/share/applications/naumen-server-access.desktop
%attr(644,root,root) /usr/share/pixmaps/naumen_server_access.png
