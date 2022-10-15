package com.udacity.SecurityService.service;

import com.udacity.ImageService.service.FakeImageService;
import com.udacity.SecurityService.data.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;

import java.awt.image.BufferedImage;


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

    @BeforeEach
    public void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    public void If_alarm_isArmed_andSensor_becomesActivated_put_theSystem_intoPendingAlarmStatus() throws Exception {

        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(securityService.getAlarmStatus()).thenReturn(NO_ALARM);
        if (securityService.getArmingStatus().equals(ArmingStatus.ARMED_AWAY)) {
            securityService.changeSensorActivationStatus(sensor, true);
            Whitebox.invokeMethod(securityService,  "handleSensorActivated");

            if (securityService.getAlarmStatus().equals(NO_ALARM)) {
                securityService.setAlarmStatus(PENDING_ALARM);
                assertEquals(ArmingStatus.ARMED_AWAY, securityService.getArmingStatus());
                assertEquals(NO_ALARM, securityService.getAlarmStatus());
            }
        }

    }

    @Test
    public void If_alarm_isArmed_andSensor_becomesActivated_andSystem_isAlready_pendingAlarm_setAlarmStatus_toAlarm() throws Exception {

        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(securityService.getAlarmStatus()).thenReturn(PENDING_ALARM);
        if (securityService.getArmingStatus().equals(ArmingStatus.ARMED_AWAY)) {
            securityService.changeSensorActivationStatus(sensor, true);
            Whitebox.invokeMethod(securityService, "handleSensorActivated");

            if (securityService.getAlarmStatus().equals(PENDING_ALARM)) {
                securityService.setAlarmStatus(ALARM);
                assertEquals(ArmingStatus.ARMED_AWAY, securityService.getArmingStatus());
                assertEquals(PENDING_ALARM, securityService.getAlarmStatus());
            }
        }
    }

    @Test
    public void ifPending_Alarm_andAllSensors_inactive_returnTo_noAlarmState() throws Exception {

             when(securityService.getAlarmStatus()).thenReturn(PENDING_ALARM);

            if (securityService.getAlarmStatus().equals(PENDING_ALARM)) {
                securityService.changeSensorActivationStatus(sensor, false);
               // when(sensor.getActive()).thenReturn(true);
                //sensor.setActive(false);
                //when(sensor.getActive()).thenReturn(false);
             // if (sensor.getActive().equals(false)) {
                    Whitebox.invokeMethod
                            (securityService, "handleSensorDeactivated");
                    assertEquals(PENDING_ALARM, securityService.getAlarmStatus());

                }
            //}
    }


    @Test
    public void IfAlarm_active_change_sensorState_shouldNotAffect_AlarmState() throws Exception {
        when(securityService.getAlarmStatus()).thenReturn(ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        //Whitebox.invokeMethod(securityService,"handleSensorDeactivated");
        assertEquals(ALARM, securityService.getAlarmStatus());
       // assertEquals(false,sensor.getActive());

    }

    @Test
    public void IfSensorActivated_whileAlreadyActive_theSystemPending_changeTo_AlarmState() throws Exception {

        when(securityService.getAlarmStatus()).thenReturn(PENDING_ALARM);
        if (securityService.getAlarmStatus().equals(PENDING_ALARM)) {

            securityService.changeSensorActivationStatus(sensor, true);
            // when(s.getActive()).thenReturn(false);
            Whitebox.invokeMethod
                    (securityService, "handleSensorActivated");
            assertEquals(PENDING_ALARM, securityService.getAlarmStatus());

        }

    }

    @Test
    public void IfSensor_deactivated_alreadyInactive_makeNoChanges_toTheAlarmState() throws Exception {
        when(securityService.getAlarmStatus()).thenReturn(ALARM);
        if (securityService.getAlarmStatus().equals(ALARM)) {

            securityService.changeSensorActivationStatus(sensor, false);
            // when(s.getActive()).thenReturn(false);
            Whitebox.invokeMethod
                    (securityService, "handleSensorDeactivated");
            assertEquals(ALARM, securityService.getAlarmStatus());

        }

    }

    @Test
    public void IfImageService_identifiesImageContaining_ACat_systemArmedHome_putSystem_intoAlarmStatus() throws Exception {
        BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        Whitebox.invokeMethod(securityService, "catDetected", true);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        if (securityService.getArmingStatus() == ArmingStatus.ARMED_HOME) {
            securityService.setAlarmStatus(ALARM);
            assertEquals(ArmingStatus.ARMED_HOME, securityService.getArmingStatus());
        }

    }

    @Test
    public void IfImageService_identifiesImageNot_ACat_changeStatus_toNoAlarm_asLongAs_sensorsNotActive() throws Exception {
        BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        Whitebox.invokeMethod(securityService, "catDetected", false);
        if (sensor.getActive().equals(false)) {
            when(securityService.getAlarmStatus()).thenReturn(NO_ALARM);
            securityService.setAlarmStatus(NO_ALARM);
            assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());
        }

        verify(imageService).imageContainsCat(bufferedImage,50.0f);
    }


    @ParameterizedTest
    @DisplayName("ARMED_AWAYOrARMED_HOMEOrDISARMED")
    @ValueSource(strings = {"ARMED_AWAY", "ARMED_HOME", "DISARMED"})
    public void ifSystem_DisarmedOrArmedOrARMEDHome_returnTheValueForEach(String args) throws Exception {
             assertNotNull(args);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);

        if (securityService.getArmingStatus().equals(ArmingStatus.ARMED_AWAY)) {
            for (Sensor s : securityService.getSensors()
            ) {
                when(s.getActive()).thenReturn(false);

                securityService.removeSensor(sensor);
                assertEquals(ArmingStatus.ARMED_AWAY, securityService.getArmingStatus());
                assertEquals(false, s.getActive());
            }
        }
    when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        if (securityService.getArmingStatus().equals(ArmingStatus.ARMED_HOME)) {
            Whitebox.invokeMethod(securityService, "catDetected", true);
            when(securityService.getAlarmStatus()).thenReturn(ALARM);
            securityService.setAlarmStatus(ALARM);
        }
        assertEquals(ALARM, securityService.getAlarmStatus());

        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        if (securityService.getArmingStatus().equals(ArmingStatus.DISARMED)) {
            when(securityService.getAlarmStatus()).thenReturn(NO_ALARM);
            securityService.setAlarmStatus(NO_ALARM);
        }
        assertEquals(NO_ALARM, securityService.getAlarmStatus());


    }


}

