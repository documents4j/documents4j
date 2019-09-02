documents4j
===========
documents4j is a Java library for converting documents into another document format. This is achieved by delegating the conversion to any native application which understands the conversion of the given file into the desired target format. documents4j comes with adaptations for MS Word and MS Excel for Windows what allows for example for the conversion of a *docx* file into a *pdf* file without the usual distortions in the resulting document which are often observed for conversions that were conducted using non-Microsoft products.

documents4j offers a simple API and two implementations of this API:

- **Local**: The local API implementation delegates a document conversion to an application on the same machine which is capable of applying the requested conversion. For this to work, the executing machine requires an installation of the backing conversion software such as for example MS Word or MS Excel. documents4j offers a simple mechanism for registering custom converters but ships with implementations of such converters for MS Word and MS Excel for Windows.
- **Remote**: The remote API implementation delegates a document conversion to a server which is queried by a simple REST-API. For this to happen, it sends the file to the server and includes information about the requested document conversion formats. It then receives the converted document as a response to its request.

To users of the documents4j API, it is fully transparent which implementation is used. This way, a local conversion implementation can for example be applied in a test environment while applying the remote implementation in production. Also, this allows for easy mocking of the converter back-end.

The API
-------
documents4j uses a fluent API for performing a document conversion. As mentioned, the API does not expose any details of the backing converter implementation. Instead, a converter is represented by an instance of `IConverter`. Using this converter, an example conversion of a MS Word file into a PDF is executed as follows:

```java
File wordFile = new File( ... ), target = new File( ... );
IConverter converter = ... ;
Future<Boolean> conversion = converter
                                .convert(wordFile).as(DocumentType.MS_WORD)
                                .to(target).as(DocumentType.PDF)
                                .prioritizeWith(1000) // optional
                                .schedule();
```

All methods of the `IConverter` interface and its builder types offer overloaded methods. Instead of providing `File` instances, it is also possible to provide an `InputStream` as a source document and an `OutputStream` for writing the result. These streams are never closed by documents4j. As another option, the source document can be obtained by querying an `IInputStreamSource` or an `IFileSource` which offer generic callback methods which are then used by documents4j. Similarly, the `IInputStreamConsumer` and `IFileConsumer` interfaces allow for implementing a generic way of processing the result of a conversion. However, note that these callbacks are normally triggered from another thread. These threads are used by documents4j internally such that you should not perform heavy tasks from these callbacks. documents4j is fully thread-safe as long as it is not stated differently.

Finally, a conversion can be prioritized via `prioritizeWith` where a higher priority signals to the converter that a conversion should be conducted before a conversion with lower priority if both conversions are getting queued. documents4j is capable of performing document conversions concurrently and puts conversion into an internal job queue which is organized by these priorities. There is however not guarantee that a conversion with higher priority is performed before a conversion with lower priority.

A conversion can be scheduled to be executed in the background by calling `schedule` after specifying a conversion. Alternatively, by calling `execute`, the current thread will block until the conversion is finished. The resulting `boolean` indicates if a conversion was successful. Exceptional conversion results are however communicated by exceptions which are described below.

For finding out which conversions are supported by an `IConverter`, you can query the `getSupportedConversions` method which returns a map of source formats to their supported target formats. Furthermore, you can call the `isOperational` in order to check the functionality of a converter. A converter might not be operational because its prerequisites are not met. Those prerequisites are described below for each implementation of an `IConverter`.

Note that an `IConverter` implementation might describe a rather expensive structure as it is normally backed by external resources such as native processes or a network connection. For repeated conversions, you should reuse the same instance of an `IConverter`. Furthermore, note that an `IConverter` has an explicit life-cycle and must be shut down by invoking `shutDown`. documents4j registers a shut-down hook for shutting down converter instances, but you should never rely on this mechanism. Once an `IConverter` was shut down, it cannot be restarted. After a converter was shut down, its `isOperational` always returns `false`.

Local converter
---------------
The `LocalConverter` implementation of `IConverter` performs conversions by converting files within the same (non-virtual) machine. A `LocalConverter` is created by using a simple builder:

```java
IConverter converter = LocalConverter.builder()
                           .baseFolder(new File("C:\Users\documents4j\temp"));
                           .workerPool(20, 25, 2, TimeUnit.SECONDS)
                           .processTimeout(5, TimeUnit.SECONDS)
                           .build();
```

