package com.github.karsaig;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ServiceLoaders {
    public static <T> T loadService(Class<T> type) {
        // FIXME: create a more sophisticated loading.
        Iterator<T> serviceProviders = ServiceLoader.load(type).iterator();

        T serviceProvider;

        if (serviceProviders.hasNext()) {
            serviceProvider = serviceProviders.next();
        } else {
            throw new IllegalStateException("Could not find Service Provider for " + type.getCanonicalName());
        }

        if (serviceProviders.hasNext()) {
            throw new IllegalStateException("There is more than on Service Provider for " + type.getCanonicalName());
        }

        return serviceProvider;
    }
}
