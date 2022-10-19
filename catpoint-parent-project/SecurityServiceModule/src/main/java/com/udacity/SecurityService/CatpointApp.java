package com.udacity.SecurityService;
import com.udacity.SecurityService.application.CatpointGui;
import com.google.gson.Gson;
/**
 * This is the main class that launches the application.
 */
public class CatpointApp {
    public static void main(String[] args) {
        CatpointGui gui = new CatpointGui();
        gui.setVisible(true);
    }
}
