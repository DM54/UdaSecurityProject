package com.udacity.SecurityService.data;

import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;

public class NewSensorProvider implements InstanceCreator<Sensor> {
    private String name;
    private SensorType sensorType;
    public NewSensorProvider(String name, SensorType type){
        this.name = name;
        this.sensorType = type;
    }

    @Override
    public Sensor createInstance(Type type) {
        Sensor sensor = new Sensor(name, sensorType);
        return sensor;
    }
}
