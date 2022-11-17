set appName to "Microsoft Word"

try 
if application appName is running then
    return 3 -- everything okay, excel is already running
else  -- excel not running, start it
	return -6 
end if
on error errMsg number errorNumber
	return -6
end try
