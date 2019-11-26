package myProject;

import helpers.LwjglWindow;

public class LwjglApp {
    public static void main(String[] args) {
        new LwjglWindow(1028, 720, new Renderer(), true);
    }
}