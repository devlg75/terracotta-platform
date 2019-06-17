/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.dynamic_config.cli.nomad;

import com.terracottatech.diagnostic.client.DiagnosticService;
import com.terracottatech.diagnostic.client.connection.ConcurrencySizing;
import com.terracottatech.diagnostic.client.connection.MultiDiagnosticServiceConnection;
import com.terracottatech.diagnostic.client.connection.MultiDiagnosticServiceConnectionFactory;
import com.terracottatech.dynamic_config.nomad.NomadEnvironment;
import com.terracottatech.nomad.client.NamedNomadServer;
import com.terracottatech.nomad.client.NomadClient;
import com.terracottatech.nomad.server.NomadServer;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class NomadClientFactory {

  private final MultiDiagnosticServiceConnectionFactory connectionFactory;
  private final NomadEnvironment environment;
  private final long requestTimeoutMillis;
  private final ConcurrencySizing concurrencySizing;

  public NomadClientFactory(MultiDiagnosticServiceConnectionFactory connectionFactory, ConcurrencySizing concurrencySizing,
                            NomadEnvironment environment, long requestTimeoutMillis) {
    this.connectionFactory = connectionFactory;
    this.environment = environment;
    this.requestTimeoutMillis = requestTimeoutMillis;
    this.concurrencySizing = concurrencySizing;
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public CloseableNomadClient createClient(Collection<InetSocketAddress> hostPortList) {
    String host = environment.getHost();
    String user = environment.getUser();

    MultiDiagnosticServiceConnection connection = connectionFactory.createConnection(hostPortList);

    Set<NamedNomadServer> servers = connection.getEndpoints().stream()
        .map(endpoint -> createNamedNomadServer(endpoint, connection.getDiagnosticService(endpoint).get()))
        .collect(Collectors.toSet());

    NomadClient client = new NomadClient(servers, host, user);
    int concurrency = concurrencySizing.getThreadCount(servers.size());
    client.setConcurrency(concurrency);
    client.setTimeoutMillis(requestTimeoutMillis);

    return new CloseableNomadClient(client, connection);
  }

  private NamedNomadServer createNamedNomadServer(InetSocketAddress address, DiagnosticService diagnosticService) {
    NomadServer nomadServerProxy = diagnosticService.getProxy(NomadServer.class);
    // use the <ip>:<port> for the name for nomad because server name as it was before might not be unique across the cluster.
    return new NamedNomadServer(address.toString(), nomadServerProxy);
  }
}
