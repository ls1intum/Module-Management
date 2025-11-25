export interface KeycloakCredential {
  id: string;
  type: string;
  userLabel?: string;
  createdDate?: number;
  credentialData?: string;
}

export interface InfoProperty {
  key: string;
  parameters: string[];
}

export interface UserCredentialMetadata {
  credential: KeycloakCredential;
  infoProperties?: InfoProperty[];
}

export interface KeycloakCredentialType {
  type: string;
  category: string;
  displayName: string;
  helptext: string;
  iconCssClass: string;
  createAction?: string;
  updateAction?: string;
  removeable: boolean;
  userCredentialMetadatas: UserCredentialMetadata[];
}

export interface Passkey {
  id: string;
  name?: string;
  createdAt?: Date;
}
