package com.kreativesquadz.billkit.api;

public class MyTokenStorage {
    public static String token;

    public static void setToken(String token) {
        MyTokenStorage.token = token;
    }

    public static String getToken() {
        return token;
    }
}
