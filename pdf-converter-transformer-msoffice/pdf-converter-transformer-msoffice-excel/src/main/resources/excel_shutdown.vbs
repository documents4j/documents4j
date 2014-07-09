' Try to get currently running instance of MS Excel.
On Error Resume Next
Dim excelApplication
Set excelApplication = GetObject(, "Excel.Application")

' If no such instance can be found, MS Excel is already shut down.
If Err <> 0 Then
  WScript.Quit 3
End If

' Try to shut down MS Excel.
excelApplication.Workbooks.Close False
excelApplication.Quit

' If this was impossible, exit with an error.
If Err <> 0 Then
  WScript.Quit -6
End If
On Error GoTo 0

' MS Excel was shut down successfully.
WScript.Quit 3
