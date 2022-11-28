package com.udacity.SecurityService.service;

import com.udacity.ImageService.service.FakeImageService;
import com.udacity.SecurityService.application.SensorPanel;
import com.udacity.SecurityService.application.StatusListener;
import com.udacity.SecurityService.data.*;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService{

    private final FakeImageService imageService;
    private final SecurityRepository securityRepository;
    private final Set<StatusListener> statusListeners = new HashSet<>();
    private  Sensor sensor = new Sensor();
    private boolean cats = false;

    public SecurityService(SecurityRepository securityRepository, FakeImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        } else if (armingStatus == ArmingStatus.ARMED_HOME && cats) {
               setAlarmStatus(AlarmStatus.ALARM);

        }else if(ArmingStatus.ARMED_HOME==armingStatus || ArmingStatus.ARMED_AWAY==armingStatus){
            Iterator<Sensor> sensorIterator = getSen().iterator();

                while (sensorIterator.hasNext()){
                    Sensor sensor1 = sensorIterator.next();
                    sensor1.setActive(true);
                    changeSensorActivationStatus(sensor1, false);
                        }
        }
        securityRepository.setArmingStatus(armingStatus);
    }
    public Set<Sensor> getSen(){
        Set<Sensor> sensorSet = new HashSet<>();
        sensorSet.addAll(securityRepository.getSensors());
        return sensorSet;

    }

    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     *
     * @param cat True if a cat is detected, otherwise false.
     * @return
     */
    private void catDetected(Boolean cat) {
        cats = cat;
        if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if(!cat) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
        statusListeners.forEach(sl -> sl.catDetected(cat));
    }
    /**
     * Register the StatusListener for alarm system updates from within the SecurityService.
     * @param statusListener
     */
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }
    /**
     * Change the alarm status of the system and notify all listeners.
     * @param status
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    /**
     * Internal method for updating the alarm status when a sensor has been activated.
     */
    private void handleSensorActivated() {
        securityRepository.getAlarmStatus();
        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return;
        }
       // if (securityRepository.getArmingStatus()==ArmingStatus.ARMED_AWAY || securityRepository.getArmingStatus()==ArmingStatus.ARMED_HOME) {
            switch (securityRepository.getAlarmStatus()) {
                case NO_ALARM ->
                    setAlarmStatus(AlarmStatus.PENDING_ALARM);
                case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
                default -> System.out.println("this is the default from handleSensorActivated");}}




      /**
       * Internal method for updating the alarm status when a sensor has been deactivated
       */
    private void handleSensorDeactivated() {
        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return;
        }
            switch (securityRepository.getAlarmStatus()) {
                case PENDING_ALARM ->setAlarmStatus(AlarmStatus.NO_ALARM);
                //case ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
                default ->{}}}
    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        if(active && !sensor.getActive()) {handleSensorActivated();}
        if (sensor.getActive() && !active) {handleSensorDeactivated();}
        sensor.setActive(active);
        securityRepository.updateSensor(sensor);}
    /**
     * Send an image to the SecurityService for processing. The securityService will use its provided
     * ImageService to analyze the image for cats and update the alarm status accordingly.
     * @param currentCameraImage
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));}
    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {securityRepository.addSensor(sensor);}

    public void removeSensor(Sensor sensor) {securityRepository.removeSensor(sensor);}

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}
