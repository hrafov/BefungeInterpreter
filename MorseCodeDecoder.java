import java.util.Stack;

public class MorseCodeDecoder {
    // https://www.codewars.com/kata/526c7b931666d07889000a3c/train/java - 4
    // online simulator - https://befunge.flogisoft.com/
    public String interpret(String code) {
        System.out.println(code);
        System.out.println();
        Stack<Integer> stack = new Stack<>();
        StringBuilder output = new StringBuilder();

        String[] stringsOfCode = code.split("\n");
        PointXY[] points = new PointXY[code.length()];
        int count = 0;
        for (int rows = 0; rows < stringsOfCode.length; rows++)
            for (int columns = 0; columns < stringsOfCode[rows].length(); columns++)
                points[count++] = new PointXY(stringsOfCode[rows].charAt(columns), columns, rows);

        PointXY current = findCurrentPoint(points, 0, 0);
        String direction = "right";
        int a, b, x, y, v;

        while (current.getC() != '@') {

            switch (current.getC()) {
                //0-9 Push this number onto the stack.
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9':
                    stack.push(current.getC() - '0');
                    break;
                // + Addition: Pop a and b, then push a+b.
                case '+':
                    if (!stack.isEmpty())
                        v = stack.pop();
                    else break;
                    if (!stack.isEmpty())
                        stack.push(v + stack.pop());
                    else stack.push(v);
                    break;
                // - Subtraction: Pop a and b, then push b-a.
                case '-':
                    a = stack.pop();
                    b = stack.pop();
                    stack.push(b - a);
                    break;
                // * Multiplication: Pop a and b, then push a*b.
                case '*':
                    a = stack.pop();
                    b = stack.pop();
                    stack.push(a * b);
                    break;
                // Integer division: Pop a and b, then push b/a rounded down. If a is zero, push zero.
                case '/':
                    a = stack.pop();
                    b = stack.pop();
                    if (a == 0) stack.push(0);
                    //else stack.push(Integer.parseInt(String.valueOf((char) b)) / Integer.parseInt(String.valueOf((char) a)));
                    else stack.push(((char) b - '0') / (((char) a) - '0'));
                    break;
                // % Modulo: Pop a and b, then push the b%a. If a is zero, push zero.
                case '%':
                    a = stack.pop();
                    b = stack.pop();
                    if (a == 0) stack.push(0);
                    //else stack.push(Integer.parseInt(String.valueOf((char) b)) % Integer.parseInt(String.valueOf((char) a)));
                    else stack.push(((char) b - '0') % (((char) a) - '0'));
                    break;
                // ! Logical NOT: Pop a value. If the value is zero, push 1; otherwise, push zero.
                case '!':
                    stack.push(stack.pop() == 0 ? 1 : 0);
                    break;
                // ` (backtick) Greater than: Pop a and b, then push 1 if b>a, otherwise push zero.
                case '`':
                    a = stack.pop();
                    b = stack.pop();
                    stack.push(b > a ? 1 : 0);
                    break;
                case '>':
                    direction = "right";
                    break;
                case '<':
                    direction = "left";
                    break;
                case '^':
                    direction = "up";
                    break;
                case 'v':
                    direction = "down";
                    break;
                // ? Start moving in a random cardinal direction.
                case '?':
                    direction = randomDirection();
                    break;
                // _ Pop a value; move right if value = 0, left otherwise.
                case '_':
                    if (stack.pop() == 0) direction = "right";
                    else direction = "left";
                    break;
                // | Pop a value; move down if value = 0, up otherwise.
                case '|':
                    if (stack.pop() == 0) direction = "down";
                    else direction = "up";
                    break;
                // " Start string mode: push each character's ASCII value all the way up to the next ".
                case '"':
                    current = findNextCurrent(points, direction, current);
                    while (!String.valueOf(current.getC()).equals("\"")) {
                        stack.push((int) current.getC());
                        current = findNextCurrent(points, direction, current);
                    }
                    break;
                // ':' Duplicate value on top of the stack. If there is nothing on top of the stack, push a 0.
                case ':':
                    if (stack.isEmpty()) stack.push(0);
                    else stack.push(stack.peek());
                    break;
                // \ Swap two values on top of the stack. If there is only one value, pretend there is an extra 0 on bottom of the stack.
                case '\\':
                    a = stack.pop();
                    if (stack.isEmpty()) b = 0;
                    else b = stack.pop();
                    stack.push(a);
                    stack.push(b);
                    break;
                // $ Pop value from the stack and discard it.
                case '$':
                    stack.pop();
                    break;
                // . Pop value and output as an integer.
                case '.':
                    output.append(stack.pop());
                    break;
                // , Pop value and output the ASCII character represented by the integer code that is stored in the value.
                case ',':
                    output.append(fromStackAsInASCCIITable(stack));
                    break;
                // # Trampoline: Skip next cell.
                case '#':
                    current = findNextCurrent(points, direction, current);
                    break;
                // p A "put" call (a way to store a value for later use).
                // Pop y, x and v,
                // then change the character at the position (x,y) in the program to the character with ASCII value v.
                case 'p':
                    y = stack.pop();
                    x = stack.pop();
                    v = stack.pop();
                    findCurrentPoint(points, x, y).setC((char) v);
                    break;
                // g A "get" call (a way to retrieve data in storage).
                // Pop y and x, then push ASCII value of the character at that position in the program.
                case 'g':
                    b = stack.pop();
                    a = stack.pop();
                    stack.push(Integer.parseInt(String.valueOf((int) findCurrentPoint(points, a, b).getC())));
                    break;
                //  (i.e. a space) No-op. Does nothing.
                case ' ':
                    break;
                default:
                    System.out.println("Error: not correct command");
                    break;
            }
            current = findNextCurrent(points, direction, current);
        }
        return output.toString();
    }

