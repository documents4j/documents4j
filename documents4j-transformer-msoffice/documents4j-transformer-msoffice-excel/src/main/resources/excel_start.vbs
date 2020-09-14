' First, try to get a running instance.
On Error Resume Next
Dim excelApplication
Set excelApplication = GetObject(, "Excel.Application")

' If MS Excel is already running, the script is successful.
If Err = 0 Then
  WScript.Quit 3
End If
Err.clear

' Start MS Excel.
Set excelApplication = CreateObject("Excel.Application")
excelApplication.DisplayAlerts = False
' Add a workbook to keep open, otherwise Excel is shut down implicitly.
excelApplication.Workbooks.Add
If Err <> 0 Then
  WScript.Quit -6
End If

' Disable execution of macros.
excelApplication.ExcelBasic.DisableAutoMacros

' Exit and signal success.
WScript.Quit 3
