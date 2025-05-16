/* eslint-disable @typescript-eslint/no-unused-vars */
import { WebPlugin } from '@capacitor/core';
export class ContactsWeb extends WebPlugin {
    async checkPermissions() {
        throw this.unimplemented('Not implemented on web.');
    }
    async requestPermissions() {
        throw this.unimplemented('Not implemented on web.');
    }
    async getContacts() {
        throw this.unimplemented('Not implemented on web.');
    }
    async createContact(_data) {
        throw this.unimplemented('Not implemented on web.');
    }
    async addToExistingContact(_data) {
        throw this.unimplemented('Not implemented on web.');
    }
    async deleteContact(_data) {
        throw this.unimplemented('Not implemented on web.');
    }
    async getGroups() {
        throw this.unimplemented('Not implemented on web.');
    }
}
//# sourceMappingURL=web.js.map