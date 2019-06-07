/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package com.terracottatech.dynamic_config.nomad;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Nomad change that supports any dynamic config change (see Cluster-tool.adoc)
 *
 * @author Mathieu Carbou
 */
public class SettingNomadChange extends FilteredNomadChange {

  public enum Cmd {SET, UNSET}

  private final Cmd cmd;
  private final String name;
  private final String value;

  @JsonCreator
  private SettingNomadChange(@JsonProperty("applicability") Applicability applicability,
                             @JsonProperty("cmd") Cmd cmd,
                             @JsonProperty("name") String name,
                             @JsonProperty("value") String value) {
    super(applicability);
    this.cmd = requireNonNull(cmd);
    this.name = requireNonNull(name);
    this.value = value;
  }

  @Override
  public String getSummary() {
    return cmd == Cmd.SET ?
        ("set " + name + "=" + value) :
        ("unset " + name);
  }

  //TODO [DYNAMIC-CONFIG]: The setting name contains the value of the key defined in the APi doc. Example: offheap-resources.foo
  //The name will be validated on client-side by the CLI so when it arrives on server we can parse it to determine where the change applies.
  //=> See whether we need an enum besides that or or if we can reuse some parsing logic somewhere to be able to determine the type of change (which handler to forward this to)
  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public Cmd getCmd() {
    return cmd;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SettingNomadChange)) return false;
    if (!super.equals(o)) return false;
    SettingNomadChange that = (SettingNomadChange) o;
    return getCmd() == that.getCmd() &&
        getName().equals(that.getName()) &&
        Objects.equals(getValue(), that.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getCmd(), getName(), getValue());
  }

  public static SettingNomadChange set(Applicability applicability, String name, String value) {
    return new SettingNomadChange(applicability, Cmd.SET, name, value);
  }

  public static SettingNomadChange unset(Applicability applicability, String name) {
    return new SettingNomadChange(applicability, Cmd.UNSET, name, null);
  }

}
