# @anuradev/capacitor-contacts

Capacitor plugin to get and modify mobile contacts

## Install

```bash
npm install @anuradev/capacitor-contacts
npx cap sync
```

## API

<docgen-index>

* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [`getContacts()`](#getcontacts)
* [`createContact(...)`](#createcontact)
* [`addToExistingContact(...)`](#addtoexistingcontact)
* [`deleteContact(...)`](#deletecontact)
* [`getGroups()`](#getgroups)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionStatus>
```

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### requestPermissions()

```typescript
requestPermissions() => Promise<PermissionStatus>
```

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### getContacts()

```typescript
getContacts() => Promise<{ path: string; }>
```

**Returns:** <code>Promise&lt;{ path: string; }&gt;</code>

--------------------


### createContact(...)

```typescript
createContact(data: { name?: string; number: string; }) => Promise<void>
```

| Param      | Type                                            |
| ---------- | ----------------------------------------------- |
| **`data`** | <code>{ name?: string; number: string; }</code> |

--------------------


### addToExistingContact(...)

```typescript
addToExistingContact(data: { name?: string; number: string; }) => Promise<void>
```

| Param      | Type                                            |
| ---------- | ----------------------------------------------- |
| **`data`** | <code>{ name?: string; number: string; }</code> |

--------------------


### deleteContact(...)

```typescript
deleteContact(data: { contactId: string; }) => Promise<void>
```

| Param      | Type                                |
| ---------- | ----------------------------------- |
| **`data`** | <code>{ contactId: string; }</code> |

--------------------


### getGroups()

```typescript
getGroups() => Promise<{ path: string; count: number; }>
```

**Returns:** <code>Promise&lt;{ path: string; count: number; }&gt;</code>

--------------------


### Interfaces


#### PermissionStatus

| Prop          | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`display`** | <code><a href="#permissionstate">PermissionState</a></code> |


### Type Aliases


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>

</docgen-api>
