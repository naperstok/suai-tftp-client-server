import common.packets.RRQPacket;
import org.junit.Assert;
import org.junit.Test;
import server.RRQ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class RRQTest {

    @Test
    public void testGetFileName() {
        String fileName = "test.txt";
        byte[] test = {0, 1, 't', 'e', 's', 't', '.', 't', 'x', 't', 0};
        RRQPacket packet = new RRQPacket(test);
        Assert.assertEquals(fileName, RRQ.getFileName(packet));
    }

    @Test
    public void testFileCreation() {
        String fileName = "1.jpg";
        File file = new File("C:\\Users\\User\\Desktop\\Kursovaya\\tftpProject\\Server\\src\\main\\resources", fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(file);
    }

    @Test
    public void testBytesRead() {
        String fileName = "test.txt";
        byte [] fileBuf = new byte[8];
        File file = new File("C:\\Users\\User\\Desktop\\Kursovaya\\tftpProject\\Server\\src\\main\\resources", fileName);
        try {
            int bytesRead = new FileInputStream(file).read(fileBuf);
            Assert.assertNotEquals(0, bytesRead);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFileStream() {
        String fileName = "test.txt";
        File file = new File("C:\\Users\\User\\Desktop\\Kursovaya\\tftpProject\\Server\\src\\main\\resources", fileName);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        Assert.assertNotNull(RRQ.responsePacket(file, null, null));
    }
}