/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.model.*;
import com.smartcampus.storage.CampusDataStore;
import com.smartcampus.exception.SensorUnavailableException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 *
 * @author ubddp
 */
public class SensorReadingResource {

    private String sensorId;
    private CampusDataStore dataStore = CampusDataStore.getInstance();

    // Constructor called by the Locator in SensorResource
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // Task 4.2: GET / - Fetch reading histry for this specific sensor
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorReading> getHistory() {
        return dataStore.readings.getOrDefault(sensorId, new ArrayList<>());
    }

    // Task 4.2: POST / - Add reading & update the parent sensor
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Sensor not found").build();
        }

        // Integrity check: Forbidden if sensor is in maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        // 1. Append reading to histry
        dataStore.readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        // 2. Side Efect: Update parent Sensor"s current value
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
