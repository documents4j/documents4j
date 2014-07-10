package com.documents4j.ws;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class PortAssert {

    private static final int VALID_PORT_MINIMUM = 1024, VALID_PORT_MAXIMUM = 65535;

    private PortAssert() {
        throw new AssertionError();
    }

    public static void assertPortFree(int port) {
        assertTrue(String.format("Port %d is not available", port), isAvailable(port));
    }

    public static void assertPortBusy(int port) {
        assertFalse(String.format("Port %d is available", port), isAvailable(port));
    }

    private static boolean isAvailable(int port) {
        assertTrue(String.format("Invalid start port: %d", port),
                port >= VALID_PORT_MINIMUM && port < VALID_PORT_MAXIMUM);
        try {
            Socket socket = new Socket("localhost", port);
            socket.close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    public static int findFreePort() throws IOException {
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        return port;
    }
}
