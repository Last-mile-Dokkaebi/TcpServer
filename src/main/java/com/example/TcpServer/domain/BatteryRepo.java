package com.example.TcpServer.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class BatteryRepo {
    private final EntityManager em;

    public void save(Battery battery) {
        em.persist(battery);
    }
}
