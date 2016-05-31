package by.honchar.hserver.io;

import java.io.*;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public final class IOUtils {

    private static final String PROJECT_FOLDER_PATH = "HServer";
    private static final String PART_FILE_EXTENSION = ".part";
    private static final String TMP_FOLDER_PATH = PROJECT_FOLDER_PATH + File.separator + "tmp";


    private IOUtils() {}

    public static void reCreateTmpDir() {
        File tmpDir = new File(TMP_FOLDER_PATH);
        if(!tmpDir.mkdir()) {
            clearFolder(tmpDir);
        }
    }

    public static boolean clearFolder(File folder) {
        boolean isSuccess = folder != null && folder.exists() && folder.isDirectory();
        if(isSuccess) {
            File[] files = folder.listFiles();
            if(files != null) {
                for(File file: files) {
                    if(file.isDirectory()) {
                        clearFolder(file);
                    } else {
                        isSuccess = file.delete() && isSuccess;
                    }
                }
            }
        }
        return isSuccess;
    }

    public static File writePartToFile(String fileName, String partNumber, BufferedInputStream bis) throws IOException {
        String shaFileName = IOUtils.sha256(fileName);
        String tmpFileName = shaFileName + "_" + partNumber + PART_FILE_EXTENSION;
        File tmpDir = new File(TMP_FOLDER_PATH + File.separator + shaFileName);
        tmpDir.mkdir();
        File partFile = new File(tmpDir, tmpFileName);
        IOUtils.writeToFile(partFile, bis);
        return partFile;
    }

    public static void deletePartFile(String fileName, String partNumber) throws IOException {
        String shaFileName = IOUtils.sha256(fileName);
        String tmpFileName = shaFileName + "_" + partNumber + PART_FILE_EXTENSION;
        File tmpDir = new File(TMP_FOLDER_PATH + File.separator + shaFileName);
        File partFile = new File(tmpDir, tmpFileName);
        partFile.delete();
        String[] folderContent = tmpDir.list();
        if(folderContent == null || folderContent.length == 0) {
            tmpDir.delete();
        }
    }

    public static void writeToFile(File file, BufferedInputStream bis) throws IOException {
        try(FileOutputStream fos = new FileOutputStream(file)) {
            int i;
            while(bis.available() > 0 && (i = bis.read()) != -1) {
                fos.write(i);
            }
            fos.flush();
        }
    }

    public static String sha256(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(value.getBytes());
            return hashToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            return value;
        }
    }

    public static String calculateFileCheckSum(File file) throws IOException {
        return IOUtils.calculateCheckSumAdnCloseIS(new FileInputStream(file));
    }

    public static String calculateClassPathFileCheckSum(String filePath) throws IOException {
        return IOUtils.calculateCheckSumAdnCloseIS(ClassPathFileLoader.loadFileInputStream(filePath));
    }

    public static String calculateCheckSumAdnCloseIS(InputStream is) throws IOException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            byte[] buffer = new byte[1024];
            int i;
            while((i = is.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, i);
            }
            return hashToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } finally {
            is.close();
        }
    }

    private static String hashToString(byte[] digest) {
        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);
        while(md5Hex.length() < 32){
            md5Hex = "0" + md5Hex;
        }
        return md5Hex;
    }
}
