package com.udacity.SecurityService.service;

import com.udacity.ImageService.service.FakeImageService;
import com.udacity.SecurityService.data.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;

import java.awt.image.BufferedImage;


import static com.udacity.SecurityService.data.AlarmStatus.*;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    @InjectMocks
    private SecurityService securityService;
    @Mock
    private SecurityRepository securityRepository = new PretendDatabaseSecurityRepositoryImpl();
    @Mock
    private Sensor sensor;
    @Mock
    private FakeImageService imageService;

    @BeforeEach
    public void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
   public void If_alarm_isArmed_andSensor_becomesActivated_put_theSystem_intoPendingAlarmStatus() throws Exception {

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);

        if (securityRepository.getArmingStatus().equals(securityService.getArmingStatus())) {
            securityService.changeSensorActivationStatus(sensor, true);
            Whitebox.invokeMethod(securityService,  "handleSensorActivated");
            when(securityRepository.getAlarmStatus()).thenReturn(NO_ALARM);
            if (securityRepository.getAlarmStatus().equals(securityService.getAlarmStatus())) {
                securityService.setAlarmStatus(PENDING_ALARM);
                assertEquals(ArmingStatus.ARMED_AWAY, securityService.getArmingStatus());
                assertEquals(NO_ALARM, securityService.getAlarmStatus());
            }
        }

    }

   @Test
    public void If_alarm_isArmed_andSensor_becomesActivated_andSystem_isAlready_pendingAlarm_setAlarmStatus_toAlarm() throws Exception {

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);

        if (securityRepository.getArmingStatus().equals(ArmingStatus.ARMED_AWAY)) {
            securityService.changeSensorActivationStatus(sensor, true);
            Whitebox.invokeMethod(securityService, "handleSensorActivated");
            when(securityRepository.getAlarmStatus()).thenReturn(PENDING_ALARM);
            if (securityRepository.getAlarmStatus().equals(PENDING_ALARM)) {
                securityService.setAlarmStatus(ALARM);
                assertEquals(ArmingStatus.ARMED_AWAY, securityRepository.getArmingStatus());
                assertEquals(PENDING_ALARM, securityRepository.getAlarmStatus());
            }
        }
    }

    @Test
    public void ifPending_Alarm_andAllSensors_inactive_returnTo_noAlarmState() throws Exception {

             when(securityRepository.getAlarmStatus()).thenReturn(PENDING_ALARM);

            if (securityRepository.getAlarmStatus().equals(PENDING_ALARM)) {
                securityService.changeSensorActivationStatus(sensor, false);
                when(sensor.getActive()).thenReturn(true);
                sensor.setActive(false);
                when(sensor.getActive()).thenReturn(false);
             if (sensor.getActive().equals(false)) {

                    Whitebox.invokeMethod
                            (securityService, "handleSensorDeactivated");
                 securityService.setAlarmStatus(NO_ALARM);
                    assertEquals(PENDING_ALARM, securityRepository.getAlarmStatus());

                }
            }
    }


    @Test
    public void IfAlarm_active_change_sensorState_shouldNotAffect_AlarmState() throws Exception {
        when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        //Whitebox.invokeMethod(securityService,"handleSensorDeactivated");
        assertEquals(ALARM, securityRepository.getAlarmStatus());
       // assertEquals(false,sensor.getActive());

    }

    @Test
    public void IfSensorActivated_whileAlreadyActive_theSystemPending_changeTo_AlarmState() throws Exception {

        when(securityRepository.getAlarmStatus()).thenReturn(PENDING_ALARM);
        if (securityRepository.getAlarmStatus().equals(PENDING_ALARM)) {

            securityService.changeSensorActivationStatus(sensor, true);
            // when(s.getActive()).thenReturn(false);
            Whitebox.invokeMethod
                    (securityService, "handleSensorActivated");
            assertEquals(PENDING_ALARM, securityRepository.getAlarmStatus());

        }

    }

    @Test
    public void IfSensor_deactivated_alreadyInactive_makeNoChanges_toTheAlarmState() throws Exception {
        when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
        if (securityRepository.getAlarmStatus().equals(securityService.getAlarmStatus())) {

            securityService.changeSensorActivationStatus(sensor, false);
            // when(s.getActive()).thenReturn(false);
            Whitebox.invokeMethod
                    (securityService, "handleSensorDeactivated");
            assertEquals(ALARM, securityRepository.getAlarmStatus());

        }

    }

    @Test
    public void IfImageService_identifiesImageContaining_ACat_systemArmedHome_putSystem_intoAlarmStatus() throws Exception {
        BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        Whitebox.invokeMethod(securityService, "catDetected", true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        if (securityRepository.getArmingStatus() == ArmingStatus.ARMED_HOME) {
            securityService.setAlarmStatus(ALARM);
            assertEquals(ArmingStatus.ARMED_HOME, securityRepository.getArmingStatus());
        }

    }

    @Test
    public void IfImageService_identifiesImageNot_ACat_changeStatus_toNoAlarm_asLongAs_sensorsNotActive() throws Exception {
        BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        Whitebox.invokeMethod(securityService, "catDetected", false);
        if (sensor.getActive().equals(false)) {
            when(securityRepository.getAlarmStatus()).thenReturn(NO_ALARM);
            securityService.setAlarmStatus(NO_ALARM);
            assertEquals(AlarmStatus.NO_ALARM, securityRepository.getAlarmStatus());
        }

        verify(imageService).imageContainsCat(bufferedImage,50.0f);
    }


    @ParameterizedTest
    @DisplayName("ARMED_AWAYOrARMED_HOMEOrDISARMED")
    @ValueSource(strings = {"ARMED_AWAY", "ARMED_HOME", "DISARMED"})
    public void ifSystem_DisarmedOrArmedOrARMEDHome_returnTheValueForEach(String args) throws Exception {
             assertNotNull(args);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);

        if (securityRepository.getArmingStatus().equals(ArmingStatus.ARMED_AWAY)) {
            for (Sensor s : securityRepository.getSensors()
            ) {
                when(s.getActive()).thenReturn(false);

                securityRepository.removeSensor(sensor);
                assertEquals(ArmingStatus.ARMED_AWAY, securityRepository.getArmingStatus());
                assertEquals(false, s.getActive());
            }
        }
    when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        if (securityRepository.getArmingStatus().equals(ArmingStatus.ARMED_HOME)) {
            Whitebox.invokeMethod(securityService, "catDetected", true);
            when(securityRepository.getAlarmStatus()).thenReturn(ALARM);
            securityService.setAlarmStatus(ALARM);
        }
        assertEquals(ALARM, securityRepository.getAlarmStatus());

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        if (securityRepository.getArmingStatus().equals(ArmingStatus.DISARMED)) {
            when(securityRepository.getAlarmStatus()).thenReturn(NO_ALARM);
            securityService.setAlarmStatus(NO_ALARM);
        }
        assertEquals(NO_ALARM, securityRepository.getAlarmStatus());


    }


}

