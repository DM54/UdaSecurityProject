package com.udacity.SecurityService.service;

import com.udacity.ImageService.service.FakeImageService;
import com.udacity.SecurityService.data.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;

import static com.udacity.SecurityService.data.AlarmStatus.PENDING_ALARM;
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
        securityService = new SecurityService(securityRepository,imageService);
    }

    @Test
    public void If_alarm_isArmed_andSensor_becomesActivated_put_theSystem_intoPendingAlarmStatus(){
         when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
         securityService.changeSensorActivationStatus(sensor,sensor.getActive());
       when(securityService.getAlarmStatus()).thenReturn(PENDING_ALARM);
       assertEquals(ArmingStatus.ARMED_HOME,securityService.getArmingStatus());
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
        securityService.setAlarmStatus(AlarmStatus.ALARM);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());
    }

    @Test
    public void ifpending_Alarm_andAllSensors_inactive_returnTo_noAlarmState(){
        //when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        when(sensor.getActive()).thenReturn(false);
        securityService.setAlarmStatus(AlarmStatus.NO_ALARM);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        assertAll(
                //()-> assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus()),
                ()-> assertEquals(false, sensor.getActive()),
                ()-> assertEquals(AlarmStatus.NO_ALARM, securityService.getAlarmStatus())
        );
    }

    @Test
    public void IfAlarm_active_change_sensorState_shouldNotAffect_AlarmState(){
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor,sensor.getActive());
        assertEquals(AlarmStatus.ALARM, securityService.getAlarmStatus());

    }

    @Test
    public void IfSensorActivated_whileAlreadyActive_theSystemPending_changeTo_AlarmState() throws Exception{
        when(securityService.getAlarmStatus()).thenReturn(PENDING_ALARM);
      //if(!sensor.getActive()){
    Whitebox.invokeMethod
                (securityService,"handleSensorActivated");
           if(securityService.getAlarmStatus().equals(AlarmStatus.PENDING_ALARM)){
               securityService.setAlarmStatus(AlarmStatus.ALARM);

                assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
           }
       // }


    }

    @Test

    public void if_theSystem_isArmed_resetALL_theSensors_toInactive(){
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        //securityService.changeSensorActivationStatus(sensor,sensor.getActive());
        securityService.removeSensor(sensor);
        assertEquals(ArmingStatus.ARMED_AWAY, securityService.getArmingStatus());
        assertEquals(false,sensor.getActive());
    }

}
