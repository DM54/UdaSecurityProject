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

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService{

    private FakeImageService imageService;
    private transient SecurityRepository securityRepository;
    private Set<StatusListener> statusListeners = new HashSet<>();

    private BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

    private  Sensor sensor = new Sensor();
    private boolean cats;
    private boolean actived;


    public SecurityService(SecurityRepository securityRepository, FakeImageService imageService) {
        this.securityRepository = new PretendDatabaseSecurityRepositoryImpl();
        this.imageService = imageService;
        sensor = new Sensor();
    }


    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        if (armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        } else if (armingStatus == ArmingStatus.ARMED_HOME || armingStatus == ArmingStatus.ARMED_AWAY) {
                if(cats) {
                    setAlarmStatus(AlarmStatus.ALARM);
                } else if(sensor.getActive().equals(true) && sensor.getSensorId().equals(sensor)){
                       sensor.setActive(false);
                       try {
                            Method method = SensorPanel.class.getDeclaredMethod("setSensorActivity", Sensor.class, Boolean.class);
                            method.setAccessible(true);
                            method.invoke(new SensorPanel(), sensor,sensor.getActive());
                            Method method1 = SensorPanel.class.getDeclaredMethod("updateSensorList", JPanel.class);
                            method1.setAccessible(true);
                            method1.invoke(new SensorPanel(), new JPanel());

                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                }
                changeSensorActivationStatus(sensor,sensor.getActive());

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
        if (securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return;
        }
       // if (securityRepository.getArmingStatus()==ArmingStatus.ARMED_AWAY || securityRepository.getArmingStatus()==ArmingStatus.ARMED_HOME) {
            switch (securityRepository.getAlarmStatus()) {
                case NO_ALARM -> {
                    setAlarmStatus(AlarmStatus.PENDING_ALARM);
                }
                case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
                default -> System.out.println("this is the default from handleSensorActivated");
         //   }
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated
     */
    private void handleSensorDeactivated() {
       // if(securityRepository.getArmingStatus()==ArmingStatus.ARMED_AWAY || securityRepository.getArmingStatus()==ArmingStatus.ARMED_HOME) {
            switch (securityRepository.getAlarmStatus()) {
                case PENDING_ALARM -> setAlarmStatus(AlarmStatus.NO_ALARM);
                case ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
                default -> System.out.println("this is the default from handleSensorDeactivated");
            }
        //}
    }

    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        actived=active;
        sensor = sensor;
        if(!sensor.getActive() && active) {
            handleSensorActivated();
        } else if (sensor.getActive() && !active) {
            handleSensorDeactivated();
            if (AlarmStatus.PENDING_ALARM==(getAlarmStatus()) && sensor.getActive()) {
                handleSensorDeactivated();
            }
        }else if (getArmingStatus()==(ArmingStatus.DISARMED) && AlarmStatus.ALARM==(getAlarmStatus())) {
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
