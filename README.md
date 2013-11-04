A reliable Word to PDF converter for Java applications
=============
This converter is a tool for converting MS Word files to PDF in Java. This is achieved by delegating the conversion
to a running instance of MS Word. This does however not mean that MS Word must be installed on the local machine.
Besides running locally, the converter can be run from a server which offers a REST-API to a converter client. We
experienced that this is the only reliable way of converting MS Word files since a native approach often results
in distorted files due to the bad documentation and the frequent changes of the Word file format. Finally, we
discovered that MS Word offers a much faster conversion than non-Microsoft solutions. This makes this converter
feasible for converting large amounts of files. (We tried quite a few before writing this application.)

**Note**: This converter was written with the aspiration of extending its functionality in the future. This might
require minor changes in its API in the future. These changes will be announced and documented.

The API
---------------------
The converter is implemented using a fluent API which hides any details of a converter's implementation. The entry point
to a conversion is represented by an `IConverter` implementation which offers an overloaded `convert` method. This method
either takes an `InputStream` or a `File` as its argument. Alternatively, `IInputStreamSource` and `IFileSource` offer
a higher level hook into a conversion's life cycle. After specifying a conversion's source, the `to` method is to be called.
This similarly overloaded method can either take an `OutputStream` or a `File` as its argument. As with specifying the
source, `IInputStreamConsumer` and `IFileConsumer` allow for a higher level hook into the conversion process. Finally,
a priority can be specified via `prioritizeWith` where a higher priority signals to the converter that a conversion should
be conducted before a conversion with lower priority if both conversions are queued at the same time.

A conversion with a given instance of `IConverter` would therefore look like this:

```
File source = new File( ... ), target = new File( ... );
IConverter converter = getConverter();
Future<Boolean> conversion = converter.convert(source).to(target).prioritizeWith(1000).schedule();
```

As obvious from the example above, a conversion can be scheduled in the background by calling `schedule` on the
specified conversion. Alternatively, calling `execute` will block and return a primitive `boolean` that signaling the
conversion's success.

Local converter
---------------------
The `LocalConverter` implementation of `IConverter` runs an instance of MS Word on the local machine. This is achieved
by communicating with a collection of VBS scripts which are triggered by the Java application on the MS Windows PowerShell.
This means that this Java application is not portable! The local converter can only be run if:

