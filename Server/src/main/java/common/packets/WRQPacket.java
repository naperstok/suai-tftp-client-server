package common.packets;


import common.Utils;
import common.codes.OpCode;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WRQPacket extends Packet {
    private static final Logger logger = Logger.getLogger(WRQPacket.class);

    public WRQPacket() {
        super();
        writeOpcode(OpCode.WRQ);
    }

    public WRQPacket(byte[] data) {
        super(data);
    }

    public void writeFilename(String filename) {
        try {
            dataOutputStream.write(filename.getBytes(StandardCharsets.US_ASCII));
            dataOutputStream.write(0x0);
            writePayload();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

    public String getFilename() throws IOException {
        return Utils.charArrayToString(this.payload, 2);
    }

}
