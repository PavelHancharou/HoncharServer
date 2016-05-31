package by.honchar.hserver.http;

import by.honchar.hserver.io.ClassPathFileLoader;
import by.honchar.hserver.io.IOUtils;

import java.io.*;
import java.net.Socket;

public class HttpClient {

    private static final String TEST_FILE_PATH = "TestFile.txt";
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8080;

    public String sendFile() throws IOException {
        String checkSum = IOUtils.calculateClassPathFileCheckSum(TEST_FILE_PATH);
        String headers = createHeaders(checkSum);
        System.out.println("Headers: " + headers);

        try(Socket socket = new Socket(HOST, PORT);
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream())) {
                sendToServer(headers, bos);
                sendToServer(ClassPathFileLoader.loadFileInputStream(TEST_FILE_PATH), bos);
                String response = readServerResponse(bis);
                System.out.println(response);
                return response;
        }
    }

    private String createHeaders(String checkSum) {
        return new StringBuilder().append("POST /localhost HTTP/1.1\r\n")
                .append("Number: 1\r\n")
                .append("File Name: ").append(TEST_FILE_PATH).append("\r\n")
                .append("IsLast: false\r\n")
                .append("CheckSum: ").append(checkSum).append("\r\n\r\n").toString();
    }

    private void sendToServer(String content, BufferedOutputStream bos) throws IOException {
        for(int cByte: content.getBytes()){
            bos.write(cByte);
        }
        bos.flush();
    }

    private void sendToServer(InputStream contentIs, BufferedOutputStream bos) throws IOException {
        int i;
        while((i = contentIs.read()) != -1){
            bos.write(i);
        }
        bos.flush();
    }

    private String readServerResponse(InputStream is) throws IOException {
        StringBuilder builder = new StringBuilder();
        int i;
        while((i = is.read()) != -1){
            builder.append((char)i);
        }
        return builder.toString();
    }

}
