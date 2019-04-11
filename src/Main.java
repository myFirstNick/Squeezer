/**
 * Метод squeeze
 * кодирует файл в новый (по указанному адресу) по следующему принципу:
 * формирует блоки байт(от 1 до 4 в зависимости от coding Parameter) и сортирует их по частотности
 * после чего происходит замена блоков на наборы бит вида: 1, 10, 100, 1000 и т.д. (метод подлежит доработке)
 * алфавит и размер блоков записываются в файл
 *
 * Метод restore
 * производит преобразования обратные методу squeeze
 * формирует раскодированный файл по заданному адресу
 */

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.BitSet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public class Main {
    public static void main(String[] args) {
        UI ui = new UI();
        //  Path path = Paths.get("D:\\text.txt");//Р_Мартин_Чистый_код_Создание,_анализ.pdf");
        Path path = Paths.get(ui.getAnswerWithTest("Write an existing filepath to continue",
                x -> Paths.get(x).toFile().exists(),"File with that path doesn't exist"));

        if (ui.getAnswerWithTest("Do you want to squeeze or restore file? (s/r)",
                x -> x.equals("r") || x.equals("s")).equals("s")) {

            byte codingParameter = 1; //кол-во байт рассматриваемых в качестве 1ого символа при формировании алфавита(от 1 до 4) для дальнейших доработок сжатия
            Path squeezedFilePath = Paths.get(path.getParent()
                    + File.separator
                    + "squeezed_"
                    + path.getFileName());

            try {
                squeeze(path, squeezedFilePath, codingParameter); //сжимаем файл
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
                restore(path, restoredFilePath); //восстанавливаем файл
            } catch (IOException e) {
                System.out.println("Error in file restoration");
                e.printStackTrace();
            }
        }

    }

    private static void squeeze(Path path,Path squeezedFilePath, byte codingParameter) throws IOException {
        byte[] allBytes = Files.readAllBytes(path);

        Integer[] allBytesGrouped = bytesGroupingByParameter(allBytes, codingParameter);

        byte[] codedBytes = bytesToBytesCoded(allBytesGrouped);
        byte[] alphabet = bytesToAlphabet(allBytesGrouped,codingParameter);
        byte[] alphabetLenBytes = intToByteArr(alphabet.length, codingParameter);

        OutputStream byteWriter = new FileOutputStream(squeezedFilePath.toFile());
        byteWriter.write(codingParameter);
        byteWriter.write(alphabetLenBytes);
        byteWriter.write(alphabet);
        byteWriter.write(codedBytes);
        byteWriter.close();

        System.out.println("File squeezed to: " + squeezedFilePath.toString());
    }

    private static Integer[] bytesGroupingByParameter(byte[] allBytes, byte codingParameter) {
        int mask = 255;
        int len = allBytes.length / codingParameter;
        len = len*codingParameter<allBytes.length ? len+1 : len;// округление вверх
        Integer[] allBytesGrouped = new Integer[len];
        for (int i = 0; i < allBytes.length; i = i + codingParameter) {
            int superposition = 0;
            for (int j = 0; j < codingParameter; j++)
                if (i+j<allBytes.length) superposition = superposition + (((int) allBytes[i + j])&mask)<<8*(codingParameter-1-j);
            allBytesGrouped[i/codingParameter]=superposition;
        }
        return allBytesGrouped;
    }

    private static Integer[] sortedByFrequency(Integer[] allBytesGrouped) {
        return Arrays.stream(allBytesGrouped)
                .collect(groupingBy(identity(),counting()))
                .entrySet()
                .stream()
                .sorted((x1,x2) -> x2.getValue().compareTo(x1.getValue()))
                .map(Map.Entry::getKey)
                .toArray(Integer[]::new);
    }

    private static byte[] bytesToBytesCoded(Integer[] allBytesGrouped){
        Integer[] sortedUniqueByteGroups =sortedByFrequency(allBytesGrouped);

        Map <Integer,Integer> squeezingMap = new HashMap<>();
        for (int i = 0; i < sortedUniqueByteGroups.length; i++){
            squeezingMap.put(sortedUniqueByteGroups[i],i);
        }

        BitSet bits = new BitSet();
        Arrays.stream(allBytesGrouped)
                .map(squeezingMap::get)
                .forEach(x -> bits.set(x+bits.length()));
        return bits.toByteArray();
    }

    private static byte[] bytesToAlphabet(Integer[] allBytesGrouped, byte codingParameter) {
        Integer[] sortedUniqueByteGroups =sortedByFrequency(allBytesGrouped);
        ByteBuffer alphabet = ByteBuffer.allocate(sortedUniqueByteGroups.length*codingParameter);

        for (int i = 0; i < sortedUniqueByteGroups.length; i++){
            for(int j=0; j< codingParameter; j++) alphabet.put((byte) (sortedUniqueByteGroups[i] >>> 8*(codingParameter-1-j)));
        }
        return alphabet.array();
    }

    private static byte[] intToByteArr(Integer alphabetLen, byte codingParameter) {
        byte[] alphabetLenBytes = new byte[codingParameter];
        for (int i = 0; i < codingParameter; i++) alphabetLenBytes[i]= (byte) (alphabetLen >>> 8*i);
        return alphabetLenBytes;
    }




    private static void restore(Path squeezedFilePath, Path restoredFilePath)throws IOException{
        InputStream byteReader = Files.newInputStream(squeezedFilePath);
        byte codingParameter = (byte) byteReader.read();

        byte[] alphabetLenBytes= new byte[codingParameter];
        byteReader.read(alphabetLenBytes,0,codingParameter);
        int alphabetLen = alphabetLenConstructor(alphabetLenBytes, codingParameter);

        byte [] alphabetBytes = new byte[alphabetLen];
        byteReader.read(alphabetBytes,0,alphabetLen);
        Map<Integer,ByteBuffer> restorationMap =  restorationMapConstructor(alphabetBytes, codingParameter);


        BitSet bitsToRestore=bitsToRestoreGetter(squeezedFilePath,1+codingParameter+alphabetLen);
        byte [] bytesRestored = decoder(bitsToRestore,restorationMap,codingParameter);

        Files.write(restoredFilePath,bytesRestored);
        System.out.println("File restored to: " + restoredFilePath.toString());
    }

    private static int alphabetLenConstructor (byte [] alphabetLenBytes, int codingParameter){
        int alphabetLen = 0;
        for (int i =codingParameter-1;i>-1; i--) alphabetLen =(alphabetLen<< 8*(codingParameter-1-i)) + (int) alphabetLenBytes[i];
        if (alphabetLen==0) alphabetLen=(int) Math.pow(256 ,codingParameter);

        return alphabetLen;
    }

    private static Map<Integer,ByteBuffer> restorationMapConstructor (byte [] alphabetBytes, int codingParameter) {
        int alphabetLen = alphabetBytes.length;

        Map<Integer,ByteBuffer> restorationMap =  new HashMap<>();
        for (int i = 0; i < alphabetLen; i=i+codingParameter) {
            ByteBuffer alphabetGroup = ByteBuffer.allocate(codingParameter);
            for (int j=0;j<codingParameter;j++) if (i+j < alphabetLen) alphabetGroup.put(alphabetBytes[i+j]);
            restorationMap.put(i/codingParameter, alphabetGroup);
        }

        return restorationMap;
    }

    private static BitSet bitsToRestoreGetter (Path squeezedFilePath, int off) throws IOException {
        byte[] allBytes = Files.readAllBytes(squeezedFilePath);
        ByteBuffer bytesToRestore = ByteBuffer.allocate(allBytes.length);
        bytesToRestore.put(allBytes).position(off);
        bytesToRestore.compact();

        return BitSet.valueOf(bytesToRestore.array());
    }

    private static byte[] decoder(BitSet bitsToRestore, Map<Integer,ByteBuffer> restorationMap, int codingParameter){
        int [] squeezedValues = bitsToRestore.stream().toArray();
        ByteBuffer restoredBytes = ByteBuffer.allocate(squeezedValues.length*codingParameter);

        restoredBytes.put(restorationMap.get(squeezedValues[0]).array());
        for (int i = 1; i < squeezedValues.length; i++)
            restoredBytes.put(restorationMap.get(squeezedValues[i]-squeezedValues[i-1]-1).array());
        return restoredBytes.array();
    }
}
