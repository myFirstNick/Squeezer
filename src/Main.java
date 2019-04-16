import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        //  Path path = Paths.get("D:\\text.txt");//Р_Мартин_Чистый_код_Создание,_анализ.pdf");
        String firstQuestion = "Write an existing filepath to continue";
        String secondQuestion = "Do you want to squeeze or restore file? (s/r)";
        String errorStatement = "File with given path doesn't exist";

        Path path = Paths.get(new UI(firstQuestion).getAnswerWithTest(x -> new File(x).exists(), errorStatement));
        NewPathGenerator pathGenerator= new NewPathGenerator(path);
        Squeezer squeezer = new Squeezer();

        if (new UI(secondQuestion).getAnswerWithTest(x -> x.equals("r") || x.equals("s")).equals("s")) {
            byte codingParameter = 1; //кол-во байт рассматриваемых в качестве 1ого символа при формировании алфавита(от 1 до 4) для дальнейших доработок сжатия
            Path squeezedFilePath = pathGenerator.generate("squeezed_");
            squeezer.squeeze(path, squeezedFilePath, codingParameter); //сжимаем файл
        } else {
            Path restoredFilePath = pathGenerator.generate("restored_");
            squeezer.restore(path, restoredFilePath); //восстанавливаем файл
        }

    }

}
