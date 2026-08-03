package com.portfolio.notification.infrastructure.delivery;

import com.portfolio.notification.domain.model.NotificationChannelType;
import com.portfolio.notification.domain.port.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationChannelResolver {
    private final Map<NotificationChannelType, NotificationChannel> channels;

    public NotificationChannelResolver(List<NotificationChannel> channels) {
        this.channels = new EnumMap<>(NotificationChannelType.class);
        for (NotificationChannel channel : channels) {
            this.channels.put(channel.supports(), channel);

        }
    }

    public NotificationChannel resolve(NotificationChannelType channelType) {
        NotificationChannel channel =channels.get(channelType);

        if(channel == null){
            throw new IllegalArgumentException("Unsupported notification channel: " + channelType);
        }

        return channel;
    }
}