The above converter was configured to write temporary files into the given folder. If this property is set, documents4j creates a random folder. By setting a worker pool, you determine the maximum number of concurrent conversions that are attempted by documents4j. A meaningful value is ultimately determined by the capabilities of the backing converters. It is however also determined by the executing machine's CPU and memory. An optimal value is best found by trial-and-error. 

Furthermore, a timeout for external processes of 5 seconds is set. In order to convert a file into another document format, the conversion is delegated to an implementation of `IExternalConverter`. Such external converters normally start a process on the OS for invoking a conversion by some installed software. documents4j ships with two such external converters, once implementation for MS Word on Windows and one for MS Excel on Windows. If these converters are found on the class path, the `LocalConverter` discovers and loads them automatically unless they are explicitly deregistered by the builder's `disable` method. Custom converters need to be registered explicitly by the builder's `enable` method.

Note that the builder itself is mutable and not thread-safe. The resulting `LocalConverter` on the other side is fully thread-safe.

#### Microsoft Word converter ####
The MS Word converter is represented by a `MicrosoftWordBridge` instance. This bridge starts MS Word when the connected `LocalConverter` is started an quits Word once the local converter is shut down. Note that this implies that only a single active `LocalConverter` instance must exist not only for a JVM but for the entire physical machine. Otherwise, MS Word might be shut down by one bridge while it is still required by another instance. This cannot be controlled by documents4j but must be assured by its user. Also, make sure not to use MS Word outside of a Java application while a `MicrosoftWordBridge` is active, for example by opening it from your desktop.

Furthermore, the `LocalConverter` can only be run if:

