
' Configure error handling to jump to next line.
On Error Resume Next

' Try to get running MS PowerPoint instance.
Dim powerpointApplication
Set powerpointApplication = GetObject(, "PowerPoint.Application")

' Signal whether or not such an instance could not be found.
If Err <> 0 then
  ' WScript.Echo "PowerPoint not found"
  WScript.Quit -6
Else
  WScript.Quit 3
End If