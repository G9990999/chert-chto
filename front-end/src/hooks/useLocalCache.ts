import { useCallback } from 'react';

const CACHE_PREFIX = 'mws-wiki-page-draft-';
const MAX_DRAFT_AGE_MS = 24 * 60 * 60 * 1000; // 24 hours

interface DraftEntry {
  content: string;
  title: string;
  savedAt: number;
}

/**
 * Hook for managing page drafts in localStorage.
 * Provides inline autosave that survives browser refreshes and network outages.
 */
export function useLocalCache(pageId: string) {
  const key = `${CACHE_PREFIX}${pageId}`;

  /** Save a draft to localStorage */
  const saveDraft = useCallback(
    (title: string, content: string) => {
      const entry: DraftEntry = { content, title, savedAt: Date.now() };
      try {
        localStorage.setItem(key, JSON.stringify(entry));
      } catch {
        // Storage quota exceeded — silently ignore
      }
    },
    [key]
  );

  /** Load the most recent draft, or null if none/expired */
  const loadDraft = useCallback((): DraftEntry | null => {
    try {
      const raw = localStorage.getItem(key);
      if (!raw) return null;
      const entry: DraftEntry = JSON.parse(raw);
      if (Date.now() - entry.savedAt > MAX_DRAFT_AGE_MS) {
        localStorage.removeItem(key);
        return null;
      }
      return entry;
    } catch {
      return null;
    }
  }, [key]);

  /** Clear the draft (e.g. after successful server save) */
  const clearDraft = useCallback(() => {
    localStorage.removeItem(key);
  }, [key]);

  return { saveDraft, loadDraft, clearDraft };
}
