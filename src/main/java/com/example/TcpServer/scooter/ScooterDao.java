package com.example.TcpServer.scooter;

import lombok.Getter;

@Getter
public class ScooterDao {
    private String stat;
    private String identity;
    private int soc;
    private double volt;
    private String current;
    private int temp;
    private int speed;
    private double lat;
    private double lng;
    private int altitude;
    private String pow;
    private int shock;

    public ScooterDao(String scooterData) {
        stat = scooterData.substring(0, 2);
        identity = scooterData.substring(2, 6);
        soc = Integer.parseInt(scooterData.substring(6, 8));
        volt = stringToDouble(scooterData.substring(8, 12),3);
        current = scooterData.substring(12,20);
        temp = Integer.parseInt(scooterData.substring(20, 24));
        speed = Integer.parseInt(scooterData.substring(24, 28));
        lat = stringToDouble(scooterData.substring(28, 36), 4);
        lng = stringToDouble(scooterData.substring(36, 44), 4);
        altitude = Integer.parseInt(scooterData.substring(44, 48));
        pow = scooterData.substring(48, 50);
        shock = Integer.parseInt(scooterData.substring(50, 52));
    }

    private double stringToDouble(String str, int index) {
        StringBuffer sb = new StringBuffer();
        sb.append(str);
        sb.insert(index-1,".");
        return Double.valueOf(sb.toString());
    }
}