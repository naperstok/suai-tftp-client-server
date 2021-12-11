package common.packets;

import common.codes.OpCode;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet {

    private static final Logger logger = Logger.getLogger(Packet.class);
    protected byte[] payload;
    protected final ByteArrayOutputStream byteArrayOutputStream;
    protected final DataOutputStream dataOutputStream;

    public Packet() {
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.dataOutputStream = new DataOutputStream(this.byteArrayOutputStream);
    }

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

    // Запись кода операции пакета в пэйлоад массив байтов
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

    public OpCode getOpcode() {
        return OpCode.mapping[this.payload[1] - 1];
    }

    protected void writePayload() throws IOException {
        this.dataOutputStream.flush();
        this.payload = byteArrayOutputStream.toByteArray();
    }

    public byte[] getPayload() {
        return this.payload;
    }
}
