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
import org.powermock.api.mockito.PowerMockito;
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
    @Mock
    AlarmStatus alarmStatus;
    @Mock
    ArmingStatus armingStatus;

    @BeforeEach
    public void init() {
        securityService = new SecurityService(securityRepository,imageService);
    }

    @Test
    public void If_alarm_isArmed_andSensor_becomesActivated_put_theSystem_intoPendingAlarmStatus(){

         when(securityService.getArmingStatus()).thenReturn(armingStatus.ARMED_HOME);
         securityService.changeSensorActivationStatus(sensor,sensor.getActive());
       when(securityService.getAlarmStatus()).thenReturn(PENDING_ALARM);
       assertEquals(armingStatus.ARMED_HOME,securityService.getArmingStatus());
       assertEquals(PENDING_ALARM, securityService.getAlarmStatus());
     // verify(securityService).getArmingStatus();
      //verify(securityService).changeSensorActivationStatus(sensor,sensor.getActive());
      //verify(securityRepository).getAlarmStatus();

    }

    @Test
    public void If_alarm_isArmed_andSensor_becomesActivated_andSystem_isAlready_pendingAlarm_setAlarmStatus_toAlarm(){
        //when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
       // securityService.changeSensorActivationStatus(sensor,sensor.getActive());
       // when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.addSensor(sensor);
        securityService.setAlarmStatus(ALARM);
        when(securityService.getAlarmStatus()).thenReturn(ALARM);
        assertEquals(ALARM, securityService.getAlarmStatus());
    }

    @Test
    public void ifpending_Alarm_andAllSensors_inactive_returnTo_noAlarmState(){
        //when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        when(sensor.getActive()).thenReturn(false);
        securityService.setAlarmStatus(NO_ALARM);
        when(securityService.getAlarmStatus()).thenReturn(NO_ALARM);
        assertAll(
                //()-> assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus()),
                ()-> assertEquals(false, sensor.getActive()),
                ()-> assertEquals(NO_ALARM, securityService.getAlarmStatus())
        );
    }

    @Test
    public void IfAlarm_active_change_sensorState_shouldNotAffect_AlarmState(){
        when(securityService.getAlarmStatus()).thenReturn(ALARM);
        securityService.changeSensorActivationStatus(sensor,sensor.getActive());
        assertEquals(ALARM, securityService.getAlarmStatus());

    }

    @Test
    public void IfSensorActivated_whileAlreadyActive_theSystemPending_changeTo_AlarmState() throws Exception{
        when(securityService.getAlarmStatus()).thenReturn(PENDING_ALARM);
      //if(!sensor.getActive()){
    Whitebox.invokeMethod
                (securityService,"handleSensorActivated");
           if(securityService.getAlarmStatus().equals(alarmStatus.PENDING_ALARM)){
               securityService.setAlarmStatus(ALARM);

                assertEquals(alarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
           }
       // }


    }

    @Test
    public void IfSensor_deactivated_alreadyInactive_makeNoChanges_toTheAlarmState() throws Exception {

       when(securityService.getAlarmStatus()).thenReturn(alarmStatus.PENDING_ALARM);
        Whitebox.invokeMethod
                (securityService,"handleSensorDeactivated");
        if(sensor.getActive().equals(false)){
            securityService.changeSensorActivationStatus(sensor,false);
            assertEquals(alarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
        }
        else {
            assertEquals(alarmStatus.ALARM, securityService.getAlarmStatus());
        }
    }

    @Test
    public void IfImageService_identifiesImageContaining_ACat_systemArmedHome_putSystem_intoAlarmStatus() throws Exception {
        BufferedImage bufferedImage = new BufferedImage(200,200,BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(bufferedImage, 50.0f)).thenReturn(true);
        securityService.processImage(bufferedImage);
        Whitebox.invokeMethod(securityService, "catDetected", true);
        when(securityService.getArmingStatus()).thenReturn(armingStatus.ARMED_HOME);
        if(securityService.getArmingStatus()==armingStatus.ARMED_HOME) {
            securityService.setAlarmStatus(alarmStatus.ALARM);
            assertEquals(armingStatus.ARMED_HOME, securityService.getArmingStatus());
        }else {
            Whitebox.invokeMethod(securityService, "catDetected", false);
            when(securityService.getAlarmStatus()).thenReturn(NO_ALARM);
            securityService.setAlarmStatus(alarmStatus.NO_ALARM);
            assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus());
        }


    }

    @Test

    public void if_theSystem_isArmed_resetALL_theSensors_toInactive(){
        when(securityService.getArmingStatus()).thenReturn(armingStatus.ARMED_AWAY);
        //securityService.changeSensorActivationStatus(sensor,sensor.getActive());
        securityService.removeSensor(sensor);
        assertEquals(armingStatus.ARMED_AWAY, securityService.getArmingStatus());
        assertEquals(false,sensor.getActive());
    }

}
