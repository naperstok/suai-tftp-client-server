package common.packets;
import common.codes.OpCode;
import org.apache.log4j.Logger;
import server.Server;

import java.io.IOException;

/**
 * Represents a Data Packet
 */
public class DataPacket extends Packet {
    private static final Logger logger = Logger.getLogger(DataPacket.class);

    /**
     * Create a Data Packet
     * @param blockNumber the block number
     * @param data byte array
     * @param offset offset
     * @param length bytes Read
     */
    public DataPacket(short blockNumber, byte[] data, int offset, int length) {
        super();
        writeOpcode(OpCode.DATA);

        try {
            dataOutputStream.writeShort(blockNumber);
            dataOutputStream.write(data, offset, length);
            writePayload();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Create a Data Packet from a pre-existing byte array
     * @param data that pre-existing byte array
     */
    public DataPacket(byte[] data) {
        super(data);
    }

    public short getBlockNumber() {
        return (short)(((payload[2] & 0xFF) << 8) | (payload[3] & 0xFF));
    }

}
