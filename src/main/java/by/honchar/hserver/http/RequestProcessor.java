package by.honchar.hserver.http;

import by.honchar.hserver.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class RequestProcessor implements Runnable {

    private static final String HEADER_DELIMETER = "\r\n";
    private static final String HEADER_CONTENT_DELIMETER = ": ";
    private static final String PACKAGE_BODY_DELIMETER = "\r\n\r\n";
    private static final String HEADER_NUMBER = "Number";
    private static final String HEADER_FILE_NAME = "File Name";
    private static final String HEADER_IS_LAST = "IsLast";
    private static final String HEADER_CHECK_SUM = "CheckSum";
    private static final long ERROR_LONG_VALUE = -1;

    private final static Logger logger = Logger.getLogger(RequestProcessor.class);

    private final Socket socket;

    public RequestProcessor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try(BufferedInputStream bis = new BufferedInputStream(socket.getInputStream())) {
            Map<String, String> headers = extractHeaders(bis); //TODO: IOException

            String fileName = headers.get(HEADER_FILE_NAME);
            String packageNumber = headers.get(HEADER_NUMBER);

            if(StringUtils.isNotBlank(fileName)) {
                File writtenFile = IOUtils.writePartToFile(fileName, packageNumber, bis); //TODO: IOException
                long headerCheckSum = NumberUtils.toLong(headers.get(HEADER_CHECK_SUM), ERROR_LONG_VALUE);
                long fileCheckSum = getFileCheckSum(writtenFile); //TODO: IOException
                logger.debug("Header checkSum: " + headerCheckSum);
                logger.debug("File checkSum: " + fileCheckSum);
                if(headerCheckSum == fileCheckSum){
                    writeResponse(socket.getOutputStream(), "HTTP/1.1 200 Ok");
                    return;
                }
            }
            String isLast = headers.get(HEADER_IS_LAST);//TODO: Process LAst
            //TODO ret 400
            writeResponse(socket.getOutputStream(), "HTTP/1.1 400 Bed Request");
        } catch (IOException ex) {
            logger.error(ex);
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                logger.error("Socket can not be closed!!!", ex);
            }
        }
    }

    private String readHeaders(BufferedInputStream bis) throws IOException {
        StringBuilder builder = new StringBuilder();
        while (builder.indexOf(PACKAGE_BODY_DELIMETER) == -1 && bis.available() > 0) {
            builder.append((char)bis.read());
        }
        return builder.toString();
    }

    private Map<String, String> parseHeaders(String headersString) {
        Map<String, String> headers = new HashMap<>();
        if(StringUtils.isNotBlank(headersString)) {
            String[] splitHeaders = headersString.split(HEADER_DELIMETER);
            for (String headerStr: splitHeaders) {
                String[] header = headerStr.split(HEADER_CONTENT_DELIMETER);
                if(header.length == 2) {
                    headers.put(header[0], header[1]);
                } else {
                    logger.debug("Invalid header: " + headerStr);
                }
            }
        }
        return headers;
    }

    private Map<String, String> extractHeaders(BufferedInputStream bis) throws IOException {
        String headers = readHeaders(bis);
        return parseHeaders(headers);
    }

    private long getFileCheckSum(File file) throws IOException {
        try(FileInputStream fis = new FileInputStream(file)){
            Checksum cs = new CRC32();
            int in;
            while((in = fis.read()) != -1){
                cs.update(in);
            }
            return cs.getValue();
        }
    }

    private void writeResponse(OutputStream os, String content) throws IOException {
        try(BufferedOutputStream bos = new BufferedOutputStream(os)) {
            for(int cByte : content.getBytes()){
                bos.write(cByte);
            }
        }
    }
}
