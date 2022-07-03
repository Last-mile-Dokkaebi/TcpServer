package com.example.TcpServer.domain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatteryDao {
    private String identity;
    private int soc;
    private double volt;
    private double current;
    private int temp;
    private double lat;
    private double lon;
    private String pow;
    private int shock;

    public BatteryDao(String batteryData) {
        /**
         * Integer.getInteger에 대한 잘못된 사용으로 계속 null이 반환되었다.
         * spring의 자동 주입과는 상관이 없었다.
         */
        identity = batteryData.substring(2, 6);
        if (batteryData != null) {
            soc = Integer.parseInt(batteryData.substring(6, 8));
            volt = stringToDouble(batteryData.substring(8, 12),3);
            current = stringToDouble(batteryData.substring(12,16),3);
            temp = Integer.parseInt(batteryData.substring(16, 18));
            lat = stringToDouble(batteryData.substring(18, 26), 4);
            lon = stringToDouble(batteryData.substring(26, 34), 4);
            pow = batteryData.substring(34, 36);
            shock = Integer.parseInt(batteryData.substring(36, 38));
        }

    }

    public Battery toEntity() {
        return Battery.builder()
                    .identity(identity)
                    .soc(soc)
                    .volt(volt)
                    .current(current)
                    .temp(temp)
                    .lat(lat)
                    .lon(lon)
                    .pow(pow)
                    .shock(shock)
                    .build();
    }

    private double stringToDouble(String str, int index) {
        StringBuffer sb = new StringBuffer();
        sb.append(str);
        sb.insert(index-1,".");
        return Double.valueOf(sb.toString());
    }
}
