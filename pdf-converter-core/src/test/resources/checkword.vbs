' Initialize variables
Dim wordApplication
On Error Resume Next

' Try to get Word
Set wordApplication = GetObject(, "Word.Application")

' Quit with negative exit status when application was not found
If Err <> 0 then
  Set wordApplication = Nothing
  WScript.Quit -10
Else
  Set wordApplication = Nothing
  WScript.Quit 0
End If