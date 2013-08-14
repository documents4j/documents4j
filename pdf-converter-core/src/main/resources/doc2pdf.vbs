' See http://msdn2.microsoft.com/en-us/library/bb238158.aspx
Const wdFormatPDF = 17  ' PDF format.
Const wdFormatXPS = 18  ' XPS format.

Const WdDoNotSaveChanges = 0

Dim arguments
Set arguments = WScript.Arguments

' Make sure that there are one or two arguments
Function CheckUserArguments()
  If arguments.Unnamed.Count < 1 Or arguments.Unnamed.Count > 2 Then
    WScript.Quit -1
  End If
End Function


' Transforms a doc to a pdf
Function DocToPdf( docInputFile, pdfOutputFile )

  Dim fileSystemObject
  Dim wordApplication
  Dim wordDocument
  Dim wordDocuments
  Dim baseFolder

  ' Get existing instance of word
  Set wordApplication = GetObject(, "Word.Application")
  Set wordDocuments = wordApplication.Documents

  ' Find files
  Set fileSystemObject = CreateObject("Scripting.FileSystemObject")
  docInputFile = fileSystemObject.GetAbsolutePathName(docInputFile)
  baseFolder = fileSystemObject.GetParentFolderName(docInputFile)

  ' Open word document
  Set wordDocument = wordDocuments.Open(docInputFile, false, true, false)

  ' Convert: See http://msdn2.microsoft.com/en-us/library/bb221597.aspx
  wordDocument.SaveAs pdfOutputFile, wdFormatPDF

  ' Close word document
  wordDocument.Close WdDoNotSaveChanges

  ' Free local resources
  Set fileSystemObject = Nothing
  Set wordApplication = Nothing
  Set wordDocument = Nothing
  Set wordDocuments = Nothing
  Set baseFolder = Nothing

End Function

' Execute script
Call CheckUserArguments()
If arguments.Unnamed.Count = 2 Then
 Call DocToPdf( arguments.Unnamed.Item(0), arguments.Unnamed.Item(1) )
Else
 Call DocToPdf( arguments.Unnamed.Item(0), "" )
End If

' Free local resources and quit
Set arguments = Nothing
WScript.Quit 0