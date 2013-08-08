' Start word
Dim wordApplication
Set wordApplication = CreateObject("Word.Application")

' Disable execution of macros
wordApplication.WordBasic.DisableAutoMacros

' Free local resources and exit
Set wordApplication = Nothing
WScript.Quit 0