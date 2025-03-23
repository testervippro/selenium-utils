package com.seleniumutils;

public class Main {
    public static void hideDock() throws IOException {
        String[] command = {"osascript", "-e", "tell application \"System Events\" to set autohide of dock preferences to true"};
        Runtime.getRuntime().exec(command);
        System.out.println("Dock hidden.");
    }

    public static void showDock() throws IOException {
        String[] command = {"osascript", "-e", "tell application \"System Events\" to set autohide of dock preferences to false"};
        Runtime.getRuntime().exec(command);
        System.out.println("Dock shown.");
    }

}
