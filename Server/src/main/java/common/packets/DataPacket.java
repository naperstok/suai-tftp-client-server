package common.packets;

import common.codes.OpCode;

import java.io.IOException;

public class DataPacket extends Packet {

    public DataPacket(short blockNumber, byte[] data, int offset, int length) {
        super();
        writeOpcode(OpCode.DATA);

        try {
            dataOutputStream.writeShort(blockNumber);
            dataOutputStream.write(data, offset, length);
            writePayload();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public DataPacket(byte[] data) {
        super(data);
    }

    public short getBlockNumber() {
        return (short)(((payload[2] & 0xFF) << 8) | (payload[3] & 0xFF));
    }

}
