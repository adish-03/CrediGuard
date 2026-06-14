import java.util.Scanner;

public class MagicNumber {

    static void checkMagic (int number) {
        int num = number;

        

        while (num > 9) {

            int num1 = num/10;
            int num2 = num%10;

            int sum = num1 + num2;

            if (sum == 1) {
                System.out.println ("The given number "+number+" is a magic number.");
                num = 0;
            }
            else if (sum > 9) {
                num = sum;
                sum = 0;

                num1 = num/10;
                num2 = num%10;
            }
            else {
                System.out.println ("The given number "+number+" is not a magic number.");
                num = 0;
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner (System.in);
        
        System.out.println ("Enter the number to check: ");
        int number = sc.nextInt();

        checkMagic(number);
    }
}