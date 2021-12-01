package common.packets;


import common.Utils;
import common.codes.ErrorCode;
import common.codes.OpCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ErrorPacket extends Packet {

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
            ex.printStackTrace();
        }
    }

    public ErrorPacket(byte[] data) {
        super(data);
    }

    public String getErrorMessage() throws IOException {
        return Utils.charArrayToString(this.payload, 4);
    }

    public ErrorCode getErrorCode() {
        return ErrorCode.intToECMappings[this.payload[3]];
    }

}