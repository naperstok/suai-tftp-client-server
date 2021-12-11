package common.packets;
import common.Utils;
import common.codes.OpCode;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Represents a Write Request Packet (for WRQ)
 */
public class WRQPacket extends Packet {
    private static final Logger logger = Logger.getLogger(WRQPacket.class);

    /**
     * Creates a Write Request Packet
     */
    public WRQPacket() {
        super();
        writeOpcode(OpCode.WRQ);
    }


    /**
     * Creates a Write Request Packet from a pre-existing byte array
     * @param data that pre-existing byte array
     */
    public WRQPacket(byte[] data) {
        super(data);
    }

    /**
     * Writes the bytes of the filename string to the byte array
     * @param filename the filename
     */
    public void writeFilename(String filename) {
        try {
            dataOutputStream.write(filename.getBytes(StandardCharsets.US_ASCII));
            dataOutputStream.write(0x0);
            writePayload();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
        closeSteams();
    }

    /**
     * Get the filename
     * @return the filename
     * @throws IOException if an error occurs
     */
    public String getFilename() throws IOException {
        return Utils.charArrayToString(this.payload, 2);
    }

}
