package cs151.application;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class AppFiles {

    public static final Path BASE_DIR = findBaseDir();
    public static final Path LANG_CSV = BASE_DIR.resolve("ProgrammingLanguage.csv");
    public static final Path STUDENTS_CSV = BASE_DIR.resolve("Students.csv");

    static {
        try { Files.createDirectories(BASE_DIR); } catch (Exception ignored) {}
    }

    private static Path findBaseDir() {
        // 1) explicit override
        String prop = System.getProperty("app.baseDir");
        if (prop != null && !prop.isBlank()) {
            return Paths.get(prop).toAbsolutePath().normalize();
        }

        // 2) working directory (current folder)
        try {
            String userDir = System.getProperty("user.dir");
            if (userDir != null && !userDir.isBlank()) {
                return Paths.get(userDir).toAbsolutePath().normalize();
            }
        } catch (Exception ignored) { }

        // 3) location of JAR or classes
        try {
            URI uri = AppFiles.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path loc = Paths.get(uri);
            if (Files.isRegularFile(loc) && loc.toString().endsWith(".jar")) {
                // running from a JAR: use the jar's folder
                return loc.getParent().toAbsolutePath().normalize();
            } else if (Files.isDirectory(loc)) {
                // running from classes dir, e.g. .../target/classes
                Path p = loc.toAbsolutePath().normalize();
                if (p.getFileName().toString().equalsIgnoreCase("classes")) {
                    Path target = p.getParent();
                    if (target != null && target.getFileName().toString().equalsIgnoreCase("target")) {
                        Path projectRoot = target.getParent();
                        if (projectRoot != null) return projectRoot;
                    }
                }
                return p;
            }
        } catch (Exception ignored) { }

        // 4) last resort
        return Paths.get(".").toAbsolutePath().normalize();
    }
    /** Load languages into the given ListView (creates file with header if missing). */
    public static void loadLanguages(ListView<String> target) throws IOException {
        target.setPlaceholder(new Label("No languages found. Use 'Define Programming Languages' to add some."));
        if (!Files.exists(LANG_CSV)) {
            try (BufferedWriter bw = Files.newBufferedWriter(LANG_CSV, StandardCharsets.UTF_8)) {
                bw.write("Name\n");
            }
            target.setItems(FXCollections.observableArrayList());
            return;
        }
        List<String> langs = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(LANG_CSV, StandardCharsets.UTF_8)) {
            String line; boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (!line.isBlank()) langs.add(line.trim());
            }
        }
        target.setItems(FXCollections.observableArrayList(langs));
    }

    /** Overwrite language CSV with header + items. */
    public static void saveLanguages(Collection<String> langs) throws IOException {
        Files.createDirectories(BASE_DIR);
        try (BufferedWriter bw = Files.newBufferedWriter(LANG_CSV, StandardCharsets.UTF_8)) {
            bw.write("Name\n");
            for (String s : langs) if (s != null && !s.isBlank()) bw.write(s.trim() + "\n");
        }
    }
    private AppFiles() {}
}
