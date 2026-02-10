package org.juv25d;

import org.juv25d.parser.HttpParser;

public class App {
    public static void main(String[] args) {
       new Server(new HttpParser()).start();
    }
}
