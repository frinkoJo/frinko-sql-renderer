package frinko.sql.renderer.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MapperScanner {
    public List<Path> scan(List<Path> roots) {
        List<Path> out = new ArrayList<>();
        for (Path root : roots) {
            try {
                Files.walk(root).filter(p -> p.toString().endsWith(".xml")).forEach(out::add);
            } catch (IOException e) { throw new RuntimeException(e); }
        }
        return out;
    }
}

