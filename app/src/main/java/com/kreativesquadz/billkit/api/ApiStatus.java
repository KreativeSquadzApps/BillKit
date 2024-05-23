package com.kreativesquadz.billkit.api;

import com.google.gson.annotations.SerializedName;

public class ApiStatus {


    @SerializedName("message")
    public final String message;

    public ApiStatus(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ApiStatus{" +
                "status='" + "status" + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}