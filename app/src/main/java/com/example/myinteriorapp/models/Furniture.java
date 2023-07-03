package com.example.myinteriorapp.models;

public class Furniture {
    private String name;
    private String description;
    // 추가적인 가구 속성들...

    public Furniture(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // 추가적인 getter 및 setter 메서드들...

    @Override
    public String toString() {
        return "Furniture{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                // 추가적인 가구 속성들...
                '}';
    }
}
