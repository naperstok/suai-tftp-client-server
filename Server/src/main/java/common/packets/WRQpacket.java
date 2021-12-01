package common.packets;


import common.Utils;
import common.codes.OpCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WRQpacket extends Packet {

    public WRQpacket() {
        super();
        writeOpcode(OpCode.WRQ);
    }

    public WRQpacket(byte[] data) {
        super(data);
    }

    public void writeFilename(String filename) {
        try {
            dataOutputStream.write(filename.getBytes(StandardCharsets.US_ASCII));
            dataOutputStream.write(0x0);
            writePayload();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public String getFilename() throws IOException {
        return Utils.charArrayToString(this.payload, 2);
    }

}
