import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Map;

public class Squeezer {
    private Squeezer(){}

    public static void squeeze(Path path, Path squeezedFilePath, byte codingParameter) throws IOException {
        byte[] allBytes = Files.readAllBytes(path);

        Integer[] allBytesGrouped = SqueezeHelper.bytesGroupingByParameter(allBytes, codingParameter);

        byte[] codedBytes = SqueezeHelper.coder(allBytesGrouped);
        byte[] alphabet = SqueezeHelper.bytesToAlphabet(allBytesGrouped,codingParameter);
        byte[] alphabetLenBytes = SqueezeHelper.intToByteArr(alphabet.length, codingParameter);

        OutputStream byteWriter = new FileOutputStream(squeezedFilePath.toFile());
        byteWriter.write(codingParameter);
        byteWriter.write(alphabetLenBytes);
        byteWriter.write(alphabet);
        byteWriter.write(codedBytes);
        byteWriter.close();

        System.out.println("File squeezed to: " + squeezedFilePath.toString());
    }

    public static void restore(Path squeezedFilePath, Path restoredFilePath) throws IOException{
        InputStream byteReader = Files.newInputStream(squeezedFilePath);
        byte codingParameter = (byte) byteReader.read();

        byte[] alphabetLenBytes= new byte[codingParameter];
        byteReader.read(alphabetLenBytes,0,codingParameter);
        int alphabetLen = RestorationHelper.alphabetLenConstructor(alphabetLenBytes, codingParameter);

        byte [] alphabetBytes = new byte[alphabetLen];
        byteReader.read(alphabetBytes,0,alphabetLen);
        Map<Integer, ByteBuffer> restorationMap =  RestorationHelper.restorationMapConstructor(alphabetBytes, codingParameter);

        BitSet bitsToRestore=RestorationHelper.bitsToRestoreGetter(squeezedFilePath,1+codingParameter+alphabetLen);
        byte [] bytesRestored = RestorationHelper.decoder(bitsToRestore,restorationMap,codingParameter);

        Files.write(restoredFilePath,bytesRestored);
        System.out.println("File restored to: " + restoredFilePath.toString());
    }
}
