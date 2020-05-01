package com.cuongdd150481.hust.esp_luongtronghai;

public class DataReceive {
    Float Humi;
    Float Temp;
    public  DataReceive(){

    }

    public DataReceive(Float hum, Float tem){
        Humi = hum;
        Temp = tem;
    }

    public Float getTemperature() {
        return Temp;
    }

    public Float getHumidity() {
        return Humi;
    }
}
