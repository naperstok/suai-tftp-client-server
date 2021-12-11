import common.packets.WRQPacket;
import org.junit.Assert;
import org.junit.Test;
import server.WRQ;

import java.io.*;
import java.lang.reflect.Array;

public class WRQTest {

    @Test
    public void testGetFileName() {
        String fileName = "test.txt";
        byte[] test = {1, 1, 't', 'e', 's', 't', '.', 't', 'x', 't', 0};
        WRQPacket packet = new WRQPacket(test);
        Assert.assertNotNull(WRQ.getFilename(packet));
        Assert.assertEquals(fileName, WRQ.getFilename(packet));
    }

    @Test
    public void testWritingFile() {
        String fileName = "testWriting.txt";
        File file = new File("..\\Server\\src\\main\\resources", fileName);
        String testLine = "Hello", resLine;
        ByteArrayOutputStream recvFileBuf = new ByteArrayOutputStream();
        FileInputStream fileInputStream;
        try {
            recvFileBuf.write(testLine.getBytes());
            WRQ.writingFile(file, recvFileBuf);
            fileInputStream = new FileInputStream(file);
            byte[] tmp = new byte[1024];
            int count = fileInputStream.read(tmp);
            byte[] result = new byte[count];
            System.arraycopy(tmp, 0, result, 0, count);
            resLine = new String(result);
            Assert.assertEquals(testLine,resLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}