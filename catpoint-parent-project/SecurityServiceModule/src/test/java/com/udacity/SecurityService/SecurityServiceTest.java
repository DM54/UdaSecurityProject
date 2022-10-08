package com.udacity.SecurityService;


import com.udacity.ImageService.application.ImageService;
import com.udacity.ImageService.service.FakeImageService;
import com.udacity.SecurityService.data.*;
import com.udacity.SecurityService.service.SecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void init(){
        securityService = new SecurityService(securityRepository,imageService);
    }

    @ParameterizedTest
    public void If_alarm_isArmed_andSensor_becomesActivated_put_theSystem_intoPendingAlarmStatus(String input){
        input = "PENDING_ALARM";



        when(ArmingStatus.valueOf(anyString())).thenReturn(ArmingStatus.ARMED_AWAY);
        when(AlarmStatus.valueOf(anyString())).thenReturn(AlarmStatus.valueOf(input));



    }

}
