package by.honchar.hserver.http;

import by.honchar.hserver.Main;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class HServerTest {

    @Test
    public void doServerTest() throws InterruptedException {
        Thread serverThread = new Thread(() ->Main.main(null));
        serverThread.start();
        HttpClient client = new HttpClient();
        try {
            String response = client.sendFile();
            Assert.assertTrue(response.indexOf("HTTP/1.1 200 Ok") != -1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
