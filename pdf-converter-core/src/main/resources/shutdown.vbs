' Get current Word instance
Dim wordApplication
Set wordApplication = GetObject(, "Word.Application")

' Shut down
wordApplication.Quit WdDoNotSaveChanges

' Exit
WScript.Quit 0