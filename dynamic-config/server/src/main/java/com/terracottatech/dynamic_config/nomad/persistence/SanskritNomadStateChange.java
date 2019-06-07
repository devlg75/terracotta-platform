/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.dynamic_config.nomad.persistence;

import com.terracottatech.nomad.server.ChangeRequest;
import com.terracottatech.nomad.server.ChangeRequestState;
import com.terracottatech.nomad.server.NomadServerMode;
import com.terracottatech.nomad.server.state.NomadStateChange;
import com.terracottatech.persistence.sanskrit.HashUtils;
import com.terracottatech.persistence.sanskrit.MutableSanskritObject;
import com.terracottatech.persistence.sanskrit.Sanskrit;
import com.terracottatech.persistence.sanskrit.SanskritException;
import com.terracottatech.persistence.sanskrit.SanskritObject;
import com.terracottatech.persistence.sanskrit.change.SanskritChange;
import com.terracottatech.persistence.sanskrit.change.SanskritChangeBuilder;

import java.util.UUID;

import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.CHANGE_CREATION_HOST;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.CHANGE_CREATION_USER;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.CHANGE_OPERATION;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.CHANGE_RESULT_HASH;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.CHANGE_STATE;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.CHANGE_VERSION;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.CURRENT_VERSION;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.HIGHEST_VERSION;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.LAST_MUTATION_HOST;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.LAST_MUTATION_USER;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.LATEST_CHANGE_UUID;
import static com.terracottatech.dynamic_config.nomad.persistence.NomadSanskritKeys.MODE;

public class SanskritNomadStateChange implements NomadStateChange {
  private final Sanskrit sanskrit;
  private final SanskritChangeBuilder changeBuilder;
  private volatile Long changeVersion;
  private volatile String changeResult;

  public SanskritNomadStateChange(Sanskrit sanskrit, SanskritChangeBuilder changeBuilder) {
    this.sanskrit = sanskrit;
    this.changeBuilder = changeBuilder;
  }

  @Override
  public NomadStateChange setInitialized() {
    setMode(NomadServerMode.ACCEPTING);
    return this;
  }

  @Override
  public NomadStateChange setMode(NomadServerMode mode) {
    changeBuilder.setString(MODE, mode.name());
    return this;
  }

  @Override
  public NomadStateChange setLatestChangeUuid(UUID changeUuid) {
    changeBuilder.setString(LATEST_CHANGE_UUID, changeUuid.toString());
    return this;
  }

  @Override
  public NomadStateChange setCurrentVersion(long versionNumber) {
    changeBuilder.setLong(CURRENT_VERSION, versionNumber);
    return this;
  }

  @Override
  public NomadStateChange setHighestVersion(long versionNumber) {
    changeBuilder.setLong(HIGHEST_VERSION, versionNumber);
    return this;
  }

  @Override
  public NomadStateChange setLastMutationHost(String lastMutationHost) {
    changeBuilder.setString(LAST_MUTATION_HOST, lastMutationHost);
    return this;
  }

  @Override
  public NomadStateChange setLastMutationUser(String lastMutationUser) {
    changeBuilder.setString(LAST_MUTATION_USER, lastMutationUser);
    return this;
  }

  @Override
  public NomadStateChange createChange(UUID changeUuid, ChangeRequest changeRequest) {
    changeVersion = changeRequest.getVersion();
    changeResult = changeRequest.getChangeResult();
    String resultHash = HashUtils.generateHash(changeResult);

    MutableSanskritObject child = sanskrit.newMutableSanskritObject();
    child.setString(CHANGE_STATE, changeRequest.getState().name());
    child.setLong(CHANGE_VERSION, changeRequest.getVersion());
    child.setExternal(CHANGE_OPERATION, changeRequest.getChange());
    child.setString(CHANGE_RESULT_HASH, resultHash);
    child.setString(CHANGE_CREATION_HOST, changeRequest.getCreationHost());
    child.setString(CHANGE_CREATION_USER, changeRequest.getCreationUser());

    changeBuilder.setObject(changeUuid.toString(), child);

    return this;
  }

  @Override
  public NomadStateChange updateChangeRequestState(UUID changeUuid, ChangeRequestState newState) {
    String uuidString = changeUuid.toString();
    SanskritObject existing = getObject(uuidString);
    MutableSanskritObject updated = sanskrit.newMutableSanskritObject();
    existing.accept(updated);

    updated.setString(CHANGE_STATE, newState.name());
    changeBuilder.setObject(uuidString, updated);
    return this;
  }

  public SanskritChange getSanskritChange() {
    return changeBuilder.build();
  }

  public Long getChangeVersion() {
    return changeVersion;
  }

  public String getChangeResult() {
    return changeResult;
  }

  private SanskritObject getObject(String key) {
    try {
      return sanskrit.getObject(key);
    } catch (SanskritException e) {
      throw new RuntimeException(e);
    }
  }
}
