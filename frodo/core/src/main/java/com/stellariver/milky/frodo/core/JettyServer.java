package com.stellariver.milky.frodo.core;

import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class JettyServer implements AutoCloseable{

    private final Logger logger = Log.getLogger(Server.class);

    private final Pattern scriptPattern = Pattern.compile("public\\s+class\\s+[a-zA-Z0-9_]+\\s+implements Callable<String>\\s*\\{");

    private final Server server;

    public JettyServer() {
        this(24113);
    }

    public JettyServer(@NonNull Integer port) {
        this.server = new Server(port);
        this.server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request request,
                               HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
                request.setHandled(true);
                byte[] bytes = IOUtils.toByteArray(httpRequest.getInputStream());
                String rawCharset = httpRequest.getCharacterEncoding();
                Charset charset = StringUtil.isBlank(rawCharset) ? StandardCharsets.UTF_8 : Charset.forName(rawCharset);
                CharsetDecoder decoder = charset.newDecoder();
                ByteBuffer buf = ByteBuffer.wrap(bytes);
                String requestBody;
                try {
                    requestBody = decoder.decode(buf).toString();
                } catch (CharacterCodingException e) {
                    httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    logger.warn(e);
                    return;
                }

                httpResponse.setCharacterEncoding(charset.name());
                httpResponse.getWriter().write(process(requestBody));

            }
        });

        try {
            this.server.start();
        } catch (Throwable throwable) {
            logger.warn(throwable);
        }

    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    public String process(String request) {

        Matcher matcher = scriptPattern.matcher(request);

        if (matcher.find()) {
            matcher.group();
        } else {
            return "could not find class implements Callable<String> in \n\n" + wrapper(request);
        }

        if (matcher.find()) {
            return "find too many public class in \n\n" + wrapper(request);
        }

        try {
            Class<?> clazz = JdkCompiler.load(request);
            Class<Callable<String>> callableClass = (Class<Callable<String>>) clazz;
            Callable<String> callable = callableClass.newInstance();
            return callable.call();
        } catch (Throwable throwable) {
            return printable(throwable);
        }

    }

    private String wrapper(String code) {
        StringBuilder builder = new StringBuilder();
        IntStream.range(0, 40).forEach(i -> builder.append("-"));
        String line = builder.toString();
        return "\\*" + line + "*\\\n\n" + code + "\n\n\\*" + line + "*\\\n";
    }

    private String printable(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    @Override
    public void close() throws Exception {
        this.server.stop();
    }

}