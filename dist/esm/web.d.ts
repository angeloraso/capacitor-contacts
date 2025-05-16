import { WebPlugin } from '@capacitor/core';
import type { ContactsPlugin, Group, PermissionStatus } from './definitions';
export declare class ContactsWeb extends WebPlugin implements ContactsPlugin {
    checkPermissions(): Promise<PermissionStatus>;
    requestPermissions(): Promise<PermissionStatus>;
    getContacts(): Promise<{
        contactFilePath: string;
    }>;
    createContact(_data: {
        nam?: string;
        number: string;
    }): Promise<void>;
    addToExistingContact(_data: {
        nam?: string;
        number: string;
    }): Promise<void>;
    deleteContact(_data: {
        contactId: string;
    }): Promise<void>;
    getGroups(): Promise<{
        groups: Group[];
    }>;
}
