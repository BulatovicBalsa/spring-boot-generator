package myplugin.generator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class ProjectGenerator {

    private final String templateRoot;
    private final String outputRoot;

    public ProjectGenerator(String templateRoot, String outputRoot) {
        this.templateRoot = templateRoot;
        this.outputRoot = outputRoot;
    }

    public void generate() throws IOException {
        File sourceDir = new File(templateRoot);
        File targetDir = new File(outputRoot);

        copyDirectory(sourceDir, targetDir);
    }

    private void copyDirectory(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                //noinspection ResultOfMethodCallIgnored
                target.mkdirs();
            }

            for (String child : Objects.requireNonNull(source.list())) {
                copyDirectory(new File(source, child), new File(target, child));
            }
        } else {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}