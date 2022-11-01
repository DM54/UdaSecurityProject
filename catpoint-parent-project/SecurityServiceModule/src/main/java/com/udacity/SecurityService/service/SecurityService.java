package com.udacity.SecurityService.service;

import com.udacity.ImageService.service.FakeImageService;
import com.udacity.SecurityService.application.SensorPanel;
import com.udacity.SecurityService.application.StatusListener;
import com.udacity.SecurityService.data.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService extends JPanel {

    private FakeImageService imageService;
    private SecurityRepository securityRepository;
    private Set<StatusListener> statusListeners = new HashSet<>();
    private BufferedImage currentCameraImage = new BufferedImage(200,200,BufferedImage.TYPE_INT_RGB);
    public SecurityService(SecurityRepository securityRepository, FakeImageService imageService) {
        this.securityRepository = new PretendDatabaseSecurityRepositoryImpl();
        this.imageService = imageService;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        if(armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        } else if (armingStatus == ArmingStatus.ARMED_HOME){
           //test 10
            for (Sensor s: securityRepository.getSensors()
                 ) {
                s.setActive(true);

                    System.out.println("this is set home to inactive " +
                            " " + s.getName() +" "+ s.getSensorType()+ " " + s.getActive());
                    changeSensorActivationStatus(s, !s.getActive());


            }
            //test 11
            securityRepository.getSensors().forEach(sensor -> {
                sensor.setActive(false);
                changeSensorActivationStatus(sensor,!sensor.getActive());
            });
        }

        securityRepository.setArmingStatus(armingStatus);
    }

    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     *
     * @param cat True if a cat is detected, otherwise false.
     * @return
     */
    private void catDetected(Boolean cat) {
        if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else {
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
        if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
         setAlarmStatus(AlarmStatus.ALARM);
            return;
        }
        switch(securityRepository.getAlarmStatus()) {
            case NO_ALARM ->
                // if(securityRepository.getArmingStatus() == ArmingStatus.ARMED_HOME && AlarmStatus.NO_ALARM.equals(securityRepository.getAlarmStatus())) {
                setAlarmStatus(AlarmStatus.PENDING_ALARM);

            case PENDING_ALARM -> {
               // if (ArmingStatus.ARMED_HOME == securityRepository.getArmingStatus() && AlarmStatus.PENDING_ALARM.equals(securityRepository.getAlarmStatus())) {
                    setAlarmStatus(AlarmStatus.ALARM);
                //}
            }

            default -> {

                System.out.println("this is the default from handleSensorActivated");}
        };
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated
     */
    private void handleSensorDeactivated() {

        switch(securityRepository.getAlarmStatus()) {
            case PENDING_ALARM -> {

                    setAlarmStatus(AlarmStatus.NO_ALARM);
                }

            case ALARM -> {
                    setAlarmStatus(AlarmStatus.PENDING_ALARM);
            }

            default -> {

                System.out.println("this is the default from handleSensorDeactivated");
            }
        };
    }

    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        if(!sensor.getActive() && active) {
            System.out.println(" this is !sensor.getActive and active " +
                    " "+" "+ sensor.getName() +" "+ sensor.getActive() +" "+ sensor.getSensorType());
            handleSensorActivated();
        } else if (sensor.getActive() && !active) {
            System.out.println(" this is sensor.getActive and not active " +
                    " "+sensor.getName() +" "+ sensor.getActive() +" "+ sensor.getSensorType());
            handleSensorDeactivated();
        }

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);
    }

    /**
     * Send an image to the SecurityService for processing. The securityService will use its provided
     * ImageService to analyze the image for cats and update the alarm status accordingly.
     * @param currentCameraImage
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));

        securityRepository.getSensors().stream().forEach(sensor -> {

            if (imageService.imageContainsCat(currentCameraImage,50.0f) == false && sensor.getActive().equals(false)) {
               // catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
                setAlarmStatus(AlarmStatus.NO_ALARM);
            } else if (imageService.imageContainsCat(currentCameraImage, 50.0f) == true &&
                    ArmingStatus.ARMED_HOME.equals(securityRepository.getArmingStatus())) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        });

    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }

}
