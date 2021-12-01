package common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Utils {

    public static String charArrayToString(byte[] data, int offset) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        int i = offset;

        while (data[i] != 0x0) {
            dataOutputStream.writeByte(data[i]);
            i++;
        }

        dataOutputStream.flush();
        dataOutputStream.close();
        byteArrayOutputStream.close();

        return byteArrayOutputStream.toString(StandardCharsets.US_ASCII);
    }
}
