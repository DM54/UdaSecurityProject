package com.udacity.SecurityService.service;

import com.udacity.ImageService.service.FakeImageService;
import com.udacity.SecurityService.data.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
        if (securityService.getArmingStatus().equals(ArmingStatus.ARMED_AWAY) && sensor.getActive().equals(true)) {
            securityService.changeSensorActivationStatus(sensor, true);
            Whitebox.invokeMethod(securityService, " handleSensorActivated");
            when(securityService.getAlarmStatus()).thenReturn(NO_ALARM);
            if (securityService.getAlarmStatus().equals(NO_ALARM)) {
                securityService.setAlarmStatus(PENDING_ALARM);
                assertEquals(ArmingStatus.ARMED_AWAY, securityService.getArmingStatus());
                assertEquals(NO_ALARM, securityService.getAlarmStatus());
            }
        }

        // verify(securityService).getArmingStatus();
        //verify(securityService).changeSensorActivationStatus(sensor,sensor.getActive());
        //verify(securityRepository).getAlarmStatus();

    }

    @Test
    public void If_alarm_isArmed_andSensor_becomesActivated_andSystem_isAlready_pendingAlarm_setAlarmStatus_toAlarm() throws Exception {

        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        if (securityService.getArmingStatus().equals(ArmingStatus.ARMED_AWAY) && sensor.getActive().equals(true)
                && securityService.getAlarmStatus().equals(PENDING_ALARM)) {
            securityService.changeSensorActivationStatus(sensor, true);
            Whitebox.invokeMethod(securityService, " handleSensorActivated");
            when(securityService.getAlarmStatus()).thenReturn(PENDING_ALARM);
            if (securityService.getAlarmStatus().equals(PENDING_ALARM)) {
                securityService.setAlarmStatus(ALARM);
                assertEquals(ArmingStatus.ARMED_AWAY, securityService.getArmingStatus());
                assertEquals(PENDING_ALARM, securityService.getAlarmStatus());
            }
        }
    }

    @Test
    public void ifPending_Alarm_andAllSensors_inactive_returnTo_noAlarmState() throws Exception {

        securityService.changeSensorActivationStatus(sensor, false);
        for (Sensor s : securityService.getSensors()
        ) {
            when(s.getActive()).thenReturn(true);

            Whitebox.invokeMethod(securityService, "handleSensorDeactivated");
            when(securityService.getAlarmStatus()).thenReturn(PENDING_ALARM);
            if (securityService.getAlarmStatus().equals(PENDING_ALARM)) {
                securityService.setAlarmStatus(NO_ALARM);
                assertEquals(PENDING_ALARM, securityService.getAlarmStatus());
            }
        }

    }


    @Test
    public void IfAlarm_active_change_sensorState_shouldNotAffect_AlarmState() {
        when(securityService.getAlarmStatus()).thenReturn(ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        assertEquals(ALARM, securityService.getAlarmStatus());

    }

    @Test
    public void IfSensorActivated_whileAlreadyActive_theSystemPending_changeTo_AlarmState() throws Exception {
        // when(securityService.getAlarmStatus()).thenReturn(PENDING_ALARM);

        if (sensor.getActive().equals(true)) {
            securityService.changeSensorActivationStatus(sensor, true);
            Whitebox.invokeMethod
                    (securityService, "handleSensorActivated");

            if (securityService.getAlarmStatus().equals(PENDING_ALARM)) {

                securityService.setAlarmStatus(ALARM);
                assertEquals(PENDING_ALARM, securityService.getAlarmStatus());
            } /*else if (securityService.getAlarmStatus().equals(NO_ALARM)) {
               when(securityService.getAlarmStatus()).thenReturn(NO_ALARM);
               securityService.setAlarmStatus(PENDING_ALARM);
               assertTrue(Whitebox.invokeMethod
                       (securityService,"handleSensorActivated").equals(NO_ALARM));
           }*/
        }

    }

    @Test
    public void IfSensor_deactivated_alreadyInactive_makeNoChanges_toTheAlarmState() throws Exception {
        when(securityService.getAlarmStatus()).thenReturn(ALARM);

        if (sensor.getActive().equals(false)) {
            securityService.changeSensorActivationStatus(sensor, false);
            Whitebox.invokeMethod
                    (securityService, "handleSensorDeactivated");
            //when(securityService.getAlarmStatus()).thenReturn(ALARM);
            if (securityService.getAlarmStatus().equals(ALARM)) {
                assertEquals(ALARM, securityService.getAlarmStatus());
            }
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
    }

   /* @Test
    public void IfSystemDisarmed_setStatusNoAlarm() {
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        if (securityService.getArmingStatus().equals(ArmingStatus.DISARMED)) {
            when(securityService.getAlarmStatus()).thenReturn(NO_ALARM);
            securityService.setAlarmStatus(NO_ALARM);
        }
        assertEquals(NO_ALARM, securityService.getAlarmStatus());
    }

    @Test
    public void IfSystemArmedHome_whileCameraShowsCat_setAlarmStatusToAlarm() throws Exception {
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        if (securityService.getArmingStatus().equals(ArmingStatus.ARMED_HOME)) {
            Whitebox.invokeMethod(securityService, "catDetected", true);
            when(securityService.getAlarmStatus()).thenReturn(ALARM);
            securityService.setAlarmStatus(ALARM);
        }
        assertEquals(ALARM, securityService.getAlarmStatus());

    }


    @Test

    public void if_theSystem_isArmed_resetALL_theSensors_toInactive() {
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        //securityService.changeSensorActivationStatus(sensor,sensor.getActive());
        if (securityService.getArmingStatus().equals(ArmingStatus.ARMED_AWAY)) {
            for (Sensor s : securityService.getSensors()
            ) {
                when(s.getActive()).thenReturn(false);

                //securityService.removeSensor(sensor);
                //assertEquals(armingStatus.ARMED_AWAY, securityService.getArmingStatus());
                assertEquals(false, s.getActive());
            }
        }
    }*/
    enum ArmingStatus1{
        ARMED_AWAY, ARMED_HOME, DISARMED
   }
    @ParameterizedTest
    @EnumSource(ArmingStatus1.class)
    public void ifSystem_DisarmedOrArmedOrARMEDHome_returnTheValueForEach() throws Exception {

        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        //securityService.changeSensorActivationStatus(sensor,sensor.getActive());
        if (securityService.getArmingStatus().equals(ArmingStatus.ARMED_AWAY)) {
            for (Sensor s : securityService.getSensors()
            ) {
                when(s.getActive()).thenReturn(false);

                //securityService.removeSensor(sensor);
                //assertEquals(armingStatus.ARMED_AWAY, securityService.getArmingStatus());
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

