module com.udacity.SecurityService {
    requires java.desktop;
    requires miglayout.swing;
    requires guava;
    requires com.google.gson;
    requires java.prefs;
    requires org.mockito.junit.jupiter;
    requires com.udacity.ImageService;
    requires miglayout.core;
    exports com.udacity.SecurityService.service;
    exports com.udacity.SecurityService.application;
    opens com.udacity.SecurityService.application to com.udacity.ImageService;
    exports com.udacity.SecurityService.data;
    opens com.udacity.SecurityService.data to  com.google.gson;
    exports com.udacity.SecurityService;


}