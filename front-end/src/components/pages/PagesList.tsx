import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { listPages, searchPages, createPage } from '../../services/api';
import type { PageSummary } from '../../types';
import { formatDistanceToNow } from 'date-fns';

/**
 * Page list view — shows all pages accessible to the current user.
 * Supports live search and quick page creation.
 */
export function PagesList() {
  const navigate = useNavigate();
  const [pages, setPages] = useState<PageSummary[]>([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    setLoading(true);
    const load = query
      ? searchPages(query)
      : listPages();
    load.then(setPages).finally(() => setLoading(false));
  }, [query]);

  const handleCreate = async () => {
    setCreating(true);
    try {
      const page = await createPage({
        title: 'Untitled',
        content: '',
        publicPage: false,
        linkedPageIds: [],
      });
      navigate(`/pages/${page.id}`);
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="pages-list">
      <div className="pages-header">
        <h1>Pages</h1>
        <button onClick={handleCreate} disabled={creating} className="btn-primary">
          {creating ? 'Creating…' : '+ New Page'}
        </button>
      </div>

      <div className="search-bar">
        <input
          type="search"
          placeholder="Search pages…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>

      {loading ? (
        <div className="loading">Loading…</div>
      ) : pages.length === 0 ? (
        <div className="empty">
          {query ? 'No pages match your search.' : 'No pages yet. Create your first one!'}
        </div>
      ) : (
        <ul className="page-items">
          {pages.map((p) => (
            <li key={p.id} className="page-item">
              <Link to={`/pages/${p.id}`} className="page-title">
                {p.title}
              </Link>
              <div className="page-meta">
                <span className="author">{p.authorName}</span>
                <span className="separator">·</span>
                <span className="date">
                  {formatDistanceToNow(new Date(p.updatedAt), { addSuffix: true })}
                </span>
                {p.publicPage && <span className="badge-public">Public</span>}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
