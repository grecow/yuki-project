package com.killiancorbel.realtimeapi.models.requests.revenuecat;

public class RevenueCatEvent {
    private String type;
    private String original_app_user_id;
    private SubscriberAttributes subscriber_attributes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SubscriberAttributes getSubscriber_attributes() {
        return subscriber_attributes;
    }

    public void setSubscriber_attributes(SubscriberAttributes subscriber_attributes) {
        this.subscriber_attributes = subscriber_attributes;
    }

    public String getOriginal_app_user_id() {
        return original_app_user_id;
    }

    public void setOriginal_app_user_id(String original_app_user_id) {
        this.original_app_user_id = original_app_user_id;
    }
}