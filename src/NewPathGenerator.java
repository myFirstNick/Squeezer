import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

class NewPathGenerator {
    private Path initialPath;

    NewPathGenerator(Path iniPath){
        this.initialPath=iniPath;
    }

    Path getInitialPath(){
        return this.initialPath;
    }

    Path generate(String prefix) {
        return Paths.get(initialPath.getParent()
                + File.separator
                + prefix
                + initialPath.getFileName());
    }
}
