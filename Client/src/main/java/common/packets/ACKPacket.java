package common.packets;

import common.codes.OpCode;

import java.io.IOException;

public class ACKPacket extends Packet {
    public ACKPacket(short blockNum) {
        super();
        writeOpcode(OpCode.ACK);

        try {
            dataOutputStream.writeShort(blockNum);
            writePayload();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public ACKPacket(byte[] data) {
        super(data);
    }

    public short getBlockNumber() {
        return (short)(((payload[2] & 0xFF) << 8) | (payload[3] & 0xFF));
    }

}
