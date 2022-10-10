package com.example.TcpServer.scooter;

import lombok.Getter;

@Getter
public class ScooterDao {
    private String stat;
    private String identity;
    private int soc;
    private double volt;
    private double current;
    private int temp;
    private double lat;
    private double lon;
    private String pow;
    private int shock;

    public ScooterDao(String scooterData) {
        stat = scooterData.substring(0, 2);
        identity = scooterData.substring(2, 6);
        soc = Integer.parseInt(scooterData.substring(6, 8));
        volt = stringToDouble(scooterData.substring(8, 12),3);
        current = stringToDouble(scooterData.substring(12,16),3);
        temp = Integer.parseInt(scooterData.substring(16, 18));
        lat = stringToDouble(scooterData.substring(18, 26), 4);
        lon = stringToDouble(scooterData.substring(26, 34), 4);
        pow = scooterData.substring(34, 36);
        shock = Integer.parseInt(scooterData.substring(36, 38));
    }

    private double stringToDouble(String str, int index) {
        StringBuffer sb = new StringBuffer();
        sb.append(str);
        sb.insert(index-1,".");
        return Double.valueOf(sb.toString());
    }
}
