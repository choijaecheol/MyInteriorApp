package com.example.myinteriorapp.activities;

import com.example.myinteriorapp.models.Furniture;

import java.util.ArrayList;
import java.util.List;

public class FurnitureList {
    private List<Furniture> furnitureList;

    public FurnitureList() {
        furnitureList = new ArrayList<>();
    }

    public void addFurniture(Furniture furniture) {
        furnitureList.add(furniture);
    }

    public List<Furniture> getFurnitureList() {
        return furnitureList;
    }
}

