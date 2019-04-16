import java.util.Scanner;
import java.util.function.Predicate;

public class UI {
    private String question;
    private Scanner in = new Scanner(System.in);

    public UI(String quest){
        this.question=quest;
    }

    public String getAnswer() {
        Scanner in = new Scanner(System.in);
        System.out.println(question);

        return in.next();
    }

    public String getAnswerWithTest (Predicate<String> predicate){
        System.out.println(question);
        while (true) {
            String ans = in.next();
            if (predicate.test(ans)) return ans;
        }
    }

    public String getAnswerWithTest (Predicate<String> predicate, String errorStatement){
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
