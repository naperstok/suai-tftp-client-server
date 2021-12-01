package common.codes;

public enum OpCode {

    RRQ(1),     // Read request -- запрос на чтение файла
    WRQ(2),     // Write request -- запрос на запись файла
    DATA(3),    // Data --данные, передаваемые через TFTP
    ACK(4),     // Acknowledgement -- подтверждение пакета
    ERROR(5),   // Error -- ошибка
    OACK(6);    // Option acknowledgement -- подтверждение опций

    public final int op;
    public static OpCode[] mapping = new OpCode[] {RRQ, WRQ, DATA, ACK, ERROR, OACK};

    OpCode(int op) {
        this.op = op;
    }
}
