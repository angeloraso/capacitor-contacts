import type { PermissionState } from "@capacitor/core";

export interface PermissionStatus {
  display: PermissionState;
}

export interface Contact {
  contactId: string;
  displayName?: string;
  phoneNumbers: PhoneNumber[];
  emails: EmailAddress[];
  photoThumbnail?: string;
  organizationName?: string;
  organizationRole?: string;
  birthday?: string;
}
export interface Group {
  groupId: string;
  accountType?: string;
  accountName: string;
  title: string;
}

export interface PhoneNumber {
  label?: string;
  number?: string;
}

export interface EmailAddress {
  label?: string;
  address?: string;
}

export interface ContactsPlugin {
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
  getContacts(): Promise<{ contacts: Contact[]}>;
  createContact(data: {name?: string, number: string}): Promise<void>;
  addToExistingContact(data: {name?: string, number: string}): Promise<void>;
  deleteContact(data: {contactId: string}): Promise<void>;
  getGroups(): Promise<{ groups: Group[]}>;
  getContactGroups(): Promise<{[key: string]: Group[]}>;
}
