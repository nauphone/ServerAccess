# ServerAccess NSIS script

!include "MUI.nsh"
!include "include\JavaVersion.nsh"
!include "include\IEVersion.nsh"

!define JAVA_MAJOR_VERSION 1
!define JAVA_MINOR_VERSION 4

Name         "Server Access"
Caption      "Server Access"
BrandingText "© NAUMEN 2005-2013"
Icon         "gfx\naumen.ico"
OutFile      "..\win${Arch}\build\sa_install-${BuildVersion}_${Arch}.exe"

InstallDir   "$PROGRAMFILES\NAUMEN\ServerAccess"

!define MUI_ABORTWARNING
!define MUI_ICON                        "gfx\naumen.ico"
!define MUI_UNICON                      "gfx\naumen.ico"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_RIGHT
!define MUI_HEADERIMAGE_BITMAP          "gfx\header.bmp"
!define MUI_WELCOMEFINISHPAGE_BITMAP    "gfx\welcome.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP  "gfx\welcome.bmp"
!define MUI_COMPONENTSPAGE_NODESC

!insertmacro    MUI_PAGE_WELCOME
!insertmacro    MUI_PAGE_DIRECTORY
!insertmacro    MUI_PAGE_COMPONENTS
!insertmacro    MUI_PAGE_INSTFILES
!insertmacro    MUI_PAGE_FINISH

;Section -CheckJavaVersion
;    Call GetJavaVersion
;    Pop $0 ; major version
;    Pop $1 ; minor version
;    Pop $2 ; micro version
;    Pop $3 ; build/update version
;    StrCmp $0 "no" NotInstalled
;    CheckMajorVersion:
;        IntCmp $0 ${JAVA_MAJOR_VERSION} CheckMinorVersion IncorrectVersion CheckMinorVersion
;    CheckMinorVersion:
;        IntCmp $1 ${JAVA_MINOR_VERSION} Done IncorrectVersion Done
;    NotInstalled:
;        MessageBox MB_OK|MB_ICONEXCLAMATION "$(TEXT_IncorrectJavaVersion)"
;        Abort "$(TEXT_IncorrectJavaVersion)"
;    IncorrectVersion:
;        MessageBox MB_OK|MB_ICONEXCLAMATION "$(TEXT_IncorrectJavaVersion)"
;        Abort "$(TEXT_IncorrectJavaVersion)"
;    Done:
;SectionEnd

Section -Main
    ;SectionIn RO
    SetOutPath "$INSTDIR"
    File "..\win${Arch}\build\libs\${Arch}\ServerAccess-all.jar"
    File "..\resource\log4j.properties"
    File "..\resource\putty.exe"
    File "..\resource\rdesktop.exe"
    File "..\base\src\main\resources\prog.ico"
SectionEnd

Section -Registry
SectionEnd

Section -Shortcut
    CreateShortCut "$INSTDIR\Server Access.lnk" "javaw.exe" "-classpath $\"ServerAccess-all.jar;$INSTDIR$\" ru.naumen.servacc.ui.Main" "$INSTDIR\prog.ico" 0
SectionEnd

Section "$(TEXT_DesktopShortcut)"
    SetOutPath "$INSTDIR"
    CreateShortCut "$DESKTOP\Server Access.lnk" "javaw.exe" "-classpath $\"ServerAccess-all.jar;$INSTDIR$\" ru.naumen.servacc.ui.Main" "$INSTDIR\prog.ico" 0
SectionEnd

Section "$(TEXT_EnableBasicAuthorization)"
    WriteRegDWORD   HKCU "Software\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_HTTP_USERNAME_PASSWORD_DISABLE" "iexplore.exe" 0x00000000
    WriteRegDWORD   HKLM "Software\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_HTTP_USERNAME_PASSWORD_DISABLE" "iexplore.exe" 0x00000000
SectionEnd

Section "$(TEXT_InstallDefaultPuttyCodepage)"
    WriteRegStr     HKCU "Software\SimonTatham\PuTTY\Sessions\Default%20Settings" "LineCodePage" "UTF-8"
SectionEnd

Function .onInit

    ; StrCpy $InstanceName ${NAU_DEFAULT_INSTANCE_NAME}
    ; InstallOptions
    ;!insertmacro MUI_INSTALLOPTIONS_EXTRACT_AS "dialogs\ioInstanceName.ini" "ioInstanceName.ini"
    ;!insertmacro MUI_INSTALLOPTIONS_EXTRACT_AS "dialogs\ioInitialUser.ini"  "ioInitialUser.ini"
    ;!insertmacro MUI_INSTALLOPTIONS_EXTRACT_AS "dialogs\ioStartOptions.ini" "ioStartOptions.ini"

	; Splash screen
    InitPluginsDir
	File /oname=$PLUGINSDIR\splash.bmp "gfx\splash.bmp"
	splash::show 2000 $PLUGINSDIR\splash

    Call CheckJavaVersion

	Pop $0
FunctionEnd

Function CheckJavaVersion
    Call GetJavaVersion
    Pop $0 ; major version
    Pop $1 ; minor version
    Pop $2 ; micro version
    Pop $3 ; build/update version
    StrCmp $0 "no" IncorrectVersion
    CheckMajorVersion:
        IntCmp $0 ${JAVA_MAJOR_VERSION} CheckMinorVersion IncorrectVersion CheckMinorVersion
    CheckMinorVersion:
        IntCmp $1 ${JAVA_MINOR_VERSION} Done IncorrectVersion Done
    IncorrectVersion:
        ;MessageBox MB_OK|MB_ICONEXCLAMATION "$(TEXT_IncorrectJavaVersion)"
        ;Abort "$(TEXT_IncorrectJavaVersion)"
        MessageBox MB_YESNO|MB_ICONQUESTION "$(TEXT_IncorrectJavaVersion)" /SD IDYES IDYES Done
        Abort
    Done:
FunctionEnd

Function CheckIEVersion
    Call GetIEFullVersion
    Pop $0
FunctionEnd




!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "Russian"

LangString TEXT_DesktopShortcut             ${LANG_ENGLISH} "Desktop shortcut"
LangString TEXT_DesktopShortcut             ${LANG_RUSSIAN} "Ярлык на Рабочий стол"

LangString TEXT_IncorrectJavaVersion        ${LANG_ENGLISH} "You need version of Java not older than ${JAVA_MAJOR_VERSION}.${JAVA_MINOR_VERSION} to run this application.$\nProceed with installation?"
LangString TEXT_IncorrectJavaVersion        ${LANG_RUSSIAN} "Для запуска приложения требуется Java версии не ниже ${JAVA_MAJOR_VERSION}.${JAVA_MINOR_VERSION}.$\nПродолжить установку?"

;LangString TEXT_JavaNotInstalled            ${LANG_ENGLISH}     ""
;LangString TEXT_JavaNotInstalled            ${LANG_RUSSIAN}     ""

LangString TEXT_EnableBasicAuthorization    ${LANG_ENGLISH} "Enable basic authorization feature for IEv6+"
LangString TEXT_EnableBasicAuthorization    ${LANG_RUSSIAN} "Включить поддержку Basic Authorization для IEv6+"

LangString TEXT_InstallDefaultPuttyCodepage    ${LANG_ENGLISH} "Install default codepage for putty to UTF-8"
LangString TEXT_InstallDefaultPuttyCodepage    ${LANG_RUSSIAN} "Установить для putty кодировку UTF-8"
