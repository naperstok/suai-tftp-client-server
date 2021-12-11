package server;

import common.Constants;
import common.ServerUDP;
import common.codes.ErrorCode;
import common.packets.ACKPacket;
import common.packets.DataPacket;
import common.packets.ErrorPacket;
import common.packets.RRQPacket;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class RRQ {

    private static final Logger logger = Logger.getLogger(RRQ.class);

    public static void handleOperation(ServerUDP server, DatagramPacket clientPacket) throws IOException {
        DatagramSocket socket = server.getSocket();
        byte[] recvBuf = server.getBuffer();
        RRQPacket rrqPacket = new RRQPacket(clientPacket.getData());

        String filename = null;
        filename = getFileName(rrqPacket);

        if (filename == null)
            logger.fatal("File name equals null");
            System.exit(1);

        File file = new File(server.getRootDir(), filename);
        FileInputStream fileInputStream = null;

        fileInputStream = responsePacket(file, socket, clientPacket);

        if (fileInputStream == null)
            return;

        fileTransfer(socket, fileInputStream, clientPacket, recvBuf);

        logger.info("Transfer of " + filename + " complete.");

        try {
            fileInputStream.close();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

    public static String getFileName(RRQPacket rrqPacket){

        try {
            return rrqPacket.getFilename();
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
        return null;
    }

    public static FileInputStream responsePacket(File file, DatagramSocket socket, DatagramPacket clientPacket){
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return fileInputStream;
        } catch (FileNotFoundException ex) {
            logger.error("File not found!");
            ErrorPacket errorPacket = new ErrorPacket(ErrorCode.FILE_NOT_FOUND, "File not found!");
            DatagramPacket response = new DatagramPacket(errorPacket.getPayload(), errorPacket.getPayload().length, clientPacket.getSocketAddress());

            try {
                socket.send(response);
            } catch (IOException exc) {
                logger.fatal(exc);
                exc.printStackTrace();
            }
        }
        return null;
    }

    public static void sendPackets(DatagramSocket socket, DatagramPacket clientPacket, short blockNum, byte[] fileBuf, int bytesRead) throws IOException {

        DataPacket dataPacket = new DataPacket(blockNum, fileBuf, 0, bytesRead);
        DatagramPacket outgoingPacket = new DatagramPacket(dataPacket.getPayload(), dataPacket.getPayload().length, clientPacket.getSocketAddress());
        socket.send(outgoingPacket);
        logger.info("Sent " + (outgoingPacket.getLength() - 4) + " bytes");
        logger.info("Waiting for client's ACK for block " + blockNum);
    }

    public static void fileTransfer (DatagramSocket socket, FileInputStream fileInputStream, DatagramPacket clientPacket, byte[] recvBuf) throws IOException {

        short blockNum = 1;
        int tries = 5;
        byte[] fileBuf = new byte[Constants.BLOCK_SIZE];

        try {
            socket.setSoTimeout(Constants.BASE_TIMEOUT);
            int bytesRead = fileInputStream.read(fileBuf);

            while (bytesRead != -1) {
                if (tries == 0) {
                    logger.fatal("Max transmission attempts reached. File transfer failed.");
                    socket.setSoTimeout(Constants.BASE_TIMEOUT);
                    fileInputStream.close();
                    System.exit(4);
                    //return;
                }

                sendPackets(socket, clientPacket, blockNum, fileBuf, bytesRead);

                try {
                    DatagramPacket incomingPacket = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(incomingPacket);
                    ACKPacket ackPacket = new ACKPacket(incomingPacket.getData());
                    logger.info("ACK received for block " + ackPacket.getBlockNumber());

                    bytesRead = fileInputStream.read(fileBuf);
                    blockNum = (short) ((blockNum == Short.MAX_VALUE) ? 0 : blockNum + 1);

                    tries = 5;
                    socket.setSoTimeout(Constants.BASE_TIMEOUT);
                } catch (SocketTimeoutException ex) {
                    tries--;
                    logger.error("No ACK received for block " + blockNum + ". " + tries + " tries remaining.");
                    socket.setSoTimeout(socket.getSoTimeout() + 1000);
                }
            }
        } catch (IOException ex) {
            logger.fatal(ex);
            ex.printStackTrace();
        }
    }

}
