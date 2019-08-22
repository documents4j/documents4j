package com.documents4j.standalone;

import ch.qos.logback.classic.Level;

import java.io.File;
import java.net.URI;

public class StandaloneClientOptions {

    public Level logLevel = Level.DEBUG;
    public File logFile;
    public Long requestTimeout = 10_000L;
    public URI baseUri;
    public boolean hasSsl;

    @Override
    public String toString() {
        return "StandaloneClientOptions{" +
                "logLevel=" + logLevel +
                ", logFile=" + logFile +
                ", requestTimeout=" + requestTimeout +
                ", baseUri=" + baseUri +
                ", hasSsl=" + hasSsl +
                '}';
    }
}
