' First, try to get a running instance.
On Error Resume Next
Dim wordApplication
Set wordApplication = GetObject(, "Word.Application")

' If MS Word is already running, the script is successful.
If Err = 0 Then
  WScript.Quit 3
End If
Err.clear

' Start MS Word.
Set wordApplication = CreateObject("Word.Application")
If Err <> 0 Then
  WScript.Quit -6
End If

' Disable execution of macros.
wordApplication.WordBasic.DisableAutoMacros

' Exit and signal success.
WScript.Quit 3
