package ar.com.anura.plugins.contacts;

public class ContactSettings {
  private boolean name = false;
  private boolean phones = false;
  private boolean emails = false;
  private boolean birthday = false;
  private boolean organization = false;
  private boolean role = false;
  private boolean photo = false;


  private ContactSettings(Builder builder) {
    this.name = builder.name;
    this.phones = builder.phones;
    this.emails = builder.emails;
    this.birthday = builder.birthday;
    this.organization = builder.organization;
    this.role = builder.role;
    this.photo = builder.photo;
  }

  public static class Builder {
    private boolean name;
    private boolean phones;
    private boolean emails;
    private boolean birthday;
    private boolean organization;
    private boolean role;
    private boolean photo;

    public Builder name(boolean name) { this.name = name; return this; }
    public Builder phones(boolean phones) { this.phones = phones; return this; }
    public Builder emails(boolean emails) { this.emails = emails; return this; }
    public Builder birthday(boolean birthday) { this.birthday = birthday; return this; }
    public Builder organization(boolean organization) { this.organization = organization; return this; }
    public Builder role(boolean role) { this.role = role; return this; }
    public Builder photo(boolean photo) { this.photo = photo; return this; }

    public ContactSettings build() {
      return new ContactSettings(this);
    }
  }

  public boolean name() {
    return name;
  }

  public boolean phones() {
    return phones;
  }

  public boolean emails() {
    return emails;
  }

  public boolean birthday() {
    return birthday;
  }

  public boolean organization() {
    return organization;
  }

  public boolean role() {
    return role;
  }

  public boolean photo() {
    return photo;
  }
}
