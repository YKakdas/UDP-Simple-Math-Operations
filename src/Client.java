import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Client {

    private final DatagramSocket socket;
    private List<Double> operands;
    private List<Character> operators;

    private final int ACKNOWLEDGMENT_CODE = -100;
    private final InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
    private final int serverPort = 8000;

    public static void main(String[] args) throws IOException {
        new Client();
    }

    public Client() throws IOException {
        socket = new DatagramSocket();

        sendAcknowledgment();
        receiveEchoAcknowledgment();

        sendLengthOfTheExpression();
        startSendingExpression();

    }

    private void sendLengthOfTheExpression() throws IOException {
        socket.send(Util.createIntegerPacket(operands.size() + operators.size(), serverAddress, serverPort));
    }

    private void receiveEchoAcknowledgment() throws IOException {
        byte[] receiveBuffer = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        socket.receive(receivePacket);

        if (Util.byteToInt(receivePacket.getData()) != ACKNOWLEDGMENT_CODE) {
            socket.close();
        } else {
            promptUserInput();
        }
    }

    private void promptUserInput() {
        System.out.println("""
                Please enter the mathematical expression in a single line.
                You may enter the expression of any length with any number of operands.
                Supported operators are multiplication(*), division(/), addition(+), subtraction(-)
                Example query is: 12 + 2 * -5 + 3 / 1.5 - 14""");
        String expression = new Scanner(System.in).nextLine();
        expression = expression.replaceAll("\\s", "");
        Map<List<Double>, List<Character>> parse = Util.parseExpression(expression);
        operands = (List<Double>) parse.keySet().toArray()[0];
        operators = parse.get(operands);
    }

    private void startSendingExpression() throws IOException {
        int operandCount = operands.size();

        ArrayList<Boolean> receiveInfo = new ArrayList<>();
        for (int i = 0; i < operands.size() + operators.size(); i++) {
            receiveInfo.add(false);
        }


        while (receiveInfo.contains(false)) {
            int index = receiveInfo.indexOf(false);

            DatagramPacket datagramPacket;
            if (index < operandCount) {
                datagramPacket = Util.createOperandDatagramPacket(index, operands.get(index), serverAddress, serverPort);
            } else {
                datagramPacket = Util.createOperatorDatagramPacket(index, operators.get(index - operandCount),
                        serverAddress, serverPort);
            }

            socket.send(datagramPacket);

            byte[] receiveBuffer = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            receiveInfo.set(Util.byteToInt(receivePacket.getData()), true);
        }

        double result = receiveResult();

        System.out.println("----------------------------------------------->");
        System.out.println("Result of the calculation is " + result);
    }

    private double receiveResult() throws IOException {
        byte[] receiveBuffer = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        socket.receive(receivePacket);
        socket.close();
        return Util.byteToDouble(receiveBuffer);
    }

    private void sendAcknowledgment() throws IOException {
        socket.send(Util.createIntegerPacket(ACKNOWLEDGMENT_CODE, serverAddress, serverPort));
    }
}
