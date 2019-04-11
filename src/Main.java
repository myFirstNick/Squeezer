import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {

        //  Path path = Paths.get("D:\\text.txt");//Р_Мартин_Чистый_код_Создание,_анализ.pdf");
        Path path = Paths.get(UI.getAnswerWithTest("Write an existing filepath to continue",
                x -> Paths.get(x).toFile().exists(),"File with that path doesn't exist"));

        if (UI.getAnswerWithTest("Do you want to squeeze or restore file? (s/r)",
                x -> x.equals("r") || x.equals("s")).equals("s")) {

            byte codingParameter = 1; //кол-во байт рассматриваемых в качестве 1ого символа при формировании алфавита(от 1 до 4) для дальнейших доработок сжатия
            Path squeezedFilePath = Paths.get(path.getParent()
                    + File.separator
                    + "squeezed_"
                    + path.getFileName());
            try {
                Squeezer.squeeze(path, squeezedFilePath, codingParameter); //сжимаем файл
            } catch (IOException e) {
                System.out.println("Error in file squeezing");
                e.printStackTrace();
            }

        } else {
            Path restoredFilePath = Paths.get(path.getParent()
                    + File.separator
                    + "restored_"
                    + path.getFileName());
            try {
                Squeezer.restore(path, restoredFilePath); //восстанавливаем файл
            } catch (IOException e) {
                System.out.println("Error in file restoration");
                e.printStackTrace();
            }
        }

    }

}
