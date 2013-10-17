Const WdDoNotSaveChanges = 0

' Try to get currently running instance of MS Word.
On Error Resume Next
Dim wordApplication
Set wordApplication = GetObject(, "Word.Application")
If Err <> 0 Then
  WScript.Quit 1
End If
On Error GoTo 0

' Try to shut down MS Word.
wordApplication.Quit WdDoNotSaveChanges

' If Word cannot be shut down, exit and signal the error.
WScript.Quit 1
