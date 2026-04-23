/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exception;

/**
 *
 * @author ubddp
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String roomId) {
        super("Room " + roomId + " cannot be deleted because it still contains active sensors.");
    }
}
