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
* [`getContacts(...)`](#getcontacts)
* [`createContact(...)`](#createcontact)
* [`addToExistingContact(...)`](#addtoexistingcontact)
* [`deleteContact(...)`](#deletecontact)
* [`getGroups(...)`](#getgroups)
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


### getContacts(...)

```typescript
getContacts(settings: ContactSettings) => Promise<{ contacts: Contact[]; }>
```

| Param          | Type                                                        |
| -------------- | ----------------------------------------------------------- |
| **`settings`** | <code><a href="#contactsettings">ContactSettings</a></code> |

**Returns:** <code>Promise&lt;{ contacts: Contact[]; }&gt;</code>

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


### getGroups(...)

```typescript
getGroups(settings: GroupSettings) => Promise<{ groups: Group[]; }>
```

| Param          | Type                                                    |
| -------------- | ------------------------------------------------------- |
| **`settings`** | <code><a href="#groupsettings">GroupSettings</a></code> |

**Returns:** <code>Promise&lt;{ groups: Group[]; }&gt;</code>

--------------------


### Interfaces


#### PermissionStatus

| Prop           | Type                                                        |
| -------------- | ----------------------------------------------------------- |
| **`contacts`** | <code><a href="#permissionstate">PermissionState</a></code> |


#### Contact

| Prop               | Type                        |
| ------------------ | --------------------------- |
| **`id`**           | <code>string</code>         |
| **`name`**         | <code>string</code>         |
| **`phones`**       | <code>PhoneNumber[]</code>  |
| **`emails`**       | <code>EmailAddress[]</code> |
| **`birthday`**     | <code>string</code>         |
| **`organization`** | <code>string</code>         |
| **`role`**         | <code>string</code>         |
| **`photo`**        | <code>string</code>         |


#### PhoneNumber

| Prop         | Type                |
| ------------ | ------------------- |
| **`label`**  | <code>string</code> |
| **`number`** | <code>string</code> |


#### EmailAddress

| Prop          | Type                |
| ------------- | ------------------- |
| **`label`**   | <code>string</code> |
| **`address`** | <code>string</code> |


#### ContactSettings

| Prop               | Type                 |
| ------------------ | -------------------- |
| **`name`**         | <code>boolean</code> |
| **`phones`**       | <code>boolean</code> |
| **`emails`**       | <code>boolean</code> |
| **`birthday`**     | <code>boolean</code> |
| **`organization`** | <code>boolean</code> |
| **`role`**         | <code>boolean</code> |
| **`photo`**        | <code>boolean</code> |


#### Group

| Prop              | Type                |
| ----------------- | ------------------- |
| **`id`**          | <code>string</code> |
| **`accountType`** | <code>string</code> |
| **`accountName`** | <code>string</code> |
| **`title`**       | <code>string</code> |


#### GroupSettings

| Prop              | Type                 |
| ----------------- | -------------------- |
| **`accountType`** | <code>boolean</code> |
| **`accountName`** | <code>boolean</code> |
| **`title`**       | <code>boolean</code> |


### Type Aliases


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>

</docgen-api>
