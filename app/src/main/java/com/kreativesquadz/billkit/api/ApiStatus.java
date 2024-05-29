package com.kreativesquadz.billkit.api;

import com.google.gson.annotations.SerializedName;

public class ApiStatus {

    @SerializedName("invoiceId")
    public final Integer invoiceId;
    @SerializedName("message")
    public final String message;

    public ApiStatus(Integer invoiceId, String message) {
        this.invoiceId = invoiceId;
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