package org.juv25d.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponseWriter {

    private HttpResponseWriter() {
    }

    // This method should be called by SocketServer/ConnectionHandler later
    public static void write(OutputStream out, HttpResponse response) throws IOException {
        writeStatusLine(out, response);
        writeHeaders(out, response.headers(), response.body());
        writeBody(out, response.body());
        out.flush();
    }

    private static void writeStatusLine(OutputStream out, HttpResponse response) throws IOException {
        String statusLine =
            "HTTP/1.1 " + response.statusCode() + " " + response.statusText() + "\r\n";
        out.write(statusLine.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeHeaders(
        OutputStream out,
        Map<String, String> headers,
        byte[] body
    ) throws IOException {

        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (!header.getKey().equalsIgnoreCase("Content-Length")) {
                String line = header.getKey() + ": " + header.getValue() + "\r\n";
                out.write(line.getBytes(StandardCharsets.UTF_8));
            }
        }

        String contentLength = "Content-Length: " + body.length + "\r\n";
        out.write(contentLength.getBytes(StandardCharsets.UTF_8));

        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }


    private static void writeBody(OutputStream out, byte[] body) throws IOException {
        out.write(body);
    }
}
