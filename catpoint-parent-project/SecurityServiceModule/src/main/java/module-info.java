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
    opens com.udacity.SecurityService.application to com.udacity.ImageService;
    opens com.udacity.SecurityService.data to  com.google.gson;
}