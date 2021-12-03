package common.packets;


import common.Utils;
import common.codes.OpCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RRQPacket extends Packet {

    public RRQPacket() {
        super();
        writeOpcode(OpCode.RRQ);
    }


    public RRQPacket(byte[] data) {
        super(data);
    }


    //Запись байтов из файла (в ASCII) в массив байтов
    public void writeFilename(String filename) {
        try {
            dataOutputStream.write(filename.getBytes(StandardCharsets.US_ASCII));
            dataOutputStream.write(0x0);
            writePayload();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getFilename() throws IOException {
        return Utils.charArrayToString(this.payload, 2);
    }

}
