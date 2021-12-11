package common.packets;

import common.codes.OpCode;
import org.apache.log4j.Logger;
import server.Server;

import java.io.IOException;

/**
 * Represents an Acknowledgement Packet
 */
public class ACKPacket extends Packet {
    private static final Logger logger = Logger.getLogger(ACKPacket.class);

    /**
     * Creates an ACK Packet
     * @param blockNum the block number
     */
    public ACKPacket(short blockNum) {
        super();
        writeOpcode(OpCode.ACK);

        try {
            dataOutputStream.writeShort(blockNum);
            writePayload();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Create an ACK Packet from a pre-existing byte array
     * @param data that pre-existing byte array
     */
    public ACKPacket(byte[] data) {
        super(data);
    }

    /**
     * Get the block number for the ACK packet
     * @return the block number
     */
    public short getBlockNumber() {
        return (short)(((payload[2] & 0xFF) << 8) | (payload[3] & 0xFF));
    }

}
