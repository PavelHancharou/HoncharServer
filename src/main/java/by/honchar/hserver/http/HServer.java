package by.honchar.hserver.http;

import by.honchar.hserver.io.PropertiesLoader;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class HServer {

    private final static String SERVER_PROPERTIES_FILE = "/server.properties";
    private final static String SERVER_PORT_PROPERTY = "server.port";
    private final static int DEFAULT_SERVER_PORT = 8080;
    private final static Logger logger = Logger.getLogger(HServer.class);

//    private final static AtomicReference<HServer> serverReference = new AtomicReference<>();
    private volatile boolean isInterrupted = false;


    public HServer(){
    }

//    public static HServer getInstance() {
//        HServer instance = serverReference.get();
//        if(instance == null) {
//            synchronized (HServer.class) {
//                instance = serverReference.get();
//                if(instance == null) {
//                    instance = new HServer();
//                    serverReference.set(instance);
//                }
//            }
//        }
//        return instance;
//
//    }

    private int getServerPort() {
        int serverPort = DEFAULT_SERVER_PORT;
        try {
            Properties properties = PropertiesLoader.loadProperties(SERVER_PROPERTIES_FILE);
            String serverPortProp = properties.getProperty(SERVER_PORT_PROPERTY);
            serverPort = NumberUtils.toInt(serverPortProp, DEFAULT_SERVER_PORT);
        } catch (IOException ex) {
            logger.error("Can not load " + SERVER_PROPERTIES_FILE + " file.", ex);
        }
        return serverPort;
    }

    public void doStart() {
        logger.debug("The Server is running...");
        int serverPort = getServerPort();
        ExecutorService threadPool = new ThreadPoolExecutor(
                4, 64, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(256));
        try(ServerSocket socketListener = new ServerSocket(serverPort, 256)) {
            while(!isInterrupted){
                final Socket socket = socketListener.accept();
                threadPool.submit(new RequestProcessor(socket));
            }
        } catch (IOException ex) {
            logger.error("Can not start the server!!!", ex);
        }
    }

    public void doStop() {
        this.isInterrupted = true;
    }

}