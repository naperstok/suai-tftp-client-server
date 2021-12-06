package common.codes;

import java.util.HashMap;

public enum ErrorCode {

    NOT_DEFINED, //Нет определенного кода, см. текст ошибки
    FILE_NOT_FOUND, //Файл не найден
    ACCESS_VIOLATION, //Доступ запрещен
    DISK_FULL, //Невозможно выделить место на диске
    ILLEGAL_OP, //Некорректная TFTP-операция
    UNKNOWN_TID, //Неправильный Transfer ID
    FILE_ALREADY_EXISTS, //Файл уже существует
    NO_SUCH_USER; //Пользователь не существует

    public static final HashMap<ErrorCode, Integer> ecToIntMappings = new  HashMap<>() {{
        put(NOT_DEFINED, 0);
        put(FILE_NOT_FOUND, 1);
        put(ACCESS_VIOLATION, 2);
        put(DISK_FULL, 3);
        put(ILLEGAL_OP, 4);
        put(UNKNOWN_TID, 5);
        put(FILE_ALREADY_EXISTS, 6);
        put(NO_SUCH_USER, 7);
    }};

    public static ErrorCode[] intToECMappings = new ErrorCode[] { NOT_DEFINED, FILE_NOT_FOUND, ACCESS_VIOLATION, DISK_FULL, ILLEGAL_OP, UNKNOWN_TID, FILE_ALREADY_EXISTS, NO_SUCH_USER };
}
