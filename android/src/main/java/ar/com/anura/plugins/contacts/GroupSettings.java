package ar.com.anura.plugins.contacts;

public class GroupSettings {
  private boolean title = false;
  private boolean systemId = false;
  private boolean notes = false;
  private boolean accountType = false;
  private boolean accountName = false;

  private GroupSettings(Builder builder) {
    this.title = builder.title;
    this.systemId = builder.systemId;
    this.notes = builder.notes;
    this.accountType = builder.accountType;
    this.accountName = builder.accountName;
  }

  public static class Builder {
    private boolean title;
    private boolean systemId;
    private boolean notes;
    private boolean accountType;
    private boolean accountName;

    public Builder title(boolean title) { this.title = title; return this; }
    public Builder systemId(boolean systemId) { this.systemId = systemId; return this; }
    public Builder notes(boolean notes) { this.notes = notes; return this; }
    public Builder accountType(boolean accountType) { this.accountType = accountType; return this; }
    public Builder accountName(boolean accountName) { this.accountName = accountName; return this; }

    public GroupSettings build() {
      return new GroupSettings(this);
    }
  }

  public boolean title() { return title; }
  public boolean systemId() { return systemId; }
  public boolean notes() { return notes; }
  public boolean accountType() { return accountType; }
  public boolean accountName() { return accountName; }
}
