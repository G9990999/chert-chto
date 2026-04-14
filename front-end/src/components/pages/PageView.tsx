import { useEffect, useState, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getPage, updatePage, listPages, createDatasheet } from '../../services/api';
import type { PageResponse, PageSummary } from '../../types';
import { WikiEditor } from '../editor/WikiEditor';
import { TableEmbed } from '../tables/TableEmbed';
import { useAuthStore } from '../../store/authStore';
import { connectWebSocket } from '../../services/websocket';
import { formatDistanceToNow } from 'date-fns';

/**
 * Full page view and editor.
 *
 * Loads the page from the API, initialises the WebSocket connection
 * for collaborative editing, and renders the TipTap editor.
 * Also displays backlinks and outgoing page links.
 */
export function PageView() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { token, username } = useAuthStore();
  const [page, setPage] = useState<PageResponse | null>(null);
  const [title, setTitle] = useState('');
  const [allPages, setAllPages] = useState<PageSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [wsReady, setWsReady] = useState(false);
  const [embedDstId, setEmbedDstId] = useState('');
  const [showEmbedDialog, setShowEmbedDialog] = useState(false);
  const [newTableName, setNewTableName] = useState('');
  const [creatingTable, setCreatingTable] = useState(false);
  const [tableError, setTableError] = useState('');

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    Promise.all([getPage(id), listPages()])
      .then(([p, pages]) => {
        setPage(p);
        setTitle(p.title);
        setAllPages(pages);
      })
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    if (!token) return;
    connectWebSocket(token)
      .then(() => setWsReady(true))
      .catch((e) => console.warn('WebSocket connect failed:', e));
  }, [token]);

  const handleSave = useCallback(
    async (content: string) => {
      if (!id || !page) return;
      setSaving(true);
      try {
        const updated = await updatePage(id, {
          title,
          content,
          publicPage: page.publicPage,
          linkedPageIds: Array.from(page.linkedPageIds),
        });
        setPage(updated);
      } catch (e) {
        console.error('Save failed', e);
      } finally {
        setSaving(false);
      }
    },
    [id, page, title]
  );

  const handleTitleBlur = useCallback(async () => {
    if (!id || !page || title === page.title) return;
    setSaving(true);
    try {
      const updated = await updatePage(id, {
        title,
        content: page.content ?? '',
        publicPage: page.publicPage,
        linkedPageIds: Array.from(page.linkedPageIds),
      });
      setPage(updated);
    } finally {
      setSaving(false);
    }
  }, [id, page, title]);

  const handleCreateTable = useCallback(async () => {
    if (!newTableName.trim()) return;
    setCreatingTable(true);
    setTableError('');
    try {
      const result = (await createDatasheet(newTableName.trim())) as { data?: { id?: string } };
      const dstId = result?.data?.id;
      if (dstId) {
        setEmbedDstId(dstId);
        setNewTableName('');
        setShowEmbedDialog(false);
      } else {
        setTableError('Failed to get new table ID from response');
      }
    } catch {
      setTableError('Failed to create table');
    } finally {
      setCreatingTable(false);
    }
  }, [newTableName]);

  if (loading) return <div className="loading">Loading page…</div>;
  if (!page) return <div className="error">Page not found</div>;

  const backlinkPages = allPages.filter((p) =>
    page.backlinks.includes(p.id)
  );
  const linkedPages = allPages.filter((p) =>
    page.linkedPageIds.includes(p.id)
  );

  return (
    <div className="page-view">
      {/* Header */}
      <div className="page-view-header">
        <button onClick={() => navigate('/pages')} className="btn-back">← Pages</button>
        <div className="page-meta">
          <span>by {page.authorName}</span>
          <span> · </span>
          <span>
            updated {formatDistanceToNow(new Date(page.updatedAt), { addSuffix: true })}
          </span>
          {saving && <span className="saving">Saving…</span>}
          {wsReady && <span className="ws-badge">● Live</span>}
        </div>
      </div>

      {/* Title */}
      <input
        className="page-title-input"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onBlur={handleTitleBlur}
        placeholder="Page title"
      />

      {/* Table embed dialog */}
      <div className="embed-controls">
        <button onClick={() => setShowEmbedDialog((v) => !v)} className="btn-secondary">
          {showEmbedDialog ? 'Close Table Panel' : 'Embed MWS Table'}
        </button>
      </div>
      {showEmbedDialog && (
        <div className="embed-dialog">
          <div className="embed-section">
            <label>Embed existing table by ID:</label>
            <div className="embed-row">
              <input
                value={embedDstId}
                onChange={(e) => setEmbedDstId(e.target.value)}
                placeholder="dstXXXXXXXXXXXX"
              />
            </div>
          </div>
          <div className="embed-section">
            <label>Or create a new MWS Table:</label>
            <div className="embed-row">
              <input
                value={newTableName}
                onChange={(e) => setNewTableName(e.target.value)}
                placeholder="Table name"
              />
              <button
                onClick={handleCreateTable}
                disabled={creatingTable || !newTableName.trim()}
                className="btn-primary"
              >
                {creatingTable ? 'Creating…' : 'Create Table'}
              </button>
            </div>
            {tableError && <div className="embed-error">{tableError}</div>}
          </div>
        </div>
      )}

      {/* Live table embed */}
      {embedDstId && <TableEmbed dstId={embedDstId} />}

      {/* Editor */}
      <WikiEditor
        pageId={page.id}
        initialContent={page.content}
        onSave={handleSave}
        currentUsername={username ?? ''}
      />

      {/* Backlinks */}
      {backlinkPages.length > 0 && (
        <div className="backlinks">
          <h3>Backlinks ({backlinkPages.length})</h3>
          <ul>
            {backlinkPages.map((p) => (
              <li key={p.id}>
                <Link to={`/pages/${p.id}`}>{p.title}</Link>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Outgoing links */}
      {linkedPages.length > 0 && (
        <div className="outgoing-links">
          <h3>Links to</h3>
          <ul>
            {linkedPages.map((p) => (
              <li key={p.id}>
                <Link to={`/pages/${p.id}`}>{p.title}</Link>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
