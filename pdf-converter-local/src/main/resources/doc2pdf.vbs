' See http://msdn2.microsoft.com/en-us/library/bb238158.aspx
Const wdFormatPDF = 17  ' PDF format.
Const wdFormatXPS = 18  ' XPS format.

Const WdDoNotSaveChanges = 0

Dim arguments
Set arguments = WScript.Arguments

' Make sure that there are two arguments given.
Function CheckUserArguments()
  If arguments.Unnamed.Count <> 2 Then
    WScript.Quit -5
  End If
End Function

' Transforms a MS Word file into PDF.
Function DocToPdf( docInputFile, pdfOutputFile )

  Dim fileSystemObject
  Dim wordApplication
  Dim wordDocument
  Dim wordDocuments

  ' Get the running instance of MS Word. If Word is not running, exit the conversion.
  On Error Resume Next
  Set wordApplication = GetObject(, "Word.Application")
  If Err <> 0 then
    WScript.Quit -6
  End If
  Set wordDocuments = wordApplication.Documents
  On Error GoTo 0

  ' Find the Word file on the file system.
  Set fileSystemObject = CreateObject("Scripting.FileSystemObject")
  docInputFile = fileSystemObject.GetAbsolutePathName(docInputFile)

  ' Convert the Word file only if it exists.
  If (fileSystemObject.FileExists(docInputFile)) Then

    ' Open the MS Word document.
    On Error Resume Next
    Set wordDocument = wordDocuments.Open(docInputFile, false, true, false)
    If Err <> 0 then
        WScript.Quit -2
    End If
    On Error GoTo 0

    ' Convert: See http://msdn2.microsoft.com/en-us/library/bb221597.aspx
    On Error Resume Next
    wordDocument.SaveAs pdfOutputFile, wdFormatPDF
    If Err <> 0 then
        WScript.Quit -3
    End If
    On Error GoTo 0

    ' Close the MS Word document.
    wordDocument.Close WdDoNotSaveChanges

    ' Signal that the conversion was successful.
    WScript.Quit 2

  Else

    ' Files does not exist, could not convert
    WScript.Quit -4

  End If

End Function

' Execute script
Call CheckUserArguments()
Call DocToPdf( arguments.Unnamed.Item(0), arguments.Unnamed.Item(1) )
