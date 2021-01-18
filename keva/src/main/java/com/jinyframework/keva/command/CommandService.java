package com.jinyframework.keva.command;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;

import java.io.PrintWriter;
import java.util.*;

@Slf4j
public final class CommandService {

    private static final Map<String, CommandHandler> commandHandlerMap = initMap();

    private CommandService() {
    }

    private static ArrayList<String> parseTokens(String line) {
        return new ArrayList<>(Arrays.asList(line.split(" ")));
    }

    public static void handleCommand(PrintWriter socketOut, String line) {
        Object output;
        try {
            val args = parseTokens(line);
            val commandName = args.get(0).toLowerCase();
            var handler = commandHandlerMap.get(commandName);
            if (handler == null) {
                handler = commandHandlerMap.get(getCommandName(Unsupported.class));
            }
            args.remove(0);
            output = handler.handle(args);
        } catch (Exception e) {
            log.error("Error while handling command: ",e);
            output = "ERROR";
        }
        socketOut.println(output);
        socketOut.flush();
    }

    @SuppressWarnings("rawtypes")
    static String getCommandName(Class clzz) {
        return clzz.getSimpleName().toLowerCase();
    }

    private static Map<String, CommandHandler> initMap() {
        val map = new HashMap<String, CommandHandler>();
        map.put(getCommandName(Ping.class), new Ping());
        map.put(getCommandName(Get.class), new Get());
        map.put(getCommandName(Set.class), new Set());

        map.put(getCommandName(Unsupported.class), new Unsupported());
        return Collections.unmodifiableMap(map);
    }
}
