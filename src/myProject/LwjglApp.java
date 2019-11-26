package myProject;

import helpers.LwjglWindow;

import java.io.IOException;

public class LwjglApp {
    public static void main(String[] args) {
        new LwjglWindow(1028, 720, new Renderer(), true);
    }
}