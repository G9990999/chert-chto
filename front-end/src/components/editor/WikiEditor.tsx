import { useEffect, useRef, useCallback, useState } from 'react';
import { useEditor, EditorContent, BubbleMenu } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Placeholder from '@tiptap/extension-placeholder';
import Link from '@tiptap/extension-link';
import Table from '@tiptap/extension-table';
import TableRow from '@tiptap/extension-table-row';
import TableHeader from '@tiptap/extension-table-header';
import TableCell from '@tiptap/extension-table-cell';
import Highlight from '@tiptap/extension-highlight';
import TaskList from '@tiptap/extension-task-list';
import TaskItem from '@tiptap/extension-task-item';
import { SlashMenu } from './SlashMenu';
import { useLocalCache } from '../../hooks/useLocalCache';
import { sendEditEvent, subscribeToPage } from '../../services/websocket';
import type { EditBroadcast } from '../../types';

interface Props {
  pageId: string;
  initialContent: string;
  onSave: (content: string) => void;
  currentUsername: string;
}

const AUTOSAVE_DEBOUNCE_MS = 1500;

/**
 * Main wiki page editor built on TipTap.
 *
 * Features:
 * - Rich text editing with StarterKit extensions
 * - Slash-menu for quick block insertion (/ commands)
 * - Real-time collaborative editing via STOMP WebSocket
 * - Inline autosave to localStorage (local cache) and server
 * - Backlink support via custom link extension
 */
export function WikiEditor({ pageId, initialContent, onSave, currentUsername }: Props) {
  const { saveDraft, loadDraft, clearDraft } = useLocalCache(pageId);
  const [collaborators, setCollaborators] = useState<string[]>([]);
  const [lastSaved, setLastSaved] = useState<Date | null>(null);
  const autosaveTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const ignoreNextBroadcast = useRef(false);

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        heading: { levels: [1, 2, 3] },
        codeBlock: false,
      }),
      Placeholder.configure({
        placeholder: 'Type / for commands, or start writing…',
      }),
      Link.configure({ openOnClick: false }),
      Table.configure({ resizable: true }),
      TableRow,
      TableHeader,
      TableCell,
      Highlight,
      TaskList,
      TaskItem.configure({ nested: true }),
    ],
    content: (() => {
      // Prefer local draft over server content
      const draft = loadDraft();
      if (draft) {
        try {
          return JSON.parse(draft.content);
        } catch { /* fall through */ }
      }
      if (initialContent) {
        try {
          return JSON.parse(initialContent);
        } catch {
          return initialContent;
        }
      }
      return '';
    })(),
    onUpdate: ({ editor }) => {
      const json = JSON.stringify(editor.getJSON());

      // Save to local cache immediately
      saveDraft('', json);

      // Broadcast edit to collaborators
      const pos = editor.state.selection.from;
      sendEditEvent(pageId, json, pos);

      // Debounced server save
      if (autosaveTimer.current) clearTimeout(autosaveTimer.current);
      autosaveTimer.current = setTimeout(() => {
        onSave(json);
        clearDraft();
        setLastSaved(new Date());
      }, AUTOSAVE_DEBOUNCE_MS);
    },
  });

  // Subscribe to collaborative edits from other users
  useEffect(() => {
    const unsubscribe = subscribeToPage(pageId, (event: EditBroadcast) => {
      if (event.editor === currentUsername) return; // ignore own broadcasts
      if (!editor || ignoreNextBroadcast.current) return;

      // Track active collaborators
      setCollaborators((prev) =>
        prev.includes(event.editor) ? prev : [...prev, event.editor]
      );

      // Apply remote content (simple last-write-wins for hackathon scope)
      ignoreNextBroadcast.current = true;
      try {
        const remoteJson = JSON.parse(event.content);
        editor.commands.setContent(remoteJson, false);
      } catch { /* ignore malformed */ }
      ignoreNextBroadcast.current = false;
    });

    return () => {
      unsubscribe();
      if (autosaveTimer.current) clearTimeout(autosaveTimer.current);
    };
  }, [pageId, editor, currentUsername]);

  const insertTable = useCallback(() => {
    editor?.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run();
  }, [editor]);

  if (!editor) return null;

  return (
    <div className="wiki-editor">
      {/* Collaborator presence bar */}
      {collaborators.length > 0 && (
        <div className="collaborators">
          {collaborators.map((name) => (
            <span key={name} className="collaborator-badge" title={`${name} is editing`}>
              {name[0].toUpperCase()}
            </span>
          ))}
          <span className="collab-label">
            {collaborators.join(', ')} {collaborators.length === 1 ? 'is' : 'are'} editing
          </span>
        </div>
      )}

      {/* Bubble menu for inline formatting */}
      <BubbleMenu editor={editor} tippyOptions={{ duration: 100 }}>
        <div className="bubble-menu">
          <button
            onClick={() => editor.chain().focus().toggleBold().run()}
            className={editor.isActive('bold') ? 'active' : ''}
          >
            B
          </button>
          <button
            onClick={() => editor.chain().focus().toggleItalic().run()}
            className={editor.isActive('italic') ? 'active' : ''}
          >
            I
          </button>
          <button
            onClick={() => editor.chain().focus().toggleHighlight().run()}
            className={editor.isActive('highlight') ? 'active' : ''}
          >
            H
          </button>
          <button
            onClick={() => {
              const url = prompt('Link URL:');
              if (url) editor.chain().focus().setLink({ href: url }).run();
            }}
          >
            🔗
          </button>
        </div>
      </BubbleMenu>

      {/* Slash-menu */}
      <SlashMenu editor={editor} onInsertTable={insertTable} />

      {/* Editor canvas */}
      <EditorContent editor={editor} className="editor-content" />

      {/* Status bar */}
      <div className="editor-status">
        {lastSaved ? (
          <span>Saved {lastSaved.toLocaleTimeString()}</span>
        ) : (
          <span>Unsaved changes</span>
        )}
      </div>
    </div>
  );
}
