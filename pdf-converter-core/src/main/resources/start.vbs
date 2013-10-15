' Start word
Dim wordApplication
Set wordApplication = CreateObject("Word.Application")

' Disable execution of macros
wordApplication.WordBasic.DisableAutoMacros

' Exit
WScript.Quit 0