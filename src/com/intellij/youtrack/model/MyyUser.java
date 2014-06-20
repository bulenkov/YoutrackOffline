package com.intellij.youtrack.model;

/**
 * @author Konstantin Bulenkov
 */
public class MyyUser {
  private final String id;
  private String name;
  private String email;

  public MyyUser(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isLoaded() {
    return name != null;
  }
}
