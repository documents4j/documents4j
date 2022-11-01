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
		tell application "Microsoft Excel"
			set isRun to running
			if formatIdentifier = 999 then -- PDF
				set targetFileFormat to (PDF file format)
			else if formatIdentifier = 51 then -- xlsx
				set targetFileFormat to (Excel XML file format)
			else if formatIdentifier = 54 then -- xltx
				set targetFileFormat to (template file format)
			else if formatIdentifier = 43 then -- xls
				set targetFileFormat to (Excel98to2004 file format)
			else if formatIdentifier = 60 then -- ods
				set targetFileFormat to (XML spreadsheet file format)
			else if formatIdentifier = 6 then -- csv
				set targetFileFormat to (CSV Mac file format)
			else if formatIdentifier = 46 then -- xml
				set targetFileFormat to (XML spreadsheet file format)
			else if formatIdentifier = 42 then --txt
				set targetFileFormat to (text Mac file format)
			end if
			activate
			open inputFile
			tell active workbook
				alias outputFile -- This is necessary to any script for 'Microsoft Office 2016', this avoid errors with any "save ... " command
				set wkbk1 to active workbook
				save as active sheet filename outputFile file format targetFileFormat with overwrite
				close saving no
			end tell
		end tell
		return 2
	on error errMsg number errorNumber
		-- log ("ERROR: " & errMsg)
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