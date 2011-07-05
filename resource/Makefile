ifndef INSTALLPATH
  INSTALLPATH = /opt/naumen/server-access
endif
INSTALLDIR=$(INSTALLROOT)/$(INSTALLPATH)
ifeq ($(strip $(USER)),root)
	OWNMODE = -o root -g root
endif

build:
	@echo todo:build

install:
	install -d -m755 $(OWNMODE) $(INSTALLDIR)
	install -m644 $(OWNMODE) CHANGES.TXT $(INSTALLDIR)/
	install -m644 $(OWNMODE) MANIFEST.MF $(INSTALLDIR)/
	install -m644 $(OWNMODE) mindterm.jar $(INSTALLDIR)/
	install -m644 $(OWNMODE) prog.ico $(INSTALLDIR)/
	install -m644 $(OWNMODE) sa-source.xml.fc14 $(INSTALLDIR)/sa-source.xml
	install -m644 $(OWNMODE) sa.xml $(INSTALLDIR)/
	install -m644 $(OWNMODE) servacc.jar $(INSTALLDIR)/
	install -m755 $(OWNMODE) server-access $(INSTALLDIR)/
	install -d -m755 $(OWNMODE) $(INSTALLROOT)/usr/share/applications/
	install -m644 $(OWNMODE) naumen-server-access.desktop $(INSTALLROOT)/usr/share/applications/
	install -d -m755 $(OWNMODE) $(INSTALLROOT)/usr/share/pixmaps/
	install -m644 $(OWNMODE) naumen-server-access.png $(INSTALLROOT)/usr/share/pixmaps/naumen_server_access.png
