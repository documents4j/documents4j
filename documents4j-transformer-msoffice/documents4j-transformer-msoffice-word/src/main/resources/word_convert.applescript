on run argv
	set MagicFormatPDF to 999
	try
		-- get a posix file object to avoid grant access issue with 'Microsoft Office 2016',  not the same as (file documentPath) when using the 'open  ...' command
		set inputFile to (POSIX path of (item 1 of argv)) as POSIX file
		set outputFile to (POSIX path of (item 2 of argv)) as POSIX file
		set formatIdentifier to (item 3 of argv) as integer
		-- delete file if it exists, with overwrite doesn't work always
		if fileExists(outputFile) then
			tell application "Finder"
				delete file outputFile
			end tell
		end if
		tell application "Microsoft Word"
			set isRun to running
			if formatIdentifier = 999 then -- PDF
				set targetFileFormat to (format PDF)
			else if formatIdentifier = 17 then -- PDF
				set targetFileFormat to (format PDF)
			else if formatIdentifier = 12 then -- docx
				set targetFileFormat to (format document)
			else if formatIdentifier = 0 then -- doc
				set targetFileFormat to (format document97)
			else if formatIdentifier = 6 then -- rtf
				set targetFileFormat to (format rtf)
			else if formatIdentifier = 9 then -- mhtml
				set targetFileFormat to (format web archive)
			else if formatIdentifier = 10 then -- html
				set targetFileFormat to (format filtered HTML)
			else if formatIdentifier = 11 then -- xml
				set targetFileFormat to (format xml)
			else if formatIdentifier = 7 then --txt
				set targetFileFormat to (format text)
			end if
			activate
			open inputFile
			tell active document
				alias outputFile -- This is necessary to any script for 'Microsoft Office 2016', this avoid errors with any "save ... " command
				set wkbk1 to active document
				save as document file name outputFile file format targetFileFormat with overwrite
				close saving no
			end tell
		end tell
		return 2
	on error errMsg number errorNumber
		log ("ERROR: " & errMsg)
		-- tell application appName to quit
		return -6
	end try
end run

-- function for testing if a file exists or not
on fileExists(theFile) -- (String) as Boolean
	tell application "System Events"
		if exists file (theFile as text) then
			return true
		else
			return false
		end if
	end tell
end fileExists