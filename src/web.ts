/* eslint-disable @typescript-eslint/no-unused-vars */
import { WebPlugin } from '@capacitor/core';

import type { ContactsPlugin, PermissionStatus } from './definitions';

export class ContactsWeb extends WebPlugin implements ContactsPlugin {
  async checkPermissions(): Promise<PermissionStatus> {
    throw this.unimplemented('Not implemented on web.');
  }

  async requestPermissions(): Promise<PermissionStatus> {
    throw this.unimplemented('Not implemented on web.');
  }

  async getContacts(): Promise<{ path: string }> {
    throw this.unimplemented('Not implemented on web.');
  }
  
  async createContact(_data: {nam?: string, number: string}): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async addToExistingContact(_data: {nam?: string, number: string}): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async deleteContact(_data: {contactId: string}): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async getGroups(): Promise<{ path: string, count: number }> {
    throw this.unimplemented('Not implemented on web.');
  }
}
