package common.packets;
import common.Utils;
import common.codes.ErrorCode;
import common.codes.OpCode;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Represents an Error Packet
 */
public class ErrorPacket extends Packet {
    private static final Logger logger = Logger.getLogger(ErrorPacket.class);

    /**
     * Creates an Error Packet
     * @param errorCode error code
     * @param errorMessage error code message
     */
    public ErrorPacket(ErrorCode errorCode, String errorMessage) {
        super();
        writeOpcode(OpCode.ERROR);
        try {
            dataOutputStream.writeByte(0x0);
            dataOutputStream.writeByte(ErrorCode.ecToIntMappings.get(errorCode));
            dataOutputStream.write(errorMessage.getBytes(StandardCharsets.US_ASCII));
            dataOutputStream.writeByte(0x0);
            writePayload();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Creates an Error Packet from a pre-existing byte array
     * @param data The pre-existing byte array
     */
    public ErrorPacket(byte[] data) {
        super(data);
    }

    /**
     * Get the error message for the error packet
     * @return the error message
     * @throws IOException if an error occurs
     */
    public String getErrorMessage() throws IOException {
        return Utils.charArrayToString(this.payload, 4);
    }

    /**
     * Get the error code
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return ErrorCode.intToECMappings[this.payload[3]];
    }

}
