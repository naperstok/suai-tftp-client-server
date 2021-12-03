package server;

import common.Constants;
import common.ServerUDP;
import common.codes.ErrorCode;
import common.packets.ACKpacket;
import common.packets.DataPacket;
import common.packets.ErrorPacket;
import common.packets.RRQpacket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class RRQ {

    public static void handleOperation(ServerUDP server, DatagramPacket clientPacket) {
        DatagramSocket socket = server.getSocket();
        byte[] recvBuf = server.getBuffer();
        RRQpacket rrqPacket = new RRQpacket(clientPacket.getData());

        String filename = null;
        filename = getFileName(rrqPacket);

        if (filename == null)
            System.exit(1);

        File file = new File(server.getRootDir(), filename);
        FileInputStream fileInputStream = null;

        sendReport("User want to download file named: " + filename + "\n");
        fileInputStream = responsePacket(file, socket, clientPacket);

        if (fileInputStream == null)
            return;

//        try {
//            sendReport("User want to download file named: " + filename + "\n");
//            fileInputStream = new FileInputStream(file);
//        } catch (FileNotFoundException ex) {
//            sendReport("File not found\n");
//            System.out.println("File not found!");
//            ErrorPacket errorPacket = new ErrorPacket(ErrorCode.FILE_NOT_FOUND, "File not found!");
//            DatagramPacket response = new DatagramPacket(errorPacket.getPayload(), errorPacket.getPayload().length, clientPacket.getSocketAddress());
//
//            try {
//                socket.send(response);
//            } catch (IOException exc) {
//                sendReport("Failed to send response packet!\n");
//                System.out.println("Failed to send response packet!");
//                exc.printStackTrace();
//            }
//        }
        short blockNum = 1;
        int tries = 5;
        byte[] fileBuf = new byte[Constants.BLOCK_SIZE];

        try {
            socket.setSoTimeout(Constants.BASE_TIMEOUT);
            int bytesRead = fileInputStream.read(fileBuf);

            while (bytesRead != -1) {
                if (tries == 0) {
                    sendReport("Max transmission attempts reached. File transfer failed.\n");
                    System.out.println("Max transmission attempts reached. File transfer failed.");
                    socket.setSoTimeout(Constants.BASE_TIMEOUT);
                    fileInputStream.close();
                    return;
                }

                sendPackets(socket, clientPacket, blockNum, fileBuf, bytesRead);

//                DataPacket dataPacket = new DataPacket(blockNum, fileBuf, 0, bytesRead);
//                DatagramPacket outgoingPacket = new DatagramPacket(dataPacket.getPayload(), dataPacket.getPayload().length, clientPacket.getSocketAddress());
//                socket.send(outgoingPacket);
//                sendReport("Sent " + (outgoingPacket.getLength() - 4) + " bytes\n");
//                System.out.println("Sent " + (outgoingPacket.getLength() - 4) + " bytes");
//
//                sendReport("\nWaiting for client's ACK for block " + blockNum);
//                System.out.println("Waiting for client's ACK for block " + blockNum);
                try {
                    DatagramPacket incomingPacket = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(incomingPacket);
                    ACKpacket ackPacket = new ACKpacket(incomingPacket.getData());
                    sendReport("ACK received for block " + ackPacket.getBlockNumber() + "\n");
                    System.out.println("ACK received for block " + ackPacket.getBlockNumber());

                    bytesRead = fileInputStream.read(fileBuf);
                    blockNum = (short) ((blockNum == Short.MAX_VALUE) ? 0 : blockNum + 1);

                    tries = 5;
                    socket.setSoTimeout(Constants.BASE_TIMEOUT);
                } catch (SocketTimeoutException ex) {
                    tries--;
                    sendReport("No ACK received for block " + blockNum + ". " + tries + " tries remaining.\n");
                    System.out.println("No ACK received for block " + blockNum + ". " + tries + " tries remaining.");
                    socket.setSoTimeout(socket.getSoTimeout() + 1000);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        sendReport("Transfer of " + filename + "complete.\n");
        System.out.println("Transfer of " + filename + "complete.");

        try {
            fileInputStream.close();
        } catch (IOException ex) {
            sendReport("Failed to close file input stream!\n");
            System.out.println("Failed to close file input stream!");
            ex.printStackTrace();
        }
    }

    public static boolean sendReport(String report) {
        return LogWriter.writeEvent(report);
    }

    public static String getFileName(RRQpacket rrqPacket){

        try {
            return rrqPacket.getFilename();
        } catch (IOException ex) {
            sendReport("Failed to deserialize file name\n");
            System.out.println("Failed to deserialize file name!");
            ex.printStackTrace();
        }
        return null;
    }

    public static FileInputStream responsePacket(File file, DatagramSocket socket, DatagramPacket clientPacket){
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return fileInputStream;
        } catch (FileNotFoundException ex) {
            sendReport("File not found\n");
            System.out.println("File not found!");
            ErrorPacket errorPacket = new ErrorPacket(ErrorCode.FILE_NOT_FOUND, "File not found!");
            DatagramPacket response = new DatagramPacket(errorPacket.getPayload(), errorPacket.getPayload().length, clientPacket.getSocketAddress());

            try {
                socket.send(response);
            } catch (IOException exc) {
                sendReport("Failed to send response packet!\n");
                System.out.println("Failed to send response packet!");
                exc.printStackTrace();
            }
        }
        return null;
    }

    public static void sendPackets(DatagramSocket socket, DatagramPacket clientPacket, short blockNum, byte[] fileBuf, int bytesRead) throws IOException {

        DataPacket dataPacket = new DataPacket(blockNum, fileBuf, 0, bytesRead);
        DatagramPacket outgoingPacket = new DatagramPacket(dataPacket.getPayload(), dataPacket.getPayload().length, clientPacket.getSocketAddress());
        socket.send(outgoingPacket);
        sendReport("Sent " + (outgoingPacket.getLength() - 4) + " bytes\n");
        System.out.println("Sent " + (outgoingPacket.getLength() - 4) + " bytes");

        sendReport("\nWaiting for client's ACK for block " + blockNum);
        System.out.println("Waiting for client's ACK for block " + blockNum);
    }

//    public static getResponsePackets (DatagramSocket socket, byte[] recvBuf, int bytesRead, ) throws IOException {
//        try {
//            DatagramPacket incomingPacket = new DatagramPacket(recvBuf, recvBuf.length);
//            socket.receive(incomingPacket);
//            ACKpacket ackPacket = new ACKpacket(incomingPacket.getData());
//            sendReport("ACK received for block " + ackPacket.getBlockNumber() + "\n");
//            System.out.println("ACK received for block " + ackPacket.getBlockNumber());
//
//            bytesRead = fileInputStream.read(fileBuf);
//            blockNum = (short) ((blockNum == Short.MAX_VALUE) ? 0 : blockNum + 1);
//
//            tries = 5;
//            socket.setSoTimeout(Constants.BASE_TIMEOUT);
//        } catch (SocketTimeoutException ex) {
//            tries--;
//            sendReport("No ACK received for block " + blockNum + ". " + tries + " tries remaining.\n");
//            System.out.println("No ACK received for block " + blockNum + ". " + tries + " tries remaining.");
//            socket.setSoTimeout(socket.getSoTimeout() + 1000);
//        }
//    }

}
