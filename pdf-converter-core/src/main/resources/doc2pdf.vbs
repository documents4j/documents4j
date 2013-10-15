Option Explicit

' See http://msdn2.microsoft.com/en-us/library/bb238158.aspx
Const wdFormatPDF = 17  ' PDF format.
Const wdFormatXPS = 18  ' XPS format.

Const WdDoNotSaveChanges = 0

Dim arguments
Set arguments = WScript.Arguments

' Make sure that there are one or two arguments
Function CheckUserArguments()
  If arguments.Unnamed.Count <> 2 Then
    WScript.Quit -4
  End If
End Function

' Transforms a doc to a pdf
Function DocToPdf( docInputFile, pdfOutputFile )

  Dim fileSystemObject
  Dim wordApplication
  Dim wordDocument
  Dim wordDocuments

  ' Get existing instance of word
  Set wordApplication = GetObject(, "Word.Application")
  Set wordDocuments = wordApplication.Documents

  ' Find Word file
  Set fileSystemObject = CreateObject("Scripting.FileSystemObject")
  docInputFile = fileSystemObject.GetAbsolutePathName(docInputFile)

  ' Convert the Word file if it exists
  If (fileSystemObject.FileExists(docInputFile)) Then

    ' Open word document
    On Error Resume Next
    Set wordDocument = wordDocuments.Open(docInputFile, false, true, false)

    ' If the file could not be opened,
    If Err <> 0 then
        WScript.Quit -2
    End If

    ' Convert: See http://msdn2.microsoft.com/en-us/library/bb221597.aspx
    wordDocument.SaveAs pdfOutputFile, wdFormatPDF

    ' Close word document
    wordDocument.Close WdDoNotSaveChanges

    ' Conversion was successful
    WScript.Quit 0

  Else

    ' Files does not exist, could not convert
    WScript.Quit -3

  End If

End Function

' Execute script
Call CheckUserArguments()
Call DocToPdf( arguments.Unnamed.Item(0), arguments.Unnamed.Item(1) )
