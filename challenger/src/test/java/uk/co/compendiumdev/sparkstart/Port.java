package uk.co.compendiumdev.sparkstart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.compendiumdev.challenger.http.httpclient.HttpRequestSender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * check if port is in use
 */
public class Port {

    static Logger logger = LoggerFactory.getLogger(Port.class);

    public static boolean inUse(String host, String port) {
        return inUse(host, Integer.valueOf(port));
    }

    // http://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java
    public static boolean inUse(String host, int port) {
        Socket s = null;
        logger.info("Checking for port on " + host + ":" + port);

        try {

            // prevent this taking ages when no proxy setup by timing out after 5 seconds
            SocketAddress address = new InetSocketAddress(host, port);
            s = new Socket();
            s.connect(address, 5000);

            // If the code makes it this far without an exception it means
            // something is using the port and has responded.
           logger.warn("Port " + port + " is in use, assuming proxy is running");
            return true;
        } catch (IOException e) {
            logger.info("Port " + port + " is free, no proxy running");
            return false;
        } finally {
            if( s != null){
                try {
                    s.close();
                } catch (IOException e) {
                    logger.error("Port " + port + " check had an error ", e);
                    // swallow exception and return false for our use case
                    return false;
                }
            }
        }
    }
}
