module com.udacity.SecurityService {
    requires java.desktop;
    requires miglayout.swing;
    requires guava;
    requires com.google.gson;
    requires java.prefs;
    requires com.udacity.ImageService;
    requires miglayout.core;
    exports com.udacity.SecurityService.service;
    exports com.udacity.SecurityService.application;
    exports com.udacity.SecurityService.data;

}