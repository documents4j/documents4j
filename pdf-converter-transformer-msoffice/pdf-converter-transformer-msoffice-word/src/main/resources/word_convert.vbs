' See http://msdn.microsoft.com/en-us/library/bb243311%28v=office.12%29.aspx
Const WdDoNotSaveChanges = 0
Const WdExportFormatPDF = 17
Const MagicFormatPDFA = 999

Dim arguments
Set arguments = WScript.Arguments

' Transforms a file using MS Word into the given format.
Function ConvertFile( inputFile, outputFile, formatEnumeration )

  Dim fileSystemObject
  Dim wordApplication
  Dim wordDocument
  Dim wordDocuments

  ' Get the running instance of MS Word. If Word is not running, exit the conversion.
  On Error Resume Next
  Set wordApplication = GetObject(, "Word.Application")
  If Err <> 0 Then
    WScript.Quit -6
  End If
  Set wordDocuments = wordApplication.Documents
  On Error GoTo 0

  ' Find the source file on the file system.
  Set fileSystemObject = CreateObject("Scripting.FileSystemObject")
  inputFile = fileSystemObject.GetAbsolutePathName(inputFile)

  ' Convert the source file only if it exists.
  If fileSystemObject.FileExists(inputFile) Then

    ' Attempt to open the source document.
    On Error Resume Next
    Set wordDocument = wordDocuments.Open(inputFile, false, true, false)
    If Err <> 0 Then
        WScript.Quit -2
    End If
    On Error GoTo 0

    ' Convert: See http://msdn2.microsoft.com/en-us/library/bb221597.aspx
    On Error Resume Next
    If formatEnumeration = MagicFormatPDFA Then
      wordDocument.ExportAsFixedFormat outputFile, _
                                       WdExportFormatPDF, _
                                       False, _
                                       , , , , , , , , , , _
                                       True
    Else
      wordDocument.SaveAs outputFile, formatEnumeration
    End If

    ' Close the source document.
    wordDocument.Close WdDoNotSaveChanges
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
Call ConvertFile( arguments.Unnamed.Item(0), arguments.Unnamed.Item(1), CInt(arguments.Unnamed.Item(2)) )
