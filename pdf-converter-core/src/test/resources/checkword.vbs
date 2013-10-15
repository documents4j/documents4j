' Initialize variables
Dim wordApplication
On Error Resume Next

' Try to get Word
Set wordApplication = GetObject(, "Word.Application")

' Quit with negative exit status when application was not found
If Err <> 0 then
  WScript.Quit -10
Else
  WScript.Quit 10
End If