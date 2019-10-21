/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.dynamic_config.service.handler;

import com.terracottatech.config.data_roots.DataDirectoriesConfig;
import com.terracottatech.dynamic_config.handler.ConfigChangeHandler;
import com.terracottatech.dynamic_config.handler.InvalidConfigChangeException;
import com.terracottatech.dynamic_config.model.Cluster;
import com.terracottatech.dynamic_config.model.Configuration;
import com.terracottatech.dynamic_config.model.NodeContext;
import com.terracottatech.dynamic_config.util.IParameterSubstitutor;

/**
 * Handles data directory additions
 */
public class DataRootConfigChangeHandler implements ConfigChangeHandler {

  private final DataDirectoriesConfig dataDirectoriesConfig;
  private final IParameterSubstitutor parameterSubstitutor;

  public DataRootConfigChangeHandler(DataDirectoriesConfig dataDirectoriesConfig, IParameterSubstitutor parameterSubstitutor) {
    this.dataDirectoriesConfig = dataDirectoriesConfig;
    this.parameterSubstitutor = parameterSubstitutor;
  }

  @Override
  public Cluster tryApply(NodeContext baseConfig, Configuration change) throws InvalidConfigChangeException {
    if (change.getValue() == null) {
      throw new InvalidConfigChangeException("Unsupported operation: " + change);
    }

    // TODO [DYNAMIC-CONFIG]: TDB-4655: handle data-dirs update correctly and finish this code:
    // - validate against existing licence (use DiagnosticServices.getService(DynamicConfig.class).get().validate...())
    //
    // - validate every combinations of operation (GET/UNSET) ok ket/value VS existing state
    // - operation == SET && key does not exist => OK => addition.
    // - operation == SET && key exists => check directory if this is a move and we can move
    // - operation == UNSET && ket does not exist => do nothing
    // - operation == UNSET && ket exists => ??? not sure we can remove, but if yes, at least check if directory is used ?
    //
    // Then apply using:
    //
    // Operation operation = configChange.getOperation();
    // Cluster updatedCluster = baseConfig.getCluster().clone();
    // configChange.toConfiguration().apply(operation, updatedCluster, parameterSubstitutor);
    // return updatedCluster;
    //
    // THIS IS VERY IMPORTANT TO CREATE A COPY OF THE CLUSTER TO NOT UPDATE THE INCOMING PARAMETER

    try {
      String dataDirectoryName = change.getKey();
      String dataDirectoryPath = change.getValue();

      validateChange(dataDirectoryName, dataDirectoryPath);

      Cluster updatedCluster = baseConfig.getCluster();
      change.apply(updatedCluster, parameterSubstitutor);
      return updatedCluster;
    } catch (Exception e) {
      throw new InvalidConfigChangeException(e.getMessage(), e);
    }
  }

  @Override
  public boolean apply(Configuration change) {
    String dataDirectoryName = change.getKey();
    String dataDirectoryPath = change.getValue();
    dataDirectoriesConfig.addDataDirectory(dataDirectoryName, dataDirectoryPath);

    // in some case, perhaps we need a restart ?
    // return false;
    return true;
  }

  private void validateChange(String name, String path) throws Exception {
    dataDirectoriesConfig.validateDataDirectory(name, path);
  }
}
