import { useEffect, useRef, useState, useCallback } from 'react';
import { type Editor } from '@tiptap/react';

interface SlashItem {
  title: string;
  description: string;
  shortcut: string;
  action: (editor: Editor) => void;
}

interface Props {
  editor: Editor;
  onInsertTable: () => void;
}

/**
 * Slash-menu (command palette) for the wiki editor.
 *
 * Opens when the user types "/" at the beginning of a line.
 * Supports keyboard navigation (↑↓ to select, Enter to execute, Escape to close).
 */
export function SlashMenu({ editor, onInsertTable }: Props) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState(0);
  const menuRef = useRef<HTMLDivElement>(null);

  const items: SlashItem[] = [
    {
      title: 'Heading 1',
      description: 'Big section heading',
      shortcut: 'h1',
      action: (e) => e.chain().focus().toggleHeading({ level: 1 }).run(),
    },
    {
      title: 'Heading 2',
      description: 'Medium section heading',
      shortcut: 'h2',
      action: (e) => e.chain().focus().toggleHeading({ level: 2 }).run(),
    },
    {
      title: 'Heading 3',
      description: 'Small section heading',
      shortcut: 'h3',
      action: (e) => e.chain().focus().toggleHeading({ level: 3 }).run(),
    },
    {
      title: 'Bullet List',
      description: 'Unordered list',
      shortcut: 'ul',
      action: (e) => e.chain().focus().toggleBulletList().run(),
    },
    {
      title: 'Numbered List',
      description: 'Ordered list',
      shortcut: 'ol',
      action: (e) => e.chain().focus().toggleOrderedList().run(),
    },
    {
      title: 'Task List',
      description: 'Checkboxes',
      shortcut: 'todo',
      action: (e) => e.chain().focus().toggleTaskList().run(),
    },
    {
      title: 'Quote',
      description: 'Blockquote',
      shortcut: 'quote',
      action: (e) => e.chain().focus().toggleBlockquote().run(),
    },
    {
      title: 'Code Block',
      description: 'Monospaced code',
      shortcut: 'code',
      action: (e) => e.chain().focus().toggleCode().run(),
    },
    {
      title: 'Table',
      description: 'Insert MWS table grid',
      shortcut: 'table',
      action: () => onInsertTable(),
    },
    {
      title: 'Divider',
      description: 'Horizontal rule',
      shortcut: 'hr',
      action: (e) => e.chain().focus().setHorizontalRule().run(),
    },
  ];

  const filtered = items.filter(
    (item) =>
      !query ||
      item.title.toLowerCase().includes(query.toLowerCase()) ||
      item.shortcut.includes(query.toLowerCase())
  );

  const executeItem = useCallback(
    (item: SlashItem) => {
      // Delete the "/" trigger character
      editor.chain().focus().deleteRange({
        from: editor.state.selection.from - query.length - 1,
        to: editor.state.selection.from,
      }).run();
      item.action(editor);
      setOpen(false);
      setQuery('');
    },
    [editor, query]
  );

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!open) return;
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setSelected((s) => Math.min(s + 1, filtered.length - 1));
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        setSelected((s) => Math.max(s - 1, 0));
      } else if (e.key === 'Enter') {
        e.preventDefault();
        if (filtered[selected]) executeItem(filtered[selected]);
      } else if (e.key === 'Escape') {
        setOpen(false);
        setQuery('');
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [open, filtered, selected, executeItem]);

  // Listen for "/" keystroke in the editor to open the menu
  useEffect(() => {
    const handleEditorKeyUp = (e: KeyboardEvent) => {
      if (e.key === '/') {
        setOpen(true);
        setQuery('');
        setSelected(0);
      } else if (open) {
        if (e.key === 'Backspace') {
          setQuery((q) => {
            if (q.length === 0) {
              setOpen(false);
              return '';
            }
            return q.slice(0, -1);
          });
        } else if (e.key.length === 1 && !e.ctrlKey && !e.metaKey) {
          setQuery((q) => q + e.key);
          setSelected(0);
        }
      }
    };
    const dom = editor.view.dom;
    dom.addEventListener('keyup', handleEditorKeyUp);
    return () => dom.removeEventListener('keyup', handleEditorKeyUp);
  }, [editor, open]);

  if (!open || filtered.length === 0) return null;

  return (
    <div ref={menuRef} className="slash-menu">
      {filtered.map((item, i) => (
        <button
          key={item.shortcut}
          className={`slash-item ${i === selected ? 'selected' : ''}`}
          onMouseDown={(e) => {
            e.preventDefault();
            executeItem(item);
          }}
        >
          <span className="slash-title">{item.title}</span>
          <span className="slash-desc">{item.description}</span>
          <kbd className="slash-shortcut">/{item.shortcut}</kbd>
        </button>
      ))}
    </div>
  );
}
