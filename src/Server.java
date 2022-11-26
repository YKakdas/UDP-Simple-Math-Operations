import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Server {

    private InetAddress clientAddress;
    private int clientPort;

    private final List<Double> operands;
    private final List<Character> operators;
    private final DatagramSocket socket;
    private final int ACKNOWLEDGMENT_CODE = -100;
    private int lengthOfExpression;
    private int numberOfOperands;

    public static void main(String[] args) throws IOException {
        new Server();
    }

    public Server() throws IOException {
        operands = new ArrayList<>();
        operators = new ArrayList<>();

        socket = new DatagramSocket(8000);

        receiveAcknowledgment();
        receiveLengthOfExpression();
        startReceivingExpression();
    }

    private void receiveAcknowledgment() throws IOException {
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        socket.receive(packet);

        clientAddress = packet.getAddress();
        clientPort = packet.getPort();

        if (Util.byteToInt(packet.getData()) != ACKNOWLEDGMENT_CODE) {
            socket.close();
        } else {
            socket.send(Util.createIntegerPacket(ACKNOWLEDGMENT_CODE, clientAddress, clientPort));
        }
    }

    private void receiveLengthOfExpression() throws IOException {
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        socket.receive(packet);
        lengthOfExpression = Util.byteToInt(packet.getData());
        numberOfOperands = lengthOfExpression / 2 + 1;
    }

    private void startReceivingExpression() throws IOException {
        ArrayList<Boolean> receiveInfo = new ArrayList<>();
        for (int i = 0; i < lengthOfExpression; i++) {
            receiveInfo.add(false);
        }

        while (receiveInfo.contains(false)) {
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            socket.receive(packet);

            byte[] data = packet.getData();
            byte[] orderByte = new byte[4];
            System.arraycopy(data, 0, orderByte, 0, orderByte.length);

            int order = Util.byteToInt(orderByte);

            if (order < numberOfOperands) { // int + double, it is operand
                Map<Integer, Double> map = Util.tryToParseOperand(data);
                double operand = map.get(order);
                operands.add(order, operand);
            } else { // int + char, it is operator
                Map<Integer, Character> map = Util.tryToParseOperator(data);
                char operator = map.get(order);
                operators.add(order - numberOfOperands, operator);
            }

            receiveInfo.set(order, true);
            socket.send(Util.createIntegerPacket(order, clientAddress, clientPort));
        }

        sendResultToClient(Util.evaluateExpression(operands, operators));
        socket.close();
    }

    private void sendResultToClient(double result) throws IOException {
        byte[] sendBuffer = Util.doubleToByte(result);
        socket.send(new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort));
    }
}