-   The JVM is run on a MS Windows platform that ships with the Microsoft Scripting Host for VBS (this is true for
    all contemporary versions of MS Windows.
-   MS Word is installed in version 2010 or higher (alternatively: MS Word 2007 with installed PDF plugin).
-   MS Word is not running when the `LocalConverter` starts. This is in particularly true for MS Word instances that
    are run by another instance of `LocalConverter`. (Be aware that this is also true for applications running on a
    different JVM.)

When these requirements are met, the construction of a `LocalConverter` is fairly easy. A preconfigured instance can
be retrieved by calling `LocalConverter.make()`. A builder that allows for custom configuration is created via
`LocalConverter.builder()`.

Remote converter
---------------------
The `RemoteConverter` implementation of `IConverter` connects to a conversion server (described above). It will send files
over the internet or the local network in order to conduct a conversion via a `LocalConverter` that is installed on a different
machine. This does of course introduce a small time penalty compared to directly using a `LocalConverter`. A `RemoteConverter`
can be constructed similarly to the `LocalConverter` by calling `RemoteConverter.make("http://myserver:9090/")` when a remote
server is accessible under this address. Alternatively, `RemoteConverter.builder()` offers a richer set of configuration
possibilities.

Conversion server
---------------------
The easiest way of setting up a conversion server is command line is command line invocation. The
*no.kantega/pdf-converter-server-standalone* (a summary of Maven modules is given below) module creates a shaded
jar that can be executed by for example:

```
java -jar conversion-server-standalone.jar http://myserver:9090/
```

A richer set of configuration is offered by several command line arguments which are described when

```
java -jar conversion-server-standalone.jar -?
```

is invoked from the command line.

Exceptions
---------------------
The exception hierarchy was kept intentionally simple in order to hide the details of an `IConverter` implementation from
the end user. All exceptions thrown by the converters [are unchecked](http://www.artima.com/intv/handcuffs.html). This is
of course not true for futures which fullfil the interface contract by wrapping any exceptions in
[`java.util.concurrent.ExecutionException`](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutionException.html).

All exceptions thrown are either instances of `ConverterException` or its subclasses. Instances of `ConverterException`
are only thrown when no specific error could be identified:

-  **ConversionInputException**: The source provided for a conversion was not found in the expected file format. This
   means that the input data either represents another file format or the input data is corrupt and cannot be read by
   MS Word.
-  **FileSystemInteractionException**: The source file does not exist or is locked by the JVM or another application.
   (You must not lock files within the JVM when using a `LocalConverter` since they eventually need to be shared with
   MS Word which is regarded as another application by MS Windows.) This exception is also thrown when the target is locked
   (unlocked, existing files are simply overwritten when a conversion is triggered in order to imitate the behaviour of
   MS Word even when using a `RemoteConverter`). Finally, the exception is also thrown when any file stream causes
   an `IOException` where this exception gets wrapped.
-  **ConverterAccessException**: This exception is thrown when a `IConverter` instance is in a bad state. This can happen
   when MS Word is for example shut down by a third entity or when the `LocalConverter` is run on a Linux machine. The exception
   is also thrown when a remote conversion server is not reachable.

**Note**: Be aware that `IConverter` implementations do not follow a prevalence of exceptions. When a user is trying to convert
a non-existent file via a broken converter, it cannot be guaranteed that this will always throw a `FileSystemInteractionException`
instead of a `ConverterAccessException`. This behavior will most likely differ for different converters.

Converter life cycle
---------------------
Each converter should be down via the `IConverter#shutDown` method when it is not used any more. This will free external
resources (such as the running instance of MS Word which is quite a heavy external resource) and clean up any files created.

The `IConverter` implementations do provide shut down hooks for this purpose but this should be seen as a last resort since
shut down hooks might not be executed by a JVM that is getting killed which would for example leak the MS Word instance.

Be aware that a converter must not be used after it was shut down and must be replaced with a fresh instance. Attempting
a conversion after shutting down a converter might result in strange behavior! Therefore, you should attempt to get rid of
the reference.

For diagnostic purposes, the `IConverter` interface offers the `isOperational` method which will return `true` if the
`IConverter` was not yet shut down and is currently functional (i.e. MS Word was not shut down by a third entity or
the network connection is functional).

Efficiency considerations
---------------------
The API intents to hide the implementation details of a specific `IConverter` implementation from the end user. However,
a `RemoteConverter` needs to send data as a stream from memory. This is why a `RemoteConverter` will always perform better
when handed instances of `InputStream` and `OutputStream` as source and target. The `LocalConverter` on the other hand,
communicates with MS Word by using the file system. Therefore, instances of `File` as source and target will perform
better when using a `LocalConverter`.

The end user should however always try to hand the available data to the `IConverter` implementation. The implementation
will then figure out by itself what data it requires and convert the data to the desired format. In doing so, the
converter will also clean up after itself (e.g. closing streams, deleting temporary files). There is no performance
advantage when input formats are converted manually.

Maven modules
---------------------
The following modules are of interest for the end user:

-  The *no.kantega/pdf-converter-api* module contains the API. It is advised to build applications against this API only
   and to provide an application-wide implementation by a dependency injection framework such as Guice, Spring or HK2.
-  The *no.kantega/pdf-converter-local* module contains the `LocalConverter` implementation. Be aware that this converter
   intents to one day host more than only MS Word. For this reason, you have to additionally put a dependency to the
   *no.kantega/pdf-converter-transformer-msoffice-word* module on your class path. The local converter will then
   auto detect the MS Word converter implementation. In doing so, we allow future implementations to choose among
   different external converters.
-  The *no.kantega/pdf-converter-remote* module contains the `RemoteConverter` implementation.
-  The *no.kantega/pdf-converter-server-standalone* module contains the shaded jar described above and a `ConverterServerBuilder`
   that allows to create a conversion server programmatically. (If you are using the `ConverterServerBuilder`, make sure
   to include the unshaded jar into your project.) The shaded jar comes bundled with the
   *no.kantega/pdf-converter-transformer-msoffice-word* dependency.
-  The *no.kantega/pdf-converter-local-demo* includes a demo application which allows to run a `LocalConverter` from
   a simple application in the browser. Simply run the application for example by calling `mvn jetty:run` and go to
   for example *http://localhost:8080*. You can then upload MS Word files and view the resulting PDF documents.
   **Warning**: Do not kill the application server since shut down hooks are not executed. This will lead to a leak of
   the MS Word instance. (You can remove this leaked instance via the Windows task manager.) In order to avoid this leak,
   terminate the servlet container running the application gently.

Building the project
---------------------
This project was set up to allow running as many tests as possible without requiring MS Word. For this purpose, the project
includes a lot of rich stubs that step in place of MS Word. When you are building this project on a machine that does not
run MS Windows or MS Word, you should build the project with the *no-office* profile which skips any tests that rely on
an actual MS Word instance:

```
mvn clean package -Pno-office
```

If Maven discovers that the build is not run on MS Windows, this profile will be activated by default.

Several time consuming operations such as building source code and javadoc artifacts as well as building the shaded jar are
only executed when the *extras* profile is active.

Credits
---------------------
Without the [*zt-exec*](https://github.com/zeroturnaround/zt-exec) library from ZeroTurnaround, the work in this project
would have been a lot harder. Also, I want to thank the makers of [*threadweaver*](http://code.google.com/p/thread-weaver/)
for their great framework for unit testing concurrent applications.
