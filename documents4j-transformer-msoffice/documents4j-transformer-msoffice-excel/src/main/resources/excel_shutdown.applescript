set appName to "Microsoft Excel"

try 
if application appName is running then
    tell application appName to quit 
else  -- excel not running, nothing to do
	return 3 
end if
on error errMsg number errorNumber
	return -6
end try
