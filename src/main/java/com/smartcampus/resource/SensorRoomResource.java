/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.storage.CampusDataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ubddp
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class SensorRoomResource {

    private CampusDataStore dataStore = CampusDataStore.getInstance();

    // Task 2.1: GET / - Lst all rooms
    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(dataStore.rooms.values());
    }

    // Task 2.1: POST / - Create a new room
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Room ID is required").build();
        }
        dataStore.rooms.put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // Task 2.1: GET /{roomId} - Fetch specific room metadata
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(room).build();
    }

    // Task 2.2: DELETE /{roomId} - Room decommissioning
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Business Logic: Prevent deletion if room has active sensors
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }

        dataStore.rooms.remove(roomId);
        return Response.noContent().build(); // 204 No Content 
    }
}
