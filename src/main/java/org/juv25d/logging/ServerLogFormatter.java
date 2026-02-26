package org.juv25d.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ServerLogFormatter extends Formatter {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        String connectionId = LogContext.getConnectionId();
        String idPart = (connectionId != null) ? " [" + connectionId + "]" : "";

        StringBuilder sb = new StringBuilder(
                String.format("%s %s%s: %s%n",
                        ZonedDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault()).format(dtf),
                        record.getLevel(),
                        idPart,
                        formatMessage(record)));

        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(sw));
            sb.append(sw);
        }
        return sb.toString();
    }
}
