/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.storage;

import com.smartcampus.model.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author ubddp
 */
public class CampusDataStore {

    private static CampusDataStore instance;

    public Map<String, Room> rooms = new ConcurrentHashMap<>();
    public Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private CampusDataStore() {
    }

    public static synchronized CampusDataStore getInstance() {
        if (instance == null) {
            instance = new CampusDataStore();
        }
        return instance;
    }

}
