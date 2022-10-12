# See http://msdn.microsoft.com/en-us/library/bb243311%28v=office.12%29.aspx
set WdExportFormatPDF to 17
set MagicFormatPDF to 999
set appName to "Microsoft Excel"

on fun argv
	try 
		tell application appName
			open (quoted form of (item 1 of argv))
			
			if formatEnumeration = MagicFormatPDF then
				save workbook as workbook (active workbook) filename (quoted form of (item 2 of argv)) file format (PDF file format) with overwrite
			else
			    save workbook as workbook (active workbook) filename (quoted form of (item 2 of argv)) file format ((item 3 of argv) file format) with overwrite
			end if
			
			close (active workbook) saving no
			
			return 2
		end tell
	on error errMsg number errorNumber
		return -6
	end try
end run	
