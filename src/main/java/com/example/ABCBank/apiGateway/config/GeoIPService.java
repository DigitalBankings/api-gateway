package com.example.ABCBank.apiGateway.config;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;

import java.io.File;
import java.net.InetAddress;

public class GeoIPService {

    private static DatabaseReader reader;

    static {
        try {
            File database = new File("/path/to/GeoLite2-Country.mmdb");
            reader = new DatabaseReader.Builder(database).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String lookupCountry(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CountryResponse response = reader.country(ipAddress);
            return response.getCountry().getIsoCode(); // e.g., "KH", "VN", "LA"
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
