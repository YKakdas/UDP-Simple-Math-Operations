# UDP-Simple-Math-Operations

This project utilizes UDP Client & Server system.

-   The client takes any number of double-operands with supported mathematical operations(`multiplication(*)`, `division(/)`, `subtraction(-)`, `addition(+)`) and sends it to the server. The client first sends operands as a list then operators as a list.
-   The server first receives operands then operators and calculates the given statement. Then, sends the result back to the client.

## Example Mathematical Query

```
12 + 2 * -5 + 3 / 1.5 - 14
```
