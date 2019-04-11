import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class SqueezeHelper {

    private SqueezeHelper(){};
    public static Integer[] bytesGroupingByParameter(byte[] allBytes, byte codingParameter) {
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

    public static Integer[] sortedByFrequency(Integer[] allBytesGrouped) {
        return Arrays.stream(allBytesGrouped)
                .collect(groupingBy(identity(),counting()))
                .entrySet()
                .stream()
                .sorted((x1,x2) -> x2.getValue().compareTo(x1.getValue()))
                .map(Map.Entry::getKey)
                .toArray(Integer[]::new);
    }

// перевести в кодировку с парными битами (два лидирующих отвечают за длину символа в битах
    public static byte[] coder(Integer[] allBytesGrouped){
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

    public static byte[] bytesToAlphabet(Integer[] allBytesGrouped, byte codingParameter) {
        Integer[] sortedUniqueByteGroups =sortedByFrequency(allBytesGrouped);
        ByteBuffer alphabet = ByteBuffer.allocate(sortedUniqueByteGroups.length*codingParameter);

        for (int i = 0; i < sortedUniqueByteGroups.length; i++){
            for(int j=0; j< codingParameter; j++) alphabet.put((byte) (sortedUniqueByteGroups[i] >>> 8*(codingParameter-1-j)));
        }
        return alphabet.array();
    }

    public static byte[] intToByteArr(Integer alphabetLen, byte codingParameter) {
        byte[] alphabetLenBytes = new byte[codingParameter];
        for (int i = 0; i < codingParameter; i++) alphabetLenBytes[i]= (byte) (alphabetLen >>> 8*i);
        return alphabetLenBytes;
    }
}
