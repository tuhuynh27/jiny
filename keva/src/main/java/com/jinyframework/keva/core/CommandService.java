package com.jinyframework.keva.core;

import com.jinyframework.keva.storage.HashMapStorage;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.PrintWriter;

@RequiredArgsConstructor
public class CommandService {
    public final HashMapStorage<String, String> stringStringStore;

    private static String[] parseTokens(String line) {
        return line.split(" ");
    }

    public void handleCommand(PrintWriter socketOut, String line) {
        val tokens = parseTokens(line);
        val command = Command.valueOf(tokens[0].toUpperCase());
        switch (command) {
            case GET:
                socketOut.println(stringStringStore.get(tokens[1]));
                break;
            case SET:
                socketOut.println(stringStringStore.put(tokens[1], tokens[2]));
                break;
            case PING:
                socketOut.println("PONG");
                break;
            default:
                socketOut.println("Unsupported command");
        }
        socketOut.flush();
    }

    enum Command {
        GET,
        PING,
        SET
    }
}
