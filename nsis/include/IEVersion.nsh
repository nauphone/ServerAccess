; GetIEFullVersion
; Version 1.0 (12.02.2003)
;
; This could replace GetIEVersion that return only major version
;
; Created by Sbarnea Sorin (INTERSOL) sorin@intersol.ro
; http://www.intersoldev.com (English) http://www.intersol.ro/ (Romanian)
;
; Feel free to improve but keep the comments
;
; Returns on top of stack
; Version is based on Shdocvw.dll version
; Details on Microsoft KB: Q164539
;
; 0.0.0.0 (IE is not installed)
;
; Usage:
;   Call GetIEVersion
;   Pop $R0
;   ; at this point $R0 is "4.72.3612.1707" for IE 4.01 SP2

Function GetIEFullVersion
  Push $R0
  Push $R1
  Push $R2
  Push $R3
  Push $R4
  Push $R5
 
  ClearErrors
  GetDLLVersion "$SYSDIR\shdocvw.dll" $R0 $R1
  IfErrors 0 +3 ; if not installed returns 0.0.0.0
  IntOp $R0 $R0 ^
  IntOp $R1 $R1 ^ 
  IntOp $R2 $R0 / 0x00010000
  IntOp $R3 $R0 & 0x0000FFFF
  IntOp $R4 $R1 / 0x00010000
  IntOp $R5 $R1 & 0x0000FFFF
  StrCpy $R0 "$R2.$R3.$R4.$R5"
  Pop $R5
  Pop $R4
  Pop $R3
  Pop $R2
  Pop $R1
  Exch $R0
FunctionEnd
