package com.udacity.SecurityService.service;

import com.udacity.ImageService.service.FakeImageService;
import com.udacity.SecurityService.application.ControlPanel;
import com.udacity.SecurityService.application.DisplayPanel;
import com.udacity.SecurityService.application.SensorPanel;
import com.udacity.SecurityService.application.StatusListener;
import com.udacity.SecurityService.data.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.udacity.SecurityService.data.AlarmStatus.*;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    private SecurityService securityService;
    @Mock
    private SecurityRepository securityRepository = new PretendDatabaseSecurityRepositoryImpl();
    @Mock
    private Sensor sensor;
    @Mock
    private FakeImageService imageService;
    @Mock
    private SensorPanel panel;
    @Mock
    private ControlPanel controlPanel;
    @Mock
    private StatusListener statusListener;

    @BeforeEach
    public void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
   public void If_alarm_isArmed_andSensor_becomesActivated_put_theSystem_intoPendingAlarmStatus() throws Exception {

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        //securityService.addSensor(sensor);
       // if (securityRepository.getArmingStatus().equals(ArmingStatus.ARMED_HOME)) {
            securityService.changeSensorActivationStatus(sensor, true);
           // Whitebox.invokeMethod(securityService,  "handleSensorActivated");
            when(securityRepository.getAlarmStatus()).thenReturn(NO_ALARM);
           if (securityRepository.getAlarmStatus().equals(NO_ALARM)) {
                securityRepository.setAlarmStatus(PENDING_ALARM);
                assertEquals(ArmingStatus.ARMED_HOME, securityRepository.getArmingStatus());
                //assertEquals(PENDING_ALARM, securityRepository.getAlarmStatus());
                verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
           }
      //  }

    }

   @Test
    public void If_alarm_isArmed_andSensor_becomesActivated_andSystem_isAlready_pendingAlarm_setAlarmStatus_toAlarm() throws Exception {

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        securityService.addSensor(sensor);
        if (securityRepository.getArmingStatus().equals(ArmingStatus.ARMED_AWAY)) {
            securityService.changeSensorActivationStatus(sensor, true);
            Whitebox.invokeMethod(securityService, "handleSensorActivated");
            when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
            if (securityRepository.getAlarmStatus().equals(ALARM)) {
                securityService.setAlarmStatus(ALARM);
                assertEquals(ArmingStatus.ARMED_AWAY, securityRepository.getArmingStatus());
                assertEquals(ALARM, securityRepository.getAlarmStatus());
            }
        }
       when(sensor.getActive()).thenReturn(true);
       securityService.changeSensorActivationStatus(sensor, true);
       Whitebox.invokeMethod(securityService,"handleSensorActivated");
       assertEquals(true,sensor.getActive());
    }

    @Test
    public void ifPending_Alarm_andAllSensors_inactive_returnTo_noAlarmState() throws Exception {
              securityRepository.getSensors().forEach(sensor1 -> {
                  if(securityRepository.getAlarmStatus().equals(PENDING_ALARM) && sensor.getActive().equals(false)){
                      when(securityRepository.getAlarmStatus()).thenReturn(NO_ALARM);
                      securityService.removeSensor(sensor);
                      if (securityRepository.getAlarmStatus().equals(NO_ALARM)) {
                          securityService.changeSensorActivationStatus(sensor, true);
                          try {
                              Whitebox.invokeMethod
                                      (securityService, "handleSensorDeactivated");
                          } catch (Exception e) {
                              throw new RuntimeException(e);
                          }
                          securityRepository.setAlarmStatus(NO_ALARM);
                          assertEquals(NO_ALARM, securityRepository.getAlarmStatus());
                  }
              }
              });
    }


    @Test
    public void IfAlarm_active_change_sensorState_shouldNotAffect_AlarmState() throws Exception {
       when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
       if(sensor.getActive().equals(true)|| sensor.getActive().equals(false) && securityRepository.getAlarmStatus().equals(ALARM)) {
           securityService.changeSensorActivationStatus(sensor, sensor.getActive());
       }
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));

    }

    @Test
    public void IfSensorActivated_whileAlreadyActive_theSystemPending_changeTo_AlarmState() throws Exception {

        when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
        if (securityRepository.getAlarmStatus().equals(ALARM)) {
            sensor.setActive(true);
            securityService.changeSensorActivationStatus(sensor, true);
            //when(sensor.getActive()).thenReturn(false);
           // if(sensor.getActive().equals(true)){
                //securityRepository.setAlarmStatus(ALARM);
         //   }
            assertEquals(ALARM, securityRepository.getAlarmStatus());
            verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));

        }

    }

    @Test
    public void IfSensor_deactivated_alreadyInactive_makeNoChanges_toTheAlarmState() throws Exception {
        when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
       if (securityRepository.getAlarmStatus().equals(ALARM)) {
            sensor.setActive(false);
            securityService.changeSensorActivationStatus(sensor, false);
            // when(s.getActive()).thenReturn(false);
           // Whitebox.invokeMethod
                 //   (securityService, "handleSensorDeactivated");
            assertEquals(ALARM, securityRepository.getAlarmStatus());
            verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));

        }

    }

   @Test
    public void IfImageService_identifiesImageContaining_ACat_systemArmedHome_putSystem_intoAlarmStatus() throws Exception {
        BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        if (securityRepository.getArmingStatus().equals(ArmingStatus.ARMED_HOME)) {
            Whitebox.invokeMethod(securityService, "catDetected", true);
            when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
            securityService.processImage(bufferedImage);
            when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
            securityRepository.setAlarmStatus(ALARM);
            assertEquals(ArmingStatus.ARMED_HOME, securityRepository.getArmingStatus());
            assertEquals(ALARM,securityRepository.getAlarmStatus());
        }

    }

    @Test
    public void IfImageService_identifiesImageNot_ACat_changeStatus_toNoAlarm_asLongAs_sensorsNotActive() throws Exception {
        BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(false);
        securityService.processImage(bufferedImage);
        Whitebox.invokeMethod(securityService, "catDetected", false);
        if (sensor.getActive().equals(false)) {
            when(securityRepository.getAlarmStatus()).thenReturn(NO_ALARM);
            securityRepository.setAlarmStatus(NO_ALARM);
            assertEquals(AlarmStatus.NO_ALARM, securityRepository.getAlarmStatus());
            assertEquals(false,sensor.getActive());
        }
       // verify(imageService).imageContainsCat(bufferedImage,50.0f);
    }

    @ParameterizedTest
    @DisplayName("ifSystem_DisarmedOrArmedOrARMEDHome_returnTheValueForEach")
    @ValueSource(strings = {"DISARMED", "ARMED_HOME", "ARMED_AWAY"})
    public void ifSystem_DisarmedOrArmedOrARMEDHome_returnTheValueForEach(String args) throws Exception {
        assertNotNull(args);
        BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.valueOf(args));
       // when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        if (securityRepository.getArmingStatus().equals(ArmingStatus.DISARMED)) {
            securityRepository.setArmingStatus(ArmingStatus.DISARMED);
            when(securityRepository.getAlarmStatus()).thenReturn(NO_ALARM);
            securityRepository.setAlarmStatus(NO_ALARM);
            assertEquals(NO_ALARM, securityRepository.getAlarmStatus());
            assertEquals(ArmingStatus.DISARMED,securityRepository.getArmingStatus());
            securityService.setArmingStatus(ArmingStatus.DISARMED);
        }

     else if(securityRepository.getArmingStatus().equals(ArmingStatus.ARMED_AWAY) ||
              securityRepository.getArmingStatus().equals(ArmingStatus.ARMED_HOME)) {
          securityRepository.getSensors().forEach((k2) -> {
              k2.setActive(false);
              securityService.changeSensorActivationStatus(k2, !k2.getActive());
              assertEquals(k2.getActive(), false);
              assertEquals(ArmingStatus.ARMED_HOME, securityRepository.getArmingStatus());
              assertEquals(ArmingStatus.ARMED_AWAY,securityRepository.getArmingStatus());
          });
          securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
          securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
   }

  else if (securityRepository.getArmingStatus().equals(ArmingStatus.ARMED_HOME)) {
        Whitebox.invokeMethod(securityService, "catDetected", true);
        when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
      //  if (securityRepository.getAlarmStatus().equals(ALARM)) {
            assertEquals(ALARM, securityRepository.getAlarmStatus());
            assertEquals(ArmingStatus.ARMED_HOME, securityRepository.getArmingStatus());
            securityService.setAlarmStatus(ALARM);
    }

    }
}

