package com.stellariver.milky.common.tool;

import com.stellariver.milky.common.base.BizEx;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;
import org.joor.ReflectException;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.stellariver.milky.common.base.ErrorEnumsBase.COMPILE_FAILURE;

/**
 * 复制自org.joor.Compile三方库的代码, 修复内存泄露的问题
 */
@Slf4j
public class JavaClassParser {

    @SneakyThrows
    public static Class<?> compile(String className, String content) {
        // remove package info
        content = Arrays.stream(content.split("\n")).map(x -> x.startsWith("package") ? " " : x).collect(Collectors.joining("\n"));

        String compileOut;

        ClassLoader parentCl = JavaClassParser.class.getClassLoader();

        // change class loader
        Thread.currentThread().setContextClassLoader(parentCl);

        try (URLClassLoader cl = new URLClassLoader(new URL[0], parentCl)) {

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            List<CharSequenceJavaFileObject> files = new ArrayList<>();
            files.add(new CharSequenceJavaFileObject(className, content));
            StringWriter out = new StringWriter();

            List<String> options = new ArrayList<>();
            // don't use SharedNameTable （jdk1.7 weak reference will affect gc， has been solved in jdk1.9）
            options.add("-XDuseUnsharedTable");

            // jdk1.8 bug, need assign parameter manually
            // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8039262
            System.setProperty("DuseJavaUtilZip", "true");
            options.add("-XDuseJavaUtilZip");

            StringBuilder classpath = new StringBuilder();
            String separator = System.getProperty("path.separator");
            String prop = System.getProperty("java.class.path");

            if (prop != null && !"".equals(prop)) {
                classpath.append(prop);
            }

            for (URL url : cl.getURLs()) {
                if (classpath.length() > 0) {
                    classpath.append(separator);
                }

                if ("file".equals(url.getProtocol())) {
                    classpath.append(new File(url.toURI()));
                }
            }
            if (parentCl instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) parentCl).getURLs()) {
                    if (classpath.length() > 0) {
                        classpath.append(separator);
                    }
                    classpath.append(url.getFile());
                }
            }
            options.addAll(Arrays.asList("-classpath", classpath.toString()));

            ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));

            CompilationTask task = compiler.getTask(out, fileManager, null, options, null, files);

            task.call();

            compileOut = out.toString();

            if (fileManager.isEmpty()) {
                throw new ReflectException("Compilation error: " + out);
            }

            Class<?> result = fileManager.loadAndReturnMainClass(className,
                    (name, bytes) -> Reflect.on(cl).call("defineClass", name, bytes, 0, bytes.length).get());

            BizEx.nullThrow(result, COMPILE_FAILURE.param("code", content).param("compile_out", compileOut));

            return result;
        }

    }

    static final class JavaFileObject extends SimpleJavaFileObject {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        JavaFileObject(String name, JavaFileObject.Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
        }

        byte[] getBytes() {
            return os.toByteArray();
        }

        @Override
        public OutputStream openOutputStream() {
            return os;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    static final class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, JavaFileObject> fileObjectMap;
        private Map<String, byte[]> classes;

        ClassFileManager(StandardJavaFileManager standardManager) {
            super(standardManager);

            fileObjectMap = new HashMap<>();
        }

        /***
         * 需要重写父类的isSameFile, 不然编译的时候会报错
         */
        @Override
        public boolean isSameFile(FileObject a, FileObject b) {
            return a.getName().equals(b.getName());
        }

        @Override
        public JavaFileObject getJavaFileForOutput(
                JavaFileManager.Location location,
                String className,
                JavaFileObject.Kind kind,
                FileObject sibling) {
            JavaFileObject result = new JavaFileObject(className, kind);
            fileObjectMap.put(className, result);
            return result;
        }

        boolean isEmpty() {
            return fileObjectMap.isEmpty();
        }

        Map<String, byte[]> classes() {
            if (classes == null) {
                classes = new HashMap<>();

                for (Entry<String, JavaFileObject> entry : fileObjectMap.entrySet()) {
                    classes.put(entry.getKey(), entry.getValue().getBytes());
                }
            }

            return classes;
        }

        Class<?> loadAndReturnMainClass(String mainClassName, ThrowingBiFunction<String, byte[], Class<?>> definer)
                throws Exception {
            Class<?> result = null;

            for (Entry<String, byte[]> entry : classes().entrySet()) {
                Class<?> c = definer.apply(entry.getKey(), entry.getValue());
                if (mainClassName.equals(entry.getKey())) {
                    result = c;
                }
            }

            return result;
        }
    }

    @FunctionalInterface
    interface ThrowingBiFunction<T, U, R> {
        R apply(T t, U u) throws Exception;
    }

    static final class CharSequenceJavaFileObject extends SimpleJavaFileObject {
        final CharSequence content;

        public CharSequenceJavaFileObject(String className, CharSequence content) {
            super(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension),
                    JavaFileObject.Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }
}
