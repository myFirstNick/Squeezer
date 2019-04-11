import java.util.Scanner;
import java.util.function.Predicate;

public class UI {

    public String getAnswer(String question) {
        Scanner in = new Scanner(System.in);
        System.out.println(question);

        return in.next();
    }

    public String getAnswer(String question, String possibleAnswer1, String possibleAnswer2) {
        Scanner in = new Scanner(System.in);
        System.out.println(question);

        while (true) {
            String ans = in.next();
            if (ans.equals(possibleAnswer1))
                return possibleAnswer1;
            if (ans.equals(possibleAnswer2))
                return possibleAnswer2;
        }
    }

    public String getAnswerWithTest (String question, Predicate<String> predicate){
        Scanner in = new Scanner(System.in);
        System.out.println(question);
        while (true) {
            String ans = in.next();
            if (predicate.test(ans)) return ans;
        }
    }

    public String getAnswerWithTest (String question, Predicate<String> predicate, String errorStatement){
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
