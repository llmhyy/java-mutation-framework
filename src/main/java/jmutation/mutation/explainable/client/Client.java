package jmutation.mutation.explainable.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Client {

    /**
     * Message to terminate the server
     */
    protected static final String END_MSG = "END";

    /**
     * MSG_BREAK appear at the end of each message
     */
    protected static final String BREAK_MSG = "BREAK";

    /**
     * Sleep time between sending message
     */
    protected static final int SLEEP_TIME = 100;

    /**
     * Buffer size of sending and receiving message
     */
    protected static final int BUFFER_SIZE = (int) Math.pow(2, 20);

    /**
     * Encoding method for string
     */
    protected static final Charset charset = StandardCharsets.UTF_8;

    /**
     * Host of the server
     */
    protected final String HOST;

    /**
     * Port of connection
     */
    protected final int PORT;

    protected Socket socket;
    private DataInputStream reader;
    private DataOutputStream writer;

    public Client(final String host, final int port) {
        this.HOST = host;
        this.PORT = port;
    }

    /**
     * Connect to the server
     */
    public void connectServer() {
        try {
            this.socket = new Socket(this.HOST, this.PORT);
            this.reader = new DataInputStream(this.socket.getInputStream());
            this.writer = new DataOutputStream(this.socket.getOutputStream());
        } catch (UnknownHostException e) {
            this.socket = null;
            this.reader = null;
            this.writer = null;
            System.out.println("Error: UnknowHostException encountered");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnect the server
     */
    public void disconnectServer() {
        try {
            this.reader.close();
            this.writer.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.reader = null;
            this.writer = null;
            this.socket = null;
        }
    }

    /**
     * Terminate the server
     *
     * @return True when successfully end the server
     * @throws Exception
     */
    public boolean endServer() throws Exception {
        byte[] ending_msg = this.strToByte(END_MSG);
        this.sendMsg(ending_msg);
        String response = this.byteToStr(this.receiveMsg());
        return response == Client.END_MSG;
    }

    /**
     * Send message to server
     *
     * @param messages Messages to send
     */
    protected void sendMsg(byte[]... messages) {
        if (this.isReady()) {
            final byte[] msgBreak = this.strToByte(Client.BREAK_MSG);
            try {
                for (byte[] message : messages) {
                    System.out.println("Message size: " + message.length);
                    this.writer.write(message);
                    Thread.sleep(Client.SLEEP_TIME);
                    this.writer.write(msgBreak);
                }
            } catch (IOException e) {
                System.out.println("Error: Fail to send data from model server.");
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Socket is not ready");
        }
    }

    /**
     * Receive message from server
     *
     * @return Message from server
     */
    protected byte[] receiveMsg() {
        try {
            byte[] response = new byte[Client.BUFFER_SIZE];
            int integer = this.reader.read(response);
            if (integer == -1) {
                throw new RuntimeException("No response from server");
            }
            return response;
        } catch (IOException e) {
            System.out.println("Error: Fail to receive data from model server.");
            e.printStackTrace();
        }
        return null;
    }

    protected byte[] strToByte(final String str) {
        return str.getBytes(Client.charset);
    }

    protected String byteToStr(final byte[] bytes) {
        return new String(bytes, Client.charset);
    }

    protected boolean isReady() {
        return this.socket != null && this.writer != null && this.reader != null;
    }
}
