import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

public class Util {

    public static Map<List<Double>, List<Character>> parseExpression(String expression) {
        StringTokenizer st = new StringTokenizer(expression, "+-*/", true);
        String previousToken = null;

        List<Double> operandList = new ArrayList<>();
        List<Character> operatorList = new ArrayList<>();

        boolean makeNextOperandNegative = false;

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            if ("+-/*".contains(token)) {
                if (previousToken == null && token.equals("-")) {
                    makeNextOperandNegative = true;
                } else if (previousToken != null && "+-/*".contains(previousToken)) {
                    makeNextOperandNegative = true;
                } else {
                    operatorList.add(token.charAt(0));
                }
            } else {
                double operand = Double.parseDouble(token);
                if (makeNextOperandNegative) {
                    operand *= -1;
                }
                operandList.add(operand);
                makeNextOperandNegative = false;
            }
            previousToken = token;
        }
        HashMap<List<Double>, List<Character>> map = new HashMap<>();
        map.put(operandList, operatorList);
        return map;
    }

    public static DatagramPacket createOperandDatagramPacket(int order, double operand,
                                                             InetAddress address, int port) throws IOException {
        byte[] orderByte = intToByte(order);
        byte[] operandByte = doubleToByte(operand);

        byte[] join = Arrays.copyOf(orderByte, orderByte.length + operandByte.length);
        System.arraycopy(operandByte, 0, join, orderByte.length, operandByte.length);

        return new DatagramPacket(join, join.length, address, port);
    }

    public static DatagramPacket createOperatorDatagramPacket(int order, char operator,
                                                              InetAddress address, int port) throws IOException {
        byte[] orderByte = intToByte(order);
        byte[] operatorByte = new byte[]{(byte) operator};


        byte[] join = Arrays.copyOf(orderByte, orderByte.length + operatorByte.length);
        System.arraycopy(operatorByte, 0, join, orderByte.length, operatorByte.length);

        return new DatagramPacket(join, join.length, address, port);
    }

    public static DatagramPacket createIntegerPacket(int num, InetAddress address, int port) throws IOException {
        byte[] sendBuffer = intToByte(num);
        return new DatagramPacket(sendBuffer, sendBuffer.length, address, port);
    }

    public static int byteToInt(byte[] data) {
        return ByteBuffer.wrap(data).getInt();
    }

    public static double byteToDouble(byte[] data) {
        return ByteBuffer.wrap(data).getDouble();
    }

    public static byte[] intToByte(int num) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeInt(num);
        return out.toByteArray();
    }

    public static byte[] doubleToByte(double num) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeDouble(num);
        return out.toByteArray();
    }

    public static Map<Integer, Double> tryToParseOperand(byte[] data) {
        try {
            // First, parse the order which is integer
            byte[] order = new byte[4];
            byte[] operand = new byte[8];
            System.arraycopy(data, 0, order, 0, order.length);
            System.arraycopy(data, order.length, operand, 0, operand.length);

            int order_int = byteToInt(order);
            double operand_double = byteToDouble(operand);

            Map<Integer, Double> map = new HashMap<>();
            map.put(order_int, operand_double);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<Integer, Character> tryToParseOperator(byte[] data) {
        try {
            // First, parse the order which is integer
            byte[] order = new byte[4];

            System.arraycopy(data, 0, order, 0, order.length);

            int order_int = byteToInt(order);
            char operator = (char) data[4];

            Map<Integer, Character> map = new HashMap<>();
            map.put(order_int, operator);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double evaluateExpression(List<Double> operands, List<Character> operators) {
        while (operators.size() > 0) {
            List<Integer> indices = findAllOccurrencesOfSamePrecedence('*', '/', operators);
            calculate(indices, operands, operators);

            indices = findAllOccurrencesOfSamePrecedence('+', '-', operators);
            calculate(indices, operands, operators);
        }
        return operands.get(0);
    }

    private static List<Integer> findAllOccurrencesOfSamePrecedence(char op1, char op2, List<Character> operators) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < operators.size(); i++) {
            if (operators.get(i) == op1 || operators.get(i) == op2) {
                indices.add(i);
            }
        }
        return indices;
    }

    private static void calculate(List<Integer> indices, List<Double> operands, List<Character> operators) {
        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i) - i;
            char operator = operators.get(index);
            double result = switch (operator) {
                case '*' -> operands.get(index) * operands.get(index + 1);
                case '/' -> operands.get(index) / operands.get(index + 1);
                case '-' -> operands.get(index) - operands.get(index + 1);
                case '+' -> operands.get(index) + operands.get(index + 1);
                default -> 0;
            };
            operands.remove(index + 1);
            operands.remove(index);
            operands.add(index, result);
            operators.remove(index);
        }
    }

}
