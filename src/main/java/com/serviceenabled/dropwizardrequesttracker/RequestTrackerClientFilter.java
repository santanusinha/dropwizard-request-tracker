package com.serviceenabled.dropwizardrequesttracker;

import org.slf4j.MDC;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;


public class RequestTrackerClientFilter extends ClientFilter {
    private final RequestTrackerConfiguration configuration;
    private final Supplier<String> idSupplier;

    public RequestTrackerClientFilter(RequestTrackerConfiguration configuration) {
        this(configuration, new UuidSupplier());
    }

    public RequestTrackerClientFilter(RequestTrackerConfiguration configuration, Supplier<String> idSupplier) {
        this.configuration = configuration;
        this.idSupplier = idSupplier;
    }

    @Override
    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {
        // As getNext() is final in ClientFilter and cannot be stubbed in test classes, all work is
        // performed in the protected doWork method which can be unit tested.
        // That the filter hands off to the next in the chain can only be tested in an integration test
        return getNext().handle(doWork(clientRequest));
    }

    protected ClientRequest doWork(ClientRequest clientRequest) {
        Optional<String> requestId = Optional.fromNullable(MDC.get(configuration.getMdcKey()));

        clientRequest.getHeaders().add(configuration.getHeaderName(), requestId.or(idSupplier));

        return clientRequest;
    }
}