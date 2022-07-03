package com.example.TcpServer.domain;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class Battery {
    @Id @GeneratedValue
    private Long id;
    private String identity;
    private int soc;
    private double volt;
    private double current;
    private int temp;
    private double lat;
    private double lon;
    private String pow;
    private int shock;
    private LocalDateTime time;

    @Builder
    public Battery(String identity, int soc, double volt, double current, int temp, double lat, double lon, String pow, int shock) {
        this.identity = identity;
        this.soc = soc;
        this.volt = volt;
        this.current = current;
        this.temp = temp;
        this.lat = lat;
        this.lon = lon;
        this.pow = pow;
        this.shock = shock;
        this.time = LocalDateTime.now();
    }
}
