import type { PermissionState } from "@capacitor/core";
export interface PermissionStatus {
    display: PermissionState;
}
export interface Contact {
    id: string;
    name?: string;
    phones: PhoneNumber[];
    emails: EmailAddress[];
    birthday?: string;
    organization?: string;
    role?: string;
    photo?: string;
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
    getContacts(): Promise<{
        fileName: string;
        count: number;
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
    getGroups(): Promise<{
        fileName: string;
        count: number;
    }>;
}
