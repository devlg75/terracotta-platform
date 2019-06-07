/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.dynamic_config.nomad.persistence;

public class InitialConfigStorage implements ConfigStorage {
  private static final long INITIAL_VERSION = 0L;

  private final ConfigStorage underlying;

  public InitialConfigStorage(ConfigStorage underlying) {
    this.underlying = underlying;
  }

  @Override
  public String getConfig(long version) throws ConfigStorageException {
    if (version == INITIAL_VERSION) {
      return null;
    }

    return underlying.getConfig(version);
  }

  @Override
  public void saveConfig(long version, String config) throws ConfigStorageException {
    if (version == INITIAL_VERSION) {
      throw new AssertionError("Invalid version: " + version);
    }

    underlying.saveConfig(version, config);
  }
}
