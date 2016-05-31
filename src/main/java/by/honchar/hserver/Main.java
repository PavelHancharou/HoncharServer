package by.honchar.hserver;

import by.honchar.hserver.http.HServer;
import by.honchar.hserver.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class Main {

    private final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        IOUtils.reCreateTmpDir();

        HServer server = new HServer();

        server.doStart();
    }

}
