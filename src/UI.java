import java.util.Scanner;
import java.util.function.Predicate;

public class UI {

    private UI(){}

    public static String getAnswer(String question) {
        Scanner in = new Scanner(System.in);
        System.out.println(question);

        return in.next();
    }

    public static String getAnswerWithTest (String question, Predicate<String> predicate){
        Scanner in = new Scanner(System.in);
        System.out.println(question);
        while (true) {
            String ans = in.next();
            if (predicate.test(ans)) return ans;
        }
    }

    public static String getAnswerWithTest (String question, Predicate<String> predicate, String errorStatement){
        Scanner in = new Scanner(System.in);
        System.out.println(question);
        while (true) {
            String ans = in.next();
            if (predicate.test(ans)) return ans;
            else {
                System.out.println(errorStatement);
                System.out.println(question);
            }
        }
    }
}
