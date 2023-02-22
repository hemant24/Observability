package com.hemant.prometheus;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class MetricService {

    private static final List<String> availablePaymentMethods = Arrays.asList("CASH", "CREDITCARD", "PAYPAL");
    private static final List<String> availableShippingMethods = Arrays.asList("STANDARD", "EXPRESS");
    private static final List<String> memory = Arrays.asList("M0", "M1");
    private static final List<String> methods = Arrays.asList("GET", "POST", "PUT", "DELETE");
    private static final List<String> uris = Arrays.asList("/v1/orders", "/v1/restaurants", "/v2/rating", "/v1/points");

    static final Counter ordersCreatedCounter = Counter.build()
            .name("orders_created")
            .labelNames("payment_method", "shipping_method")
            .help("Number of orders created").register();

    /*
    static final Gauge sizeOfFileProcessed = Gauge.build()
            .name("file_size_processed_mb").help("Size of file processed in mb").register();
    */
    static final Gauge availableHeapMemory = Gauge.build()
            .name("avail_heap_memory_mb")
            .labelNames("memory_name")
            .help("Heap Memory available in MB").register();

    static final Histogram requestLatency = Histogram.build()
            .labelNames("method", "uri")
            .name("requests_latency_seconds").help("Request latency in seconds.").register();


    @Scheduled(fixedDelay = 5000)
    public void increaseCounter() {
        ordersCreatedCounter.labels(randomPaymentMethod(), randomShippingMethod()).inc();
        availableHeapMemory.labels(randomMemoryName())
                .set((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /(1024*1024));
    }

    @Scheduled(fixedDelay = 5000)
    public void registerLatency() throws InterruptedException {
        int methodRandomIndex = new Random().nextInt(methods.size());
        int uriRandomIndex = new Random().nextInt(uris.size());
        Histogram.Timer requestTimer = requestLatency.
                labels(methods.get(methodRandomIndex), uris.get(uriRandomIndex)).startTimer();
        int randomSleep = new Random().nextInt(4 * 1000); //4 sec
        Thread.sleep(randomSleep);
        requestTimer.observeDuration();
    }

    private String randomPaymentMethod() {
        int randomIndex = new Random().nextInt(availablePaymentMethods.size());
        return availablePaymentMethods.get(randomIndex);
    }

    private String randomShippingMethod() {
        int randomIndex = new Random().nextInt(availableShippingMethods.size());
        return availableShippingMethods.get(randomIndex);
    }

    private String randomMemoryName() {
        int randomIndex = new Random().nextInt(memory.size());
        return memory.get(randomIndex);
    }
}
