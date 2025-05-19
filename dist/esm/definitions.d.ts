import type { PermissionState } from "@capacitor/core";
export interface PermissionStatus {
    contacts: PermissionState;
}
export interface ContactSettings {
    name?: boolean;
    phones?: boolean;
    emails?: boolean;
    birthday?: boolean;
    organization?: boolean;
    role?: boolean;
    photo?: boolean;
}
export interface Contact {
    id: string;
    name?: string;
    phones?: PhoneNumber[];
    emails?: EmailAddress[];
    birthday?: string;
    organization?: string;
    role?: string;
    photo?: string;
}
export interface GroupSettings {
    accountType?: boolean;
    accountName?: boolean;
    title?: boolean;
}
export interface Group {
    id: string;
    accountType?: string;
    accountName?: string;
    title?: string;
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
    getContacts(settings: ContactSettings): Promise<{
        contacts: Contact[];
    }>;
    createContact(data: {
        name?: string;
        number: string;
    }): Promise<void>;
    addToExistingContact(data: {
        name?: string;
        number: string;
    }): Promise<void>;
    deleteContact(data: {
        contactId: string;
    }): Promise<void>;
    getGroups(settings: GroupSettings): Promise<{
        groups: Group[];
    }>;
}
