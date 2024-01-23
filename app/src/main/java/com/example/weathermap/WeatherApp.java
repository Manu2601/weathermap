package com.example.weathermap;

import java.util.List;

public class WeatherApp {
    private Main main;
    private List<Weather> weather;
    private String name;


    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public List<Weather> getWeatherList() {
        return weather;
    }

    public void setWeatherList(List<Weather> weatherList) {
        this.weather = weatherList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}