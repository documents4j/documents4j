on run argv
	set MagicFormatPDF to 999
	set appName to "Microsoft Excel"
	try
		-- get a posix file object to avoid grant access issue with 'Microsoft Office 2016',  not the same as (file documentPath) when using the 'open  ...' command
		set tFile to (POSIX path of (item 1 of argv)) as POSIX file
		set PDFFile to (POSIX path of (item 2 of argv)) as POSIX file
		--tell application "Finder"
		if fileExists(PDFFile) then
			tell application "Finder"
			delete file PDFFile
			end tell
		end if
		--end tell
		tell application "Microsoft Excel"
			set isRun to running
			activate
			open tFile
			tell active workbook
				alias PDFFile -- This is necessary to any script for 'Microsoft Office 2016', this avoid errors with any "save ... " command
				set wkbk1 to active workbook
				save as active sheet filename PDFFile file format PDF file format with overwrite
				close saving no
			end tell
		end tell
		return 2
	on error errMsg number errorNumber
		log ("ERROR: ")
		log (errMsg)
		tell application appName to quit
		return -6
	end try
end run
on fileExists(theFile) -- (String) as Boolean
	tell application "System Events"
		if exists file (theFile as text) then
			return true
		else
			return false
		end if
	end tell
end fileExists