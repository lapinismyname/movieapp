package com.lapin.movieapp.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class CustomAppender extends AppenderBase<ILoggingEvent> {
    private final ObjectMapper objectMapper;

    private final String host;
    private final int port;

    private Socket socket;

    public CustomAppender() {
        objectMapper = new ObjectMapper();

        this.host = "127.0.0.1";
        this.port = 5001;

        try {
            socket = new Socket(host, port);
            System.out.println("Successfully connected to socket " +  host + ':' + port);
        }
        catch (Exception e) {
            System.out.println("Failed to connect to socket " +  host + ':' + port);
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            String json = objectMapper.writeValueAsString(logMapper(event));
            writer.println(json);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to send the log to socket " +  host + ':' + port);
        }
    }

    private Map<String, Object> logMapper(ILoggingEvent event) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("level", event.getLevel().toString());
        logMap.put("message", event.getFormattedMessage());
        logMap.put("timestamp", event.getTimeStamp());
        logMap.put("loggerName", event.getLoggerName());

        String user = event.getMDCPropertyMap().get("User");
        if (user != null) {
            try {
                Map<String, Object> userMap = objectMapper.readValue(user, new TypeReference<>() {});
                logMap.put("User", userMap);
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to parse User object in CustomAppender");
            }
        }

        String movie = event.getMDCPropertyMap().get("Movie");
        if (movie != null) {
            try {
                Map<String, Object> movieMap = objectMapper.readValue(movie, new TypeReference<>() {});
                logMap.put("Movie", movieMap);
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to parse Movie object in CustomAppender");
            }
        }
        return logMap;
    }
}
