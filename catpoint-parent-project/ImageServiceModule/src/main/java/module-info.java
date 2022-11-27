module com.udacity.ImageService {
    requires java.desktop;
    requires org.slf4j;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    exports com.udacity.ImageService.service to com.udacity.SecurityService;


}