- The JVM is run on a MS Windows platform that ships with the Microsoft Scripting Host for VBS (this is true for all contemporary versions of MS Windows.
- MS Word is installed in version 2007 or higher. PDF conversion is only supported when the PDF plugin is installed. The plugin is included into MS Word from Word 2010 and higher.
- MS Word is not already running when the `LocalConverter` starts. This is in particularly true for MS Word instances that are run by another instance of `LocalConverter`. (As mentioned, be aware that this is also true for instances running on a different JVM or that are loaded by a different class loader.)
- MS Word is properly activated and configured for the user running the JVM. MS Word does therefore not require any configuration on program startup or any other wizard.
- When the JVM application which uses the `LocalConverter` is run as a service, note the information on using MS Word from the MS Windows service profile below.

Note that MS Windows's process model requires GUI processes (such as MS Word) to be started as a child of a specific MS Windows process. Thus, the MS Word process is never a child process of the JVM process. Thus, the MS Word process will survive in case that the JVM process is killed without triggering its shut-down hooks. Make sure to always end your JVM process normally when using documents4j. Otherwise, orphan processes might live without the JVM process. documents4j will however attempt to reuse these processes after a restart.

#### Microsoft Excel converter ####
The MS Excel converter is represented by a `MicrosoftExcelBridge` instance. All information that was given on the `MicrosoftWordBridge` apply to the MS Excel bridge. However, note that MS Excel is not equally robust as MS Word when it comes to concurrent access. For this reason, the `MicrosoftExcelBridge` only allows for the concurrent conversion of a single file. This property is enforced by documents4j by using an internal lock.

**Important**: Note that you have to manually add a dependency to either the `MicrosoftWordBridge` or the `MicrosoftExcelBridge` when using the `LocalConverter`. The MS Word bridge is contained by the *com.documents4j/documents4j-transformer-msoffice-word* Maven module and the MS Excel bridge by the *com.documents4j/documents4j-transformer-msoffice-excel* module.

#### Give it a try ####
documents4j was written after evaluating several solutions for converting *docx* files into *pdf* which unfortunately all produced files with layout distortions of different degrees. For these experiences, documents4j comes with an evaluation application which is run in the browser. For starting this application, simply run the following commands on a Windows machine with MS Word and MS Excel installed:   

```shell
git clone https://github.com/documents4j/documents4j.git
cd documents4j
cd documents4j-local-demo
mvn jetty:run
```

You can now open `http://localhost:8080` on you machine's browser and convert files from the browser window. Do not kill the application process but shut it down gracefully such that documents4j can shut down its MS Word and MS Excel processes. In order for this application to function, MS Word and MS Excel must not be started on application startup.

#### Custom converters ####
Any converter engine is represented by an implementation of `IExternalConverter`. Any implementation is required to define a public constructor which accepts arguments of type `File`, `long` and `TimeUnit` as its parameters. The first argument represents an existing folder for writing temporary files, the second and third parameters describe the user-defined time out for conversions. Additionally, any class must be annotated with `@ViableConversion` where the annotation's `from` parameter describes accepted input formats and the `to` parameter accepted output formats. All these formats must be encoded as [parameterless MIME types](http://en.wikipedia.org/wiki/Internet_media_type). If a converter allows for distinct conversions of specific formats to another then the `@ViableConversions` annotation allows to define several `@ViableConversion` annotations.

Remote converter
----------------
A `RemoteConverter` is created fairly similar to a `LocalConverter` by using another builder:

```java
IConverter converter = RemoteConverter.builder()
                           .baseFolder(new File("C:\Users\documents4j\temp"));
                           .workerPool(20, 25, 2, TimeUnit.SECONDS)
                           .requestTimeout(10, TimeUnit.SECONDS)
                           .baseUri("http://localhost:9998");
                           .build();
```

Similarly to the `LocalConverter`, the `RemoteConverter` requires a folder for writing temporary files which is created implicitly if no such folder is specified. This time however, the worker pool implicitly determines the number of concurrent REST requests for converting a file where the request timeout specifies the maximal time such a conversion is allowed to take. As the base URI, the remote converter specifies the address of a *conversion server* which offers a REST API for performing document conversions. Note that all the `IConverter`'s `getSupportedConversions` and `isOperational` methods delegate to this REST API as well and are not cached. 

#### Conversion server ####
documents4j offers a standalone conversion server which implements the required REST API by using a `LocalConverter` under the covers. This conversion server is contained in the *com.documents4j/documents4j-server-standalone* module. The Maven build creates a shaded artifact for this module which contains all dependencies. This way, the conversion server can be started from the command line, simply by:

```shell
java -jar documents4j-server-standalone-<VERSION>-shaded.jar http://localhost:9998
```

The above command starts the conversion server to listen for a HTTP connection on port 9998 which is now accessible to the `RemoteConverter`. The standalone server comes with a rich set of option which are passed via command line. For a comprehensive description, you can print a summary of these options by supplying the `-?` option on the command line. 

A conversion server can also be started programmatically using a `ConversionServerBuilder`.

#### Conversion client ####
Similarly to the conversion server, documents4j ships with a small console client which is mainly intended for debugging purposes. Using the client it is possible to connect to a conversion server in order to validate that a connection is possible and not prevented by for example active fire walls. The client is contained in the *com.documents4j/documents4j-client-standalone* module. You can connect to a server by:

```shell
java -jar documents4j-client-standalone-<VERSION>-shaded.jar http://localhost:9998
```

Again, the `-?` option can be supplied for obtaining a list of options.

#### Encryption ####

 It is possible to use a SSL connection between the client and server by specifying a `SSLContext` in the server.

TThe standalone implementations of server and client converters are capable of using `SSLContext.getDefault()` for establishing a connection by setting the `-ssl` parameter on startup. The [default trust store and key store configuration](http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#InstallationAndCustomization)) can be adjusted by setting `javax.net.ssl.*` system properties when running a standalone application from the console. The allowed encryption algorithms can be adjusted by setting the `https.protocols` property.

To run the standalone server with SSL support:

1. Import your certificate into a keystore. `keytool` does not support importing certificates directly, therefore, you have to bundle them first using `openssl`:
   ```
   openssl pkcs12 -export -in /path/to/your/cert.crt -inkey /path/to/your/cert.key -name serverCert -out /tmp/keystore-PKCS-12.p12 -password pass:yourPassword
   keytool -importkeystore -noprompt -deststorepass yourPassword -srcstorepass yourPassword -destkeystore /path/to/your/keystore -srckeystore /tmp/keystore-PKCS-12.p12
   ```
2. Afterwards, run the server with the given key store:
   ```
   java -jar documents4j-client-standalone-<VERSION>-shaded.jar https://0.0.0.0:8443 -ssl -Djavax.net.ssl.keyStore=/path/to/your/keystore -Djavax.net.ssl.keyStorePassword=yourPassword
   ```

A password such as `yourPassword` can be any chosen freely but is required.

#### Authentication ####

The standalone server can be started with basic authentication support with `-auth user:pass`.

Aggregating converter
----------------
Additionally to the `LocalConverter` and the `RemoteConverter`, documents4j extends the `IConverter` API by `IAggregatingConverter` which allows to delegate conversions to a collection of underlying converters. This interface is implemented by the `AggregationConverter` class.
 
Using this extension serves three main purposes:

1. It allows for the aggregation of several `IConverter`s to achieve a load balancing for multiple conversions. By default, an `AggregatingConverter` applies a *round robin* strategy. A custom strategy can be implemented as an `ISelectionStrategy`.
2. Using the methods of the `IAggregatingConverter` interface, it is possible to register or remove aggregated `IConverter`s after the creation of the `AggregatingConverter`. This way, it is for example possible to migrate to another conversion server without restarting an application or to restart an inoperative local converter.
3. It allows to expose multiple converters that support different conversion formats by a single instance of `IConverter`. 

An `AggregatingConverter` is created using a similar builder as when creating a `LocalConverter` or `RemoteConverter` which allows to specify the converter's behavior:

```java
IConverter first = ... , second = ... ;

IConverterFailureCallback converterFailureCallback = ... ;
ISelectionStrategy selectionStrategy = ... ;

IAggregatingConverter converter = AggregatingConverter.builder()
                                      .aggregates(first, second)
                                      .selectionStrategy(selectionStrategy)
                                      .callback(converterFailureCallback)
                                      .build();
```

An `AggregatingConverter` cannot generally guarantee the success of an individual conversion if an aggregated `IConverter` becomes inoperative during a conversion process. The aggregating converter does however eventually discover a converter' inaccessibility and removes it from circulation. For being notified of such events, it is possible to register a delegate as an `IConverterFailureCallback`. It is also possible to request regular health checks when creating a converter. Doing so, inoperative converters are checked for their state and removed on failure in fixed time intervals.

Exception hierarchy
-------------------
The exception hierarchy was intentionally kept simple in order to hide the details of an `IConverter` implementation from the end user. All exceptions thrown by the converters [are unchecked](http://www.artima.com/intv/handcuffs.html). This is of course not true for futures which fulfill the [`Future` interface contract](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html) and wrap any exception in an [`java.util.concurrent.ExecutionException`](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutionException.html) whenever `Future#get()` or `Future#get(long, TimeUnit)` are invoked.

The native exceptions thrown by an `IConverter` are either instances of `ConverterException` or one of its subclasses. Instances of `ConverterException` are only thrown when no specific cause for an error could be identified. More specific exceptions are:

- `ConversionFormatException`: The converter was requested to translate a file into a `DocumentType` that is does not support.
- `ConversionInputException`: The source file that was provided for a conversion could not be read in the given source file format. This means that the input data either represents another file format or the input data is corrupt and cannot be read by the responsible converter.
- `FileSystemInteractionException`: The source file does not exist or is locked by the JVM or another application. (*Note*: You must **not** lock files in the JVM when using a `LocalConverter` since they might need to be processed by another software which is then prevented to do so.) This exception is also thrown when the target file is locked. Unlocked, existing files are simply overwritten when a conversion is triggered. Finally, the exception is also thrown when using a file stream causes an `IOException` where the IO exception is wrapped before it is rethrown. 
- `ConverterAccessException`: This exception is thrown when a `IConverter` instance is in invalid state. This occurs when an `IConverter` was either shut down or the conditions for using a converter are not met, either because a remote converter cannot longer connect to its conversion server or because a backing conversion software is inaccessible. This exception can also occur when creating a `LocalConverter` or a `RemoteConverter`.

**Note**: Be aware that `IConverter` implementations do not follow a prevalence of exceptions. When a user is trying to convert a non-existent file with a converter in an inoperative state, it cannot be guaranteed that this will always throw a `FileSystemInteractionException` instead of a `ConverterAccessException`. The prevalence will differ for different implementations of the `IConverter` API.

Logging
-------
All logging is delegated to the [SLF4J](http://www.slf4j.org) facade and can therefore be processed independently of this application. The verbosity of this application's logging behavior is determined by the overall logging level where *info* or *warn* are recommended as minimum logging levels in production. The different logging levels will determine the following events to be logged:

- *trace*: On this level, all concurrent code will log the acquisition and release of monitors.
- *info*: On this level, non-exceptional state interactions with external resources will be logged. A logging message will for example expose when MS Word is started or stopped or when a conversion server is bound to a port.
- *debug*: This logging level is not used by this application.
- *warn*: On this level, non-fatal errors are logged such as the timeout of a HTTP conversion due to high traffic. Normally, such log events are accompanied by an exception being thrown.
- *error*: On this level, all user errors are logged. For example, the attempt of converting a non-existent file would cause a logging event on this level. Normally, such events are accompanied by an exception being thrown.

Monitoring
----------

documents4j registers two monitoring endpoints:

* Health endpoint under `/health` returning *200 OK* if the converter server is operational and *500 Internal Server Error* otherwise.
* Running endpoint under `/running` returning always *200 OK*.

Both endpoints are always unprotected, even if the documents4j runs with basic authentication.

Troubleshooting
---------------

* Don't open and close MS Office on the same machine as the server is running. After closing it again, the server won't be operational any more. The client will fail with 
    ````
    com.documents4j.throwables.ConverterAccessException: The converter could not process the request
    ````
    and `<operational>false</operational>` will appear at the top of the status xml page returned by `GET /`.
    Of course, there may be more reasons causing MS Office to stop working. A restart of the server will fix this.

* If the server is run by command line in an Windows Command Line window be sure not to leave the window in "Select mode" by clicking on it or marking text. This will cause the server not to respond any more and the clients will run into timeouts.

Performance considerations
-------------------------

#### Input and target description ####
The API intents to hide the implementation details of a specific `IConverter` implementation from the end user. However, a `RemoteConverter` needs to send data as a stream which requires reading it to memory first. (As of today, documents4j does not make use of Java NIO.) This is why a `RemoteConverter` will always perform better when handed instances of `InputStream` and `OutputStream` as source and target compared to files. The `LocalConverter` on the other hand, communicates with a backing conversion software such as MS Word by using the file system. Therefore, instances of `File` as source and target input will perform better when using a `LocalConverter`.

In the end, a user should however always try to hand the available data to the `IConverter` implementation. The implementation will then figure out by itself what data it requires and convert the data to the desired format. In doing so, the converter will also clean up after itself (e.g. closing streams, deleting temporary files). There is no performance advantage when input formats are converted manually.

#### Configuring an executing JVM ####
MS Office components are (of course) not run within the Java virtual machine's process. Therefore, an allocation of a significant amount of the operating system's memory to the JVM can cause an opposite effect to performance than intended. Since the JVM already reserved most of the operating system's memory, the MS Word processes that were started by the JVM will run short for memory. At the same time, the JVM that created these processes remains idle waiting for a result. It is difficult to tell what amount of memory should optimally be reserved for the JVM since this is highly dependant of the number of concurrent conversion. However, if one observes conversion to be critically unperformant, the allocation of a significant amount of memory to the JVM should be considered as a cause. 

#### Configuring MS Office ####
When running a MS Office-based converter, it it important to appropriately configure MS Office before running documents4j. For example, it is crucial to disable all kinds of start-up wizards which can abort the conversion process if the MS Office API returns unexpected status codes. Furthermore, it can improve performance significantly when bookkeeping features such as the *recent documents* listing are disabled.

Running as Windows service
--------------------------
documents4j might malfunction when run as a Windows service together with MS Office conversion. Note that MS Office does [not officially support](http://support.microsoft.com/kb/257757) execution in a service context. When run as a service, MS Office is always started with MS Window's local service account which does not configure a desktop. However, MS Office expects a desktop to exist in order to run properly. Without such a desktop configuration, MS Office will start up correctly but fail to read any input file. In order to allow MS Office to run in a service context, there are two possible approaches of which the first approach is more recommended:

1. On a 32-bit system, create the folder *C:\Windows\System32\config\systemprofile\Desktop*. On a 64-bit system, create the folder *C:\Windows\SysWOW64\config\systemprofile\Desktop*. [Further information can be found on MSDN](http://social.msdn.microsoft.com/Forums/en-US/b81a3c4e-62db-488b-af06-44421818ef91/excel-2007-automation-on-top-of-a-windows-server-2008-x64?forum=innovateonoffice).
2. You can manipulate MS Window's registry such that MS Office applications are run with another account than the local service account. [This approach is documented on MSDN](http://social.technet.microsoft.com/Forums/en-US/334c9f30-4e27-4904-9e71-abfc65975e23/problem-running-windows-service-with-excel-object-on-windows-server-2008-64-bit?forum=officesetupdeploylegacy). Note that this breaks MS Window's sandbox model and imposes additional security threats to the machine that runs MS Office.

When running the standalone server, you should also start it in service mode by the `-M` flag to avoid using system input.

Windows service - troubleshooting
--------------------------

1. If you are experiencing requests jammed by a MS Office window reporting stating that some previous conversion failed, you can use MS bat script for looking those windows and close them over Windows' task scheduler. First create a .bat file with these commands and add it as a new task to run every minute:
    ```
    taskkill /F /FI "WindowTitle eq Microsoft Word*"
    taskkill /F /FI "WindowTitle eq Microsoft Office*"
    ```
2. For those that are experiencing multiples instances of msword and wsscript on task manager, all of them as zombies, you can use MS bat script to kill them by script over Windows' task scheduler. First create a *.bat* file with these commands, then, add it to new task to run every day at 6AM (for instance). Attention: current conversions will fail when this script been triggered, therefore, choose an idle time for the script application:
    ```
    taskkill /f /t /im wscript.exe
    taskkill /f /t /im winword.exe
    ```

Building the project
--------------------
This project is set up to allow running as many tests as possible without requiring MS Office or even MS Windows installed. For this purpose, the project includes several rich stubs that step in place of the MS Office bridges. When you are building this project on a machine with MS Windows and MS Office installed, you should build the project with the `ms-office` profile which triggers tests that rely on an actual MS Office instance. You can then build the project using Maven:

```shell
mvn package -Pms-office
```
 
When you are testing native converters such as the `MicrosoftWordBridge` or the `MicrosoftExcelBridge`, do not forget to keep an eye on your task manager. Consider an alternative to the default task manager such as [Process Explorer](http://technet.microsoft.com/en-us/sysinternals/bb896653.aspx) for debugging purposes. For monitoring network connections, I recommend [TCPView](http://technet.microsoft.com/de-de/sysinternals/bb897437.aspx). 

Several time consuming operations such as building source code and javadoc artifacts as well as building the shaded jar for the standalone server are only executed when the corresponding profile `extras` is active.

[![Build Status](https://travis-ci.org/documents4j/documents4j.svg)](https://travis-ci.org/documents4j/documents4j)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.documents4j/documents4j-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.documents4j/documents4j)
[![Download](https://api.bintray.com/packages/raphw/maven/documents4j/images/download.svg)](https://bintray.com/raphw/maven/documents4j/_latestVersion)

Licensing
---------
This software is licensed under the [*Apache Licence, Version 2.0*](http://www.apache.org/licenses/LICENSE-2.0.html). When using this converter in correspondence with MS Office products, please note Microsoft's commentary on [the use of MS Office in a server context](http://support.microsoft.com/kb/257757) which is not officially supported. Also note
the legal requirements for using MS Office in a server context. Microsoft states:

> Current licensing guidelines prevent Office applications from being used on a server to service client requests, unless those clients themselves have licensed copies of Office. Using server-side Automation to provide Office functionality to unlicensed workstations is not covered by the End User License Agreement (EULA).

Note that documents4j has several dependencies which are note licensed under the Apache License. This includes dependencies using a CDDL license and the GPL license with a class path exception. All this normally allows the use of documents4j without redistributing the source code. However, note that using documents4j comes without any (legal) warranties, both when used together with or without MS Office components. 

Credits
-------
This application was developed by [Kantega AS](http://kantega.no) as a project order of the [municipality of Oslo](http://www.oslo.kommune.no) and was open-sourced thanks to their generous endorsement. 

This library would not possible without the use of [*zt-exec*](https://github.com/zeroturnaround/zt-exec) library from ZeroTurnaround is a great help for handling command line processes in a Java application. Also, I want to thank the makers of [*thread-weaver*](http://code.google.com/p/thread-weaver/) for their great framework for unit testing concurrent applications. Finally, without the help of [*mockito*](http://code.google.com/p/mockito/), it would have been impossible to write proper unit tests that run without the integration of MS Word.  
