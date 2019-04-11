import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class RestorationHelper {
    private RestorationHelper(){};

    public static int alphabetLenConstructor (byte [] alphabetLenBytes, int codingParameter){
        int alphabetLen = 0;
        for (int i =codingParameter-1;i>-1; i--) alphabetLen =(alphabetLen<< 8*(codingParameter-1-i)) + (int) alphabetLenBytes[i];
        if (alphabetLen==0) alphabetLen=(int) Math.pow(256 ,codingParameter);

        return alphabetLen;
    }

    public static Map<Integer, ByteBuffer> restorationMapConstructor (byte [] alphabetBytes, int codingParameter) {
        int alphabetLen = alphabetBytes.length;

        Map<Integer,ByteBuffer> restorationMap =  new HashMap<>();
        for (int i = 0; i < alphabetLen; i=i+codingParameter) {
            ByteBuffer alphabetGroup = ByteBuffer.allocate(codingParameter);
            for (int j=0;j<codingParameter;j++) if (i+j < alphabetLen) alphabetGroup.put(alphabetBytes[i+j]);
            restorationMap.put(i/codingParameter, alphabetGroup);
        }

        return restorationMap;
    }

    public static BitSet bitsToRestoreGetter (Path squeezedFilePath, int off) throws IOException {
        byte[] allBytes = Files.readAllBytes(squeezedFilePath);
        ByteBuffer bytesToRestore = ByteBuffer.allocate(allBytes.length);
        bytesToRestore.put(allBytes).position(off);
        bytesToRestore.compact();

        return BitSet.valueOf(bytesToRestore.array());
    }

    public static byte[] decoder(BitSet bitsToRestore, Map<Integer,ByteBuffer> restorationMap, int codingParameter){
        int [] squeezedValues = bitsToRestore.stream().toArray();
        ByteBuffer restoredBytes = ByteBuffer.allocate(squeezedValues.length*codingParameter);

        restoredBytes.put(restorationMap.get(squeezedValues[0]).array());
        for (int i = 1; i < squeezedValues.length; i++)
            restoredBytes.put(restorationMap.get(squeezedValues[i]-squeezedValues[i-1]-1).array());
        return restoredBytes.array();
    }
}
