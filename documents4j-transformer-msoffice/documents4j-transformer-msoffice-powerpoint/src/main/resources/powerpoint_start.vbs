' First, try to get a running instance.
On Error Resume Next
Dim powerpointApplication
Set powerpointApplication = GetObject(, "PowerPoint.Application")
' Wscript.Echo "Got powerpoint "

' If MS PowerPoint is already running, the script is successful.
If Err = 0 Then
''  Wscript.Echo "Powerpoint is running"
  WScript.Quit 3
End If
Err.clear

' Start MS Powerpoint.
Set powerpointApplication = CreateObject("PowerPoint.Application")

'-- make powerpoint visible--'
powerpointApplication.Visible = True

If Err <> 0 Then
' Wscript.Echo "Powerpoint could not be created"
  WScript.Quit -6
End If

' Disable execution of macros.
powerpointApplication.WordBasic.DisableAutoMacros

' Exit and signal success.
' Wscript.Echo "Powerpoint is running now"
WScript.Quit 3