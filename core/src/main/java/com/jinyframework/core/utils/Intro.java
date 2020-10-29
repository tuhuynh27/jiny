package com.jinyframework.core.utils;

/**
 * The type Intro.
 */
public final class Intro {
    private static Intro instance;

    private Intro() {
        System.out.println("     _ _             _____                                            _    \n" +
                "    | (_)_ __  _   _|  ___| __ __ _ _ __ ___   _____      _____  _ __| | __\n" +
                " _  | | | '_ \\| | | | |_ | '__/ _` | '_ ` _ \\ / _ \\ \\ /\\ / / _ \\| '__| |/ /\n" +
                "| |_| | | | | | |_| |  _|| | | (_| | | | | | |  __/\\ V  V / (_) | |  |   < \n" +
                " \\___/|_|_| |_|\\__, |_|  |_|  \\__,_|_| |_| |_|\\___| \\_/\\_/ \\___/|_|  |_|\\_\\\n" +
                "               |___/                                                       \n");
    }

    /**
     * Begin intro.
     *
     * @return the intro
     */
    public static synchronized Intro begin() {
        if (instance == null) {
            instance = new Intro();
        }
        return instance;
    }
}