package com.udacity.SecurityService.data;

import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;


public class NewSensorProvider implements InstanceCreator<Sensor> {
    private String name;
    private SensorType sensorType;
    public NewSensorProvider(String name, SensorType sensorType){
        this.name = name;
        this.sensorType = sensorType;
    }

    @Override
    public Sensor createInstance(Type type) {
        Sensor sensor = new Sensor(name, sensorType);
        return sensor;
    }
}
