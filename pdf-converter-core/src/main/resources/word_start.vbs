' First, check if MS Word is already running.
On Error Resume Next
Dim wordApplication
Set wordApplication = GetObject(, "Word.Application")
If Err = 0 Then
  WScript.Quit 1
End If
On Error GoTo 0

' Start MS Word.
Set wordApplication = CreateObject("Word.Application")

' Disable execution of macros.
wordApplication.WordBasic.DisableAutoMacros

' Exit and signal success.
WScript.Quit 1
