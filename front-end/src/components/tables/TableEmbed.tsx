import { useEffect, useState } from 'react';
import { getFields, getRecords } from '../../services/api';

interface Props {
  dstId: string;
}

interface Field {
  id: string;
  name: string;
  type: string;
}

interface TableRow {
  recordId: string;
  fields: Record<string, unknown>;
}

/**
 * Renders a live MWS Tables datasheet embedded inside a wiki page.
 * Fetches fields and records from the backend proxy (which in turn
 * calls the MWS Tables API with circuit-breaker protection).
 */
export function TableEmbed({ dstId }: Props) {
  const [fields, setFields] = useState<Field[]>([]);
  const [records, setRecords] = useState<TableRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    setError('');
    Promise.all([
      getFields(dstId) as Promise<{ data?: { fields?: Field[] } }>,
      getRecords(dstId) as Promise<{ data?: { records?: TableRow[] } }>,
    ])
      .then(([fieldsRes, recordsRes]) => {
        setFields((fieldsRes as { data?: { fields?: Field[] } }).data?.fields ?? []);
        setRecords((recordsRes as { data?: { records?: TableRow[] } }).data?.records ?? []);
      })
      .catch(() => setError('Failed to load table data'))
      .finally(() => setLoading(false));
  }, [dstId]);

  if (loading) return <div className="table-loading">Loading table…</div>;
  if (error) return <div className="table-error">{error}</div>;

  return (
    <div className="table-embed">
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              {fields.map((f) => (
                <th key={f.id}>{f.name}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {records.map((rec) => (
              <tr key={rec.recordId}>
                {fields.map((f) => (
                  <td key={f.id}>
                    {renderCell(rec.fields[f.name] ?? rec.fields[f.id])}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {records.length === 0 && (
        <p className="table-empty">No records in this table.</p>
      )}
    </div>
  );
}

function renderCell(value: unknown): React.ReactNode {
  if (value === null || value === undefined) return '';
  if (Array.isArray(value)) return value.join(', ');
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
}
