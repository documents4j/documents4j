' See http://msdn.microsoft.com/en-us/library/bb243311%28v=office.12%29.aspx
Const WdExportFormatPDF = 17
Const MagicFormatPDF = 999

Dim arguments
Set arguments = WScript.Arguments

' Transforms a file using MS Excel into the given format.
Function ConvertFile( inputFile, outputFile, formatEnumeration )

  Dim fileSystemObject
  Dim excelApplication
  Dim excelDocument

  ' Get the running instance of MS Excel. If Excel is not running, exit the conversion.
  On Error Resume Next
  Set excelApplication = GetObject(, "Excel.Application")
  If Err <> 0 Then
    WScript.Quit -6
  End If
  On Error GoTo 0

  ' Find the source file on the file system.
  Set fileSystemObject = CreateObject("Scripting.FileSystemObject")
  inputFile = fileSystemObject.GetAbsolutePathName(inputFile)

  
  ' Convert the source file only if it exists.
  If fileSystemObject.FileExists(inputFile) Then

    ' Attempt to open the source document.
    On Error Resume Next
    Set excelDocument = excelApplication.Workbooks.Open(inputFile, , True, , , , , , , , , , , , 2)
    If Err <> 0 Then
        WScript.Quit -2
    End If
    On Error GoTo 0

    ' Convert: See http://msdn2.microsoft.com/en-us/library/bb221597.aspx
    On Error Resume Next
    If formatEnumeration = MagicFormatPDF Then
      excelDocument.ExportAsFixedFormat xlTypePDF, outputFile
    Else
      excelDocument.SaveAs outputFile, formatEnumeration
    End If

    ' Close the source document.
    excelDocument.Close False
    If Err <> 0 Then
        WScript.Quit -3
    End If
    On Error GoTo 0

    ' Signal that the conversion was successful.
    WScript.Quit 2

  Else

    ' Files does not exist, could not convert
    WScript.Quit -4

  End If

End Function

' Execute the script.
Call ConvertFile( WScript.Arguments.Unnamed.Item(0), WScript.Arguments.Unnamed.Item(1), CInt(WScript.Arguments.Unnamed.Item(2)) )
