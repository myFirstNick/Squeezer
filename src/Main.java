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
        Path path = Paths.get("D:\\Р_Мартин_Чистый_код_Создание,_анализ.pdf");
        byte codingParameter = 1; //кол-во байт рассматриваемых в качестве 1ого символа при формировании алфавита(от 1 до 4) для дальнейших доработок сжатия

        Path squeezedFilePath= Paths.get(path.getParent()
                +File.separator
                +"squeezed_"
                +path.getFileName());

        Path restoredFilePath = Paths.get(squeezedFilePath.getParent()
                +File.separator
                +"restored_"
                +squeezedFilePath.getFileName());
        try {
            squeeze(path,squeezedFilePath,codingParameter); //сжимаем файл
            restore(squeezedFilePath,restoredFilePath); //восстанавливаем файл

        } catch (IOException e){System.out.println(e);};
    }

    private static void squeeze(Path path,Path squeezedFilePath, byte codingParameter) throws IOException {
        byte[] allBytes = Files.readAllBytes(path);
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

        Integer[] sortedUniqueByteGroups = Arrays.stream(allBytesGrouped)
                .collect(groupingBy(identity(),counting()))
                .entrySet()
                .stream()
                .sorted((x1,x2) -> x2.getValue().compareTo(x1.getValue()))
                .map(Map.Entry::getKey)
                .toArray(Integer[]::new);

        Map <Integer,Integer> squeezingMap = new HashMap<>();
        ByteBuffer alphabet = ByteBuffer.allocate(sortedUniqueByteGroups.length*codingParameter);

        for (int i = 0; i < sortedUniqueByteGroups.length; i++){
            squeezingMap.put(sortedUniqueByteGroups[i],i);

            for(int j=0; j< codingParameter; j++) alphabet.put((byte) (sortedUniqueByteGroups[i] >>> 8*(codingParameter-1-j)));
        }

        int alphabetLen =  alphabet.array().length;
        byte[] alphabetLenBytes = new byte[codingParameter];
        for (int i = 0; i < codingParameter; i++) alphabetLenBytes[i]= (byte) (alphabetLen >>> 8*i);

        BitSet bits = new BitSet();
        Arrays.stream(allBytesGrouped)
                .map(squeezingMap::get)
                .forEach(x -> bits.set(x+bits.length()));

        OutputStream byteWriter = new FileOutputStream(squeezedFilePath.toFile());

        byteWriter.write(codingParameter);
        byteWriter.write(alphabetLenBytes);
        byteWriter.write(alphabet.array());
        byteWriter.write(bits.toByteArray());
        byteWriter.close();
        System.out.println("File squeezed to: " + squeezedFilePath.toString());
    }

    private static void restore(Path squeezedFilePath, Path restoredFilePath)throws IOException{
        int bytesAmount = Files.readAllBytes(squeezedFilePath).length;

        InputStream byteReader = Files.newInputStream(squeezedFilePath);
        byte codingParameter = (byte) byteReader.read();
        byte[] alphabetLenBytes= new byte[codingParameter];
        byteReader.read(alphabetLenBytes,0,codingParameter);
        int alphabetLen = 0;
        for (int i =codingParameter-1;i>-1; i--) alphabetLen =(alphabetLen<< 8*(codingParameter-1-i)) + (int) alphabetLenBytes[i];
        if (alphabetLen==0) alphabetLen=(int) Math.pow(256 ,codingParameter);

        byte [] alphabetBytes = new byte[alphabetLen];
        byteReader.read(alphabetBytes,0,alphabetLen);
        Map<Integer,ByteBuffer> restorationMap =  new HashMap<>();
        for (int i = 0; i < alphabetLen; i=i+codingParameter) {
            ByteBuffer alphabetGroup = ByteBuffer.allocate(codingParameter);
            for (int j=0;j<codingParameter;j++) if (i+j < alphabetLen) alphabetGroup.put(alphabetBytes[i+j]);
            restorationMap.put(i/codingParameter, alphabetGroup);
        }
        ByteBuffer bytesToRestore = ByteBuffer.allocate(bytesAmount);
        for (int next = byteReader.read(); next >-1;) {
            bytesToRestore.put((byte) next);
            next = byteReader.read();
        }

        BitSet bitsToRestore=BitSet.valueOf(bytesToRestore.array());
        int [] squeezedValues = bitsToRestore.stream().toArray();
        ByteBuffer restoredBytes = ByteBuffer.allocate(squeezedValues.length*codingParameter);

        restoredBytes.put(restorationMap.get(squeezedValues[0]).array());
        for (int i = 1; i < squeezedValues.length; i++)
            restoredBytes.put(restorationMap.get(squeezedValues[i]-squeezedValues[i-1]-1).array());

        Files.write(restoredFilePath,restoredBytes.array());
        System.out.println("File restored to: " + restoredFilePath.toString());
    }
}
