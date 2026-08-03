package com.portfolio.notification.domain.model;

public record DeliveryResult (
        boolean successful,
        String reason
){
}
