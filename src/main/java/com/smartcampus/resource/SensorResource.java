/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.Room;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.storage.CampusDataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
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
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
        if (sensor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid request body", 400)).build();
        }

        Room parentRoom = dataStore.rooms.get(sensor.getRoomId());
        if (parentRoom == null) {
            throw new LinkedResourceNotFoundException("Room ID " + sensor.getRoomId() + " not found.");
        }

        //Check if sensor is moving from another room
        Sensor existingSensor = dataStore.sensors.get(sensor.getId());
        if (existingSensor != null && !existingSensor.getRoomId().equals(sensor.getRoomId())) {
            Room oldRoom = dataStore.rooms.get(existingSensor.getRoomId());
            if (oldRoom != null) {
                oldRoom.getSensorIds().remove(sensor.getId());
            }
        }

        dataStore.sensors.put(sensor.getId(), sensor);

        if (!parentRoom.getSensorIds().contains(sensor.getId())) {
            parentRoom.getSensorIds().add(sensor.getId());
        }

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // Task 4.1: Sub-Resource Locator Pattern
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sId) {
        return new SensorReadingResource(sId);
    }
}
