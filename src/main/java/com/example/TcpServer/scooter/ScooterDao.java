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
    private String pow;
    private int shock;

    public ScooterDao(String scooterData) {
        stat = scooterData.substring(0, 2);
        identity = scooterData.substring(2, 6);
        soc = Integer.parseInt(scooterData.substring(6, 8));
        volt = stringToDouble(scooterData.substring(8, 12),3);
        current = scooterData.substring(12,20);
        temp = Integer.parseInt(scooterData.substring(20, 22));
        speed = Integer.parseInt(scooterData.substring(22, 26));
        lat = stringToDouble(scooterData.substring(26, 34), 4);
        lng = stringToDouble(scooterData.substring(34, 42), 4);
        pow = scooterData.substring(42, 44);
        shock = Integer.parseInt(scooterData.substring(44, 46));
    }

    private double stringToDouble(String str, int index) {
        StringBuffer sb = new StringBuffer();
        sb.append(str);
        sb.insert(index-1,".");
        return Double.valueOf(sb.toString());
    }
}