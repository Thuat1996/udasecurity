module com.udacity.catpoint {
    requires com.miglayout.swing;
    requires com.udacity.catpoint.image;
    requires java.desktop;
    requires com.google.gson;
    requires java.prefs;
    requires com.google.common;
    opens com.udacity.catpoint.data to com.google.gson;
    exports com.udacity.catpoint.service;
}