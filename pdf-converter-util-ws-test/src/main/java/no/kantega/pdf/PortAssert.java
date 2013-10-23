package no.kantega.pdf;

import java.io.IOException;
import java.net.Socket;

public final class PortAssert {

    public static final int TEST_PORT = 57379;

    private static final int VALID_PORT_MINIMUM = 1024, VALID_PORT_MAXIMUM = 65535;

    public static void assertPortFree(int port) {
        assert isAvailable(port) : String.format("Port %d is not available", port);
    }

    public static void assertPortBusy(int port) {
        assert !isAvailable(port) : String.format("Port %d is available", port);
    }

    private static boolean isAvailable(int port) {
        assert port >= VALID_PORT_MINIMUM && port < VALID_PORT_MAXIMUM
                : String.format("Invalid start port: %d", port);
        try {
            Socket socket = new Socket("localhost", port);
            socket.close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private PortAssert() {
        throw new AssertionError();
    }
}
