module com.udacity.catpoint {
    requires com.miglayout.swing;
    requires com.udacity.catpoint.image;
    requires java.desktop;
    requires com.google.gson;
    requires java.prefs;
    requires com.google.common;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
    opens com.udacity.catpoint.data to com.google.gson;
}