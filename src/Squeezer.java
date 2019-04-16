import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Map;

class Squeezer {
    void squeeze(Path path, Path squeezedFilePath, byte codingParameter) {
        try{
            SqueezeHelper squeezeHelper = new SqueezeHelper();

            byte[] allBytes = Files.readAllBytes(path);

            Integer[] allBytesGrouped = squeezeHelper.bytesGroupingByParameter(allBytes, codingParameter);

            byte[] codedBytes = squeezeHelper.coder(allBytesGrouped);
            byte[] alphabet = squeezeHelper.bytesToAlphabet(allBytesGrouped,codingParameter);
            byte[] alphabetLenBytes = squeezeHelper.intToByteArr(alphabet.length, codingParameter);

            OutputStream byteWriter = new FileOutputStream(squeezedFilePath.toFile());
            byteWriter.write(codingParameter);
            byteWriter.write(alphabetLenBytes);
            byteWriter.write(alphabet);
            byteWriter.write(codedBytes);
            byteWriter.close();

            System.out.println("File squeezed to: " + squeezedFilePath.toString());

        } catch (IOException e) {
             System.out.println("Error in file squeezing");
             e.printStackTrace();
        }
    }

    void restore(Path squeezedFilePath, Path restoredFilePath) {
        try {
            RestorationHelper restorationHelper = new RestorationHelper();

            InputStream byteReader = Files.newInputStream(squeezedFilePath);
            byte codingParameter = (byte) byteReader.read();

            byte[] alphabetLenBytes= new byte[codingParameter];
            byteReader.read(alphabetLenBytes,0,codingParameter);
            int alphabetLen = restorationHelper.alphabetLenConstructor(alphabetLenBytes, codingParameter);

            byte [] alphabetBytes = new byte[alphabetLen];
            byteReader.read(alphabetBytes,0,alphabetLen);
            Map<Integer, ByteBuffer> restorationMap =  restorationHelper.restorationMapConstructor(alphabetBytes, codingParameter);

            BitSet bitsToRestore=restorationHelper.bitsToRestoreGetter(squeezedFilePath,1+codingParameter+alphabetLen);

            byte [] bytesRestored = restorationHelper.decoder(bitsToRestore,restorationMap,codingParameter);

            Files.write(restoredFilePath,bytesRestored);
            System.out.println("File restored to: " + restoredFilePath.toString());

        } catch (IOException e) {
            System.out.println("Error in file restoration");
            e.printStackTrace();
        }
    }
}
