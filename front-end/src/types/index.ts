export type Role = 'USER' | 'MANAGER' | 'ADMIN';

export interface AuthResponse {
  token: string;
  userId: string;
  username: string;
  role: Role;
  displayName: string;
}

export interface PageSummary {
  id: string;
  title: string;
  authorName: string;
  updatedAt: string;
  publicPage: boolean;
}

export interface PageResponse {
  id: string;
  title: string;
  content: string | null;
  authorId: string;
  authorName: string;
  createdAt: string;
  updatedAt: string;
  version: number;
  publicPage: boolean;
  linkedPageIds: string[];
  backlinks: string[];
}

export interface TableRecord {
  recordId: string;
  fields: Record<string, unknown>;
  createdAt: number;
  updatedAt: number;
}

export interface TableField {
  id: string;
  name: string;
  type: string;
}

export interface TableView {
  id: string;
  name: string;
  type: string;
}

export interface EditBroadcast {
  pageId: string;
  editor: string;
  content: string;
  cursorPosition: number | null;
  timestamp: string;
}

export interface Collaborator {
  username: string;
  cursorPosition: number | null;
  lastSeen: string;
}
