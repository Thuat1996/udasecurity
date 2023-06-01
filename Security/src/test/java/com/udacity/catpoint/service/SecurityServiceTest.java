package com.udacity.catpoint.service;

import com.udacity.catpoint.data.*;
import com.udacity.catpoint.image.service.FakeImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import com.udacity.catpoint.image.interfaces.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private FakeImageService imageServiceTest;

    @Mock
    private SecurityRepository securityRepository;
    private SecurityService securityService;
    Sensor sensor_door = new Sensor("Door", SensorType.DOOR);
    BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
    @BeforeEach
	void init() {
		securityService = new SecurityService(securityRepository, imageServiceTest);
        securityRepository.addSensor(sensor_door);
	}

    //#1. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Tests Requirement #1")
    public void alarmArmed_and_sensorActivated_pendingAlarmStatus(ArmingStatus armingStatus) {

        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);        
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor_door, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    //#2. If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Tests Requirement #2")
    public void alarmArmed_and_sensorActivated_and_systemIsPendingAlarmStatus_setAlarmStatusToAlarm(ArmingStatus armingStatus) {

        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor_door, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //#3. If pending alarm and all sensors are inactive, return to no alarm state.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Tests Requirement #3")
   public void alarmPending_and_allSensorInactivate_setAlarmStatusToNoAlarm(ArmingStatus armingStatus) {

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        securityService.changeSensorActivationStatus(sensor_door, true);
        securityService.changeSensorActivationStatus(sensor_door, false);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
   }

    //#4. If alarm is active, change in sensor state should not affect the alarm state.
    @ParameterizedTest
    @DisplayName("Tests Requirement #4")
    @ValueSource(booleans = {true, false})
    public void alarmActive_and_changeSensorState_doesNotAffectAlarmState(boolean sensorStatus) {
 
         when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
         securityService.changeSensorActivationStatus(sensor_door, sensorStatus);
         verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //#5. If a sensor is activated while already active and the system is in pending state, change it to alarm state.
    @Test
    @DisplayName("Tests Requirement #5")
    public void sensorActivated_whileAlreadyActive_and_systemPending_changeToAlarmState() {
        
        sensor_door.setActive(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor_door, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //#6. If a sensor is deactivated while already inactive, make no changes to the alarm state.
    @ParameterizedTest
    @DisplayName("Tests Requirement #6")
    @EnumSource(AlarmStatus.class)
    public void sensorActivated_whileAlreadyActive_and_systemPending_changeToAlarmState(AlarmStatus alarmStatus) {
        
        sensor_door.setActive(false);
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        securityService.changeSensorActivationStatus(sensor_door, false);

        verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));
    }

    //#7. If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status
    @Test
    @DisplayName("Tests Requirement #7")
    public void identifiesAnImageACat_whileSystemArmedHome_putSystemIntoAlarm() {

        when(imageServiceTest.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(img);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
    //#8. If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
    @Test
    @DisplayName("Tests Requirement #8")
    public void identifiesAnImageACat_doesNotContainACat_changeStatusToNoAlarm_ifSensorsAreNotActive() {
        when(imageServiceTest.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(false);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(img);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //#9. If the system is disarmed, set the status to no alarm.
    @Test
    @DisplayName("Tests Requirement #9")
    public void systemDisarmed_setStatusNoAlarm() {
        securityRepository.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //#10. If the system is armed, reset all sensors to inactive.
    @Test
    @DisplayName("Tests Requirement #10")
    public void systemArmed_resetAllSensorsInactive() {
       // securityRepository.setArmingStatus(ArmingStatus.ARMED);
    }
}