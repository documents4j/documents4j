package no.kantega.pdf.ws.standalone;

final class CommandDescription {

    private CommandDescription() {
        /* empty */
    }

    public static final String ARGUMENT_LONG_BASE_FOLDER = "base-folder";
    public static final String ARGUMENT_SHORT_BASE_FOLDER = "F";
    public static final String DESCRIPTION_ARGUMENT_BASE_FOLDER = "A directory for saving temporary files and executable " +
            "scripts. If not set, a temporary folder will be generated automatically.";
    public static final String DESCRIPTION_CONTEXT_BASE_FOLDER = "The conversion software needs to save MS Office files to disc " +
            "before they can be converted by a MS Office product. All such files will be safed in this directory and deleted when " +
            "the conversion completed. Also, MS Office will be run by scripts. All theses scripts will be executed from this folder.";

    public static final String ARGUMENT_LONG_CORE_POOL_SIZE = "core-size";
    public static final String ARGUMENT_SHORT_CORE_POOL_SIZE = "S";
    public static final String DESCRIPTION_ARGUMENT_CORE_POOL_SIZE = "The number of threads constantly " +
            "ready to process conversions.";
    public static final String DESCRIPTION_CONTEXT_CORE_POOL_SIZE = "These threads will be constantly held ready for picking " +
            "up conversion jobs. This number should be adjusted to the share of regular conversions that are processed by this server on " +
            "average. These threads are never killed. Idle threads do however imply a cost for maintenance. Together with the number of fallback " +
            "thread, this parameter implicitly determines the number of maximum concurrent conversions. This number should not be set to high " +
            "since the concurrent execution of too many shell processes can result in the operating system killing this application.";

    public static final String ARGUMENT_LONG_MAXIMUM_POOL_SIZE = "fallback-size";
    public static final String ARGUMENT_SHORT_MAXIMUM_POOL_SIZE = "B";
    public static final String DESCRIPTION_ARGUMENT_MAXIMUM_POOL_SIZE = "The number of threads that " +
            "are created dynamically when all core threads are busy.";
    public static final String DESCRIPTION_CONTEXT_MAXIMUM_POOL_SIZE = "These threads will be created on demand if all core " +
            "threads are busy. These threads can be used to overcome peaks. Their creation and maintenance does however imply a " +
            "small overhead. Such threads will be destroyed after they were idle for a specified amount of milliseconds.";

    public static final String ARGUMENT_LONG_KEEP_ALIVE_TIME = "lifetime";
    public static final String ARGUMENT_SHORT_KEEP_ALIVE_TIME = "T";
    public static final String DESCRIPTION_ARGUMENT_THREAD_POOL_FALLBACK_LIFE_TIME = "The number of milliseconds until " +
            "an idle non-core thread is destroyed.";
    public static final String DESCRIPTION_CONTEXT_KEEP_ALIVE_TIME = "Both the creation/destruction of a thread " +
            "and its maintenance imply the use of system resources. A too high number will result in too many idle threads while " +
            "a too low number will cause the constant creation and destruction of threads.";


    public static final String ARGUMENT_LONG_PROCESS_TIME_OUT = "process-timeout";
    public static final String ARGUMENT_SHORT_PROCESS_TIME_OUT = "P";
    public static final String DESCRIPTION_ARGUMENT_PROCESS_TIME_OUT = "The number of milliseconds until a " +
            "conversion process times out.";
    public static final String DESCRIPTION_CONTEXT_PROCESS_TIME_OUT = "A conversion process is conducted by the execution " +
            "of a shell script. Such executions might crash for reasons that cannot be observed by an application running on " +
            "the Java Virtual Machine. Therefore, a timeout needs determine the maximum time for a shell process to terminate " +
            "regularly. This time out is applied per file and counts only the time of actual conversion by an MS Office product.";

    public static final String ARGUMENT_LONG_REQUEST_TIME_OUT = "request-timeout";
    public static final String ARGUMENT_SHORT_REQUEST_TIME_OUT = "R";
    public static final String DESCRIPTION_ARGUMENT_REQUEST_TIME_OUT = "The number of milliseconds until a web request times out.";
    public static final String DESCRIPTION_CONTEXT_REQUEST_TIME_OUT = "If a web request cannot be answered in the specified amount of " +
            "time, the request and the corresponding conversion will be cancelled. This timeout also applies, if the requesting entity " +
            "fails to confirm an answer within this time frame. This value should be set considerably higher than the timeout " +
            "of a conversion process. A too low value will result in requesting entities to be denied file conversion if the server " +
            "is currently very busy.";

    public static final String ARGUMENT_LONG_LOG_TO_FILE = "log";
    public static final String ARGUMENT_SHORT_LOG_TO_FILE = "L";
    public static final String DESCRIPTION_ARGUMENT_LOG_TO_FILE = "A file to which all log information will be written. If " +
            "not set, all log information will be written to the console.";
    public static final String DESCRIPTION_CONTEXT_LOG_TO_FILE = "This file will contain all log information instead of writing " +
            "the log output to the console. Make sure that this file can be written to and that no other application holds locks to it.";

    public static final String ARGUMENT_LONG_HELP = "help";
    public static final String ARGUMENT_SHORT_HELP = "?";
    public static final String DESCRIPTION_CONTEXT_HELP = "Displays information about this program.";

    public static final String DESCRIPTION_BASE_URI = "The base URI of this server instance. (e.g. http://localhost:8080)";
}
