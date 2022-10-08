package com.udacity.SecurityService;


import com.udacity.ImageService.application.ImageService;
import com.udacity.ImageService.service.FakeImageService;
import com.udacity.SecurityService.data.PretendDatabaseSecurityRepositoryImpl;
import com.udacity.SecurityService.data.SecurityRepository;
import com.udacity.SecurityService.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;
    @Mock
    private SecurityRepository securityRepository = new PretendDatabaseSecurityRepositoryImpl();

    @Mock
    private FakeImageService imageService;
    @BeforeEach
    public void init(){
        securityService = new SecurityService(securityRepository,imageService);
    }

    @ParameterizedTest
    public void alarm(){}

}