    private static String fromStackAsInASCCIITable(Stack<Integer> stack) {
        if (stack.peek() == 9) {
            stack.pop();
            return "\t";
        } else if (stack.peek() == 10) {
            stack.pop();
            return "\n";
        } else if (stack.peek() == 11) {
            stack.pop();
            return " ";
        } else if (stack.peek() == 12) {
            stack.pop();
            return "\f";
        } else if (stack.peek() == 13) {
            stack.pop();
            return "\r";
        } else {
            return String.valueOf((char) (int) stack.pop());
        }
    }

    private static String randomDirection() {
        String[] directions = {"left", "right", "up", "down"};
        return directions[(int) (Math.random() * directions.length)];
    }

    private static PointXY findNextCurrent(PointXY[] points, String direction, PointXY current) {
        if (direction.equals("left")) current = findCurrentPoint(points, current.getX() - 1, current.getY());
        else if (direction.equals("right")) current = findCurrentPoint(points, current.getX() + 1, current.getY());
        else if (direction.equals("up")) current = findCurrentPoint(points, current.getX(), current.getY() - 1);
        else if (direction.equals("down")) current = findCurrentPoint(points, current.getX(), current.getY() + 1);
        else System.out.println("Error: unknown direction");
        return current;
    }

    private static PointXY findCurrentPoint(PointXY[] points, int x, int y) {
        for (PointXY point : points)
            if (point.getX() == x && point.getY() == y) return point;
        return new PointXY('@', 999, 999);
    }

    private static class PointXY {
        private char c;
        private final int x;
        private final int y;
        public PointXY(char c, int x, int y) {
            this.c = c;
            this.x = x;
            this.y = y;
        }
        // return string representation of this point
        public String toString() {
            return "char " + c + " (" + x + ", " + y + ")";
        }
        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }
        public char getC() {
            return c;
        }
        public void setC(char c) {
            this.c = c;
        }
    }

    public static void main(String[] args) {
        //tests
//      "123456789"
        System.out.println(new MorseCodeDecoder().interpret(">987v>.v\nv456<  :\n>321 ^ _@"));

//      "Hello World! with \n
        System.out.println(new MorseCodeDecoder().interpret(">25*\"!dlroW olleH\":v\n" +
                                                                  "                v:,_@\n" +
                                                                  "                >  ^"));

//      40320 // it's 8!
        System.out.println(new MorseCodeDecoder().interpret( "08>:1-:v v *_$.@ \n" +
                                                                   "  ^    _$>\\:^"));

//      01->1# +# :# 0# g# ,# :# 5# 8# *# 4# +# -# _@
        System.out.println(new MorseCodeDecoder().interpret("01->1# +# :# 0# g# ,# :# 5# 8# *# 4# +# -# _@"));

//      23571113171923293137 // prime numbers
        System.out.println(new MorseCodeDecoder().interpret("2>:3g\" \"-!v\\  g30          <\n" +
                " |!`\"&\":+1_:.:03p>03g+:\"&\"`|\n" +
                " @               ^  p3\\\" \":<\n" +
                "2 2345678901234567890123456789012345678"));
    }
}
