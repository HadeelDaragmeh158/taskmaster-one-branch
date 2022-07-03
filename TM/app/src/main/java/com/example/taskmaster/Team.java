package com.example.taskmaster;

import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

public class Team {
    @PrimaryKey(autoGenerate = true)
    private int id ;

    String name ;
    List<Task> tasksList = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
