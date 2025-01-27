package com.killiancorbel.realtimeapi.models.requests.revenuecat;

public class SubscriberAttributes {
    private RevenueCatUid uid;
    private RevenueCatEmail email;

    public RevenueCatUid getUid() {
        return uid;
    }

    public void setUid(RevenueCatUid uid) {
        this.uid = uid;
    }

    public RevenueCatEmail getEmail() {
        return email;
    }

    public void setEmail(RevenueCatEmail email) {
        this.email = email;
    }
}
