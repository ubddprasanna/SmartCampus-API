/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.Room;
import com.smartcampus.storage.CampusDataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author ubddp
 */
public class SensorResource {

    private CampusDataStore dataStore = CampusDataStore.getInstance();

    // Task 3.1 & 3.2: GET / - Support optional type filtering 
    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = new ArrayList<>(dataStore.sensors.values());
        if (type == null || type.isEmpty()) {
            return allSensors;
        }
        // Filter logic for Part 3.2 
        return allSensors.stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // Task 3.1: POST / - Registr sensor with integrity check 
    @POST
    public Response registerSensor(Sensor sensor) {
        Room parentRoom = dataStore.rooms.get(sensor.getRoomId());
        if (parentRoom == null) {
            return Response.status(422)
                    .entity("Room ID " + sensor.getRoomId() + " not found.")
                    .build();
        }

        dataStore.sensors.put(sensor.getId(), sensor);

        // Link the sensor ID to the room's list
        parentRoom.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }
}
