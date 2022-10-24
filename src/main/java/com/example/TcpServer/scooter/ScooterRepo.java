package com.example.TcpServer.scooter;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;

@Repository
public class ScooterRepo {

    @Value("${driver-class-name}")
    private String driver;
    @Value("${url}")
    private String url;
    @Value("${username}")
    private String user;
    @Value("${password}")
    private String pw;

    public boolean saveScooter(ScooterDao scooterDao) {
        Connection con = null;                                     // 데이터 베이스와 연결을 위한 객체
        PreparedStatement pstmt = null;
        boolean start = false;

        String saveDriveScooterState = "insert into scooter_state(lat, lng, pow, shock,soc,stat,temp," +
                "time,volt,current,speed,scooter_id, altitude,drive_log_id) " +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String saveNoneDriveScooterState = "insert into scooter_state(lat, lng, pow, shock, soc, stat," +
                " temp, time, volt,current,speed,scooter_id, altitude) " +
                "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            ScooterInfo scooter = findScooter(scooterDao.getIdentity());
            if (scooter.getId() != null) {
                Integer id = scooter.getId();
                Integer activate_id = scooter.getActivate_id();

                // 1. JDBC 드라이버 로딩 - MySQL JDBC 드라이버의 Driver Class 로딩
                Class.forName(driver);

                // 2. Connection 생성 - .getConnection(연결문자열, DB-ID, DB-PW)
                con = DriverManager.getConnection(url, user, pw);

                // 3. PreParedStatement 객체 생성, 객체 생성시 SQL 문장 저장
                if (activate_id != null && scooterDao.getPow().equals("01")) {
                    start = true;
                    pstmt = con.prepareStatement(saveDriveScooterState);
                    pstmt.setInt(14, activate_id);
                } else {
                    start = false;
                    pstmt = con.prepareStatement(saveNoneDriveScooterState);
                }
                pstmt.setDouble(1, scooterDao.getLat());
                pstmt.setDouble(2, scooterDao.getLng());
                pstmt.setString(3, scooterDao.getPow());
                pstmt.setInt(4, scooterDao.getShock());
                pstmt.setInt(5, scooterDao.getSoc());
                pstmt.setString(6, scooterDao.getStat());
                pstmt.setInt(7, scooterDao.getTemp());
                pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setDouble(9, scooterDao.getVolt());
                pstmt.setString(10, scooterDao.getCurrent());
                pstmt.setInt(11, scooterDao.getSpeed());
                pstmt.setInt(12, id);
                pstmt.setInt(13, scooterDao.getAltitude());


                // 5. SQL 문장을 실행하고 결과를 리턴 - SQL 문장 실행 후, 변경된 row 수 int type 리턴
                pstmt.executeUpdate();
                return start;
            }

        } catch (SQLException e) {
            System.out.println("[SQL Error : " + e.getMessage() + "]");
        } catch (ClassNotFoundException e1) {
            System.out.println("[JDBC Connector Driver 오류 : " + e1.getMessage() + "]");
        } finally {
            //사용순서와 반대로 close 함
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return start;
    }

    public ScooterInfo findScooter(String identity) {

        Connection con = null;                                     // 데이터 베이스와 연결을 위한 객체
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Integer id = null;
        Integer activate_id = null;
        String SQL = "select * from scooter where identity=" + identity;

        try {
            // 1. JDBC 드라이버 로딩 - MySQL JDBC 드라이버의 Driver Class 로딩
            Class.forName(driver);

            // 2. Connection 생성 - .getConnection(연결문자열, DB-ID, DB-PW)
            con = DriverManager.getConnection(url, user, pw);

            // 3. PreParedStatement 객체 생성, 객체 생성시 SQL 문장 저장
            pstmt = con.prepareStatement(SQL);

            // 5. SQL 문장을 실행하고 결과를 리턴 - SQL 문장 실행 후, 변경된 row 수 int type 리턴
            rs = pstmt.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id");
                activate_id = rs.getInt("activate_id");
            }

        } catch (SQLException e) {
            System.out.println("[SQL Error : " + e.getMessage() + "]");
        } catch (ClassNotFoundException e1) {
            System.out.println("[JDBC Connector Driver 오류 : " + e1.getMessage() + "]");
        } finally {
            //사용순서와 반대로 close 함
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ScooterInfo(id,activate_id);
    }

    @Getter
    private class ScooterInfo {
        private Integer id;
        private Integer activate_id;

        public ScooterInfo(Integer id, Integer activate_id) {
            this.id = id;
            this.activate_id = activate_id;
        }
    }

}
