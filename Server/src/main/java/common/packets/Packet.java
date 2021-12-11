package common.packets;
import common.codes.OpCode;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Base class for a packet
 */
public class Packet {

    private static final Logger logger = Logger.getLogger(Packet.class);
    protected byte[] payload;
    protected final ByteArrayOutputStream byteArrayOutputStream;
    protected final DataOutputStream dataOutputStream;

    /**
     * Create a packet
     */
    public Packet() {
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.dataOutputStream = new DataOutputStream(this.byteArrayOutputStream);
    }

    /**
     * Create a packet from a pre-existing byte array
     * @param data that pre-existing byte array
     */
    public Packet(byte[] data) {
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.dataOutputStream = new DataOutputStream(this.byteArrayOutputStream);

        try {
            dataOutputStream.write(data);
            dataOutputStream.flush();
            writePayload();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Write the packet's opcode to the byte array payload
     * @param opcode opcode from the packet
     */
    public void writeOpcode(OpCode opcode) {
        try {
            dataOutputStream.writeByte(0);
            dataOutputStream.writeByte(opcode.op);
            writePayload();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

    /**
     * Get the packet's opcode
     * @return opcode
     */
    public OpCode getOpcode() {
        return OpCode.mapping[this.payload[1] - 1];
    }

    /**
     * Flush the internal byte stream to the byte array payload
     * @throws IOException if an error occurs
     */
    protected void writePayload() throws IOException {
        this.dataOutputStream.flush();
        this.payload = byteArrayOutputStream.toByteArray();
    }

    public void closeSteams() {
        try {
            this.dataOutputStream.close();
            this.byteArrayOutputStream.close();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

    /**
     *  Get the packet's byte array payload
     * @return  the payload
     */
    public byte[] getPayload() {
        return this.payload;
    }
}
