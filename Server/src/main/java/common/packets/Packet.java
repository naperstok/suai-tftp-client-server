package common.packets;


import common.codes.OpCode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet {

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
