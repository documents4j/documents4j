' Get current Word instance
Dim wordApplication
Set wordApplication = GetObject(, "Word.Application")

' Shut down
wordApplication.Quit WdDoNotSaveChanges

' Free local resources and exit
Set wordApplication = Nothing
WScript.Quit 0