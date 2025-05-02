package org.swdc.websdk.core.generator.java;

import org.swdc.websdk.core.SDKProject;
import org.swdc.websdk.core.generator.FileUtil;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.jar.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class JavaSDKPackager {

    private static class SourceJavaFileObject extends SimpleJavaFileObject {

        private File target;

        /**
         * Construct a SimpleJavaFileObject of the given kind and with the
         * given URI.
         */
        protected SourceJavaFileObject(File targetFile, Kind kind) {
            super(targetFile.toURI(), kind);
            target = targetFile;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return new String(Files.readAllBytes(target.toPath()), StandardCharsets.UTF_8);
        }

    }

    private SDKProject project;

    private File assetFolder;

    private String classVersion;

    public JavaSDKPackager(SDKProject project,File assetFolder, String classVersion) {
        this.project = project;
        this.assetFolder = assetFolder;
        this.classVersion = classVersion;
    }


    public void doPackage(List<File> sources) {

        try {

            File targetClassRoot = new File(project.getSourceFile().getParent(),"target/classes");
            Path targetClassRootPath = targetClassRoot.toPath();
            FileUtil.clean(targetClassRoot);

            compileSource(sources);

            File target = new File(project.getSourceFile().getParent(),project.getProjectName() + ".jar");
            if (target.exists()) {
                target.delete();
            }

            JarOutputStream jos = new JarOutputStream(new FileOutputStream(target));
            Files.walkFileTree(targetClassRootPath, new FileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    byte[] data = Files.readAllBytes(file);

                    Path path = targetClassRootPath.relativize(file).normalize();
                    ZipEntry entry = new ZipEntry(path.toString());

                    jos.putNextEntry(entry);
                    jos.write(data);
                    jos.closeEntry();

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });

            jos.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private void compileSource(List<File> sources) throws IOException {

        File projectRoot = project.getSourceFile().getParentFile();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        StandardJavaFileManager manager = compiler.getStandardFileManager(collector, Locale.getDefault(), StandardCharsets.UTF_8);
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                manager,
                collector,
                Arrays.asList(
                        "--release", "8",
                        "-d", new File(projectRoot,"/target/classes").getAbsolutePath(),
                        "-cp", generateClassPathForBuild(assetFolder)
                ),
                null,
                sources.stream().map(
                        f -> new SourceJavaFileObject(f, JavaFileObject.Kind.SOURCE)
                ).collect(Collectors.toList())
        );

        boolean result = task.call();
        if (!result) {
            StringBuilder sb = new StringBuilder();
            for(Diagnostic diagnostic : collector.getDiagnostics()) {
                sb.append(diagnostic.getCode() + "\r\n" + diagnostic.getMessage(Locale.getDefault()));
            }
            throw new RuntimeException(sb.toString());
        }

    }

    private String generateClassPathForBuild(File assetFolder) {
        File[] jars = new File(assetFolder, "libraries").listFiles();
        StringBuilder sb = new StringBuilder();
        if (jars == null) {
            return "";
        }
        for (File jar: jars) {
            sb.append(jar.getAbsolutePath()).append(";");
        }
        return sb.toString();
    }

}
