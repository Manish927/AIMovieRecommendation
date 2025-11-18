package com.spring5.movieservice.gateway;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Configuration
public class LoadBalancerConfig {

    /**
     * Configure Recommendation Service instance(s)
     * Currently single instance, but ready for horizontal scaling
     */
    @Bean
    @Primary
    public ServiceInstanceListSupplier recommendationServiceInstances() {
        return new ServiceInstanceListSupplier() {
            @Override
            public String getServiceId() {
                return "recommendation-service";
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                return Flux.just(Arrays.asList(
                    new DefaultServiceInstance(
                        "recommendation-service-1",
                        "recommendation-service",
                        "recommendation-service",  // Docker service name
                        8083,
                        false  // Not secure
                    )
                    // Add more instances here when scaling:
                    // new DefaultServiceInstance(
                    //     "recommendation-service-2",
                    //     "recommendation-service",
                    //     "recommendation-service-2",
                    //     8083,
                    //     false
                    // )
                ));
            }
        };
    }

    /**
     * Configure Ticket Booking Service instance(s)
     */
    @Bean
    public ServiceInstanceListSupplier ticketBookingServiceInstances() {
        return new ServiceInstanceListSupplier() {
            @Override
            public String getServiceId() {
                return "ticket-booking-service";
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                return Flux.just(Arrays.asList(
                    new DefaultServiceInstance(
                        "ticket-booking-service-1",
                        "ticket-booking-service",
                        "ticket-booking-service",  // Docker service name
                        8085,
                        false
                    )
                ));
            }
        };
    }
}

