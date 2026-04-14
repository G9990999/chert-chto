import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { EditBroadcast } from '../types';

let stompClient: Client | null = null;
const subscriptions: Map<string, () => void> = new Map();

/**
 * Connects to the WebSocket/STOMP broker at /ws.
 * Attaches the JWT token from localStorage as a STOMP header.
 */
export function connectWebSocket(token: string): Promise<void> {
  return new Promise((resolve, reject) => {
    stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      onConnect: () => {
        console.log('STOMP connected');
        resolve();
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame);
        reject(new Error(frame.headers?.message ?? 'STOMP error'));
      },
      reconnectDelay: 5000,
    });
    stompClient.activate();
  });
}

/**
 * Disconnects from the STOMP broker.
 */
export function disconnectWebSocket(): void {
  if (stompClient?.active) {
    stompClient.deactivate();
    stompClient = null;
  }
  subscriptions.clear();
}

/**
 * Subscribes to real-time edit events for a page.
 *
 * @param pageId   the page to subscribe to
 * @param callback called with each broadcast event from other editors
 * @returns unsubscribe function
 */
export function subscribeToPage(
  pageId: string,
  callback: (event: EditBroadcast) => void
): () => void {
  if (!stompClient?.active) {
    console.warn('STOMP not connected — skipping subscription');
    return () => {};
  }

  const sub = stompClient.subscribe(
    `/topic/pages/${pageId}`,
    (message: IMessage) => {
      try {
        const event: EditBroadcast = JSON.parse(message.body);
        callback(event);
      } catch (e) {
        console.error('Failed to parse WebSocket message', e);
      }
    }
  );

  const unsubscribe = () => sub.unsubscribe();
  subscriptions.set(pageId, unsubscribe);
  return unsubscribe;
}

/**
 * Sends an edit event for the current page to the STOMP broker.
 *
 * @param pageId         the page being edited
 * @param content        current TipTap JSON content
 * @param cursorPosition optional cursor position for presence
 */
export function sendEditEvent(
  pageId: string,
  content: string,
  cursorPosition?: number
): void {
  if (!stompClient?.active) return;
  stompClient.publish({
    destination: `/app/pages/${pageId}/edit`,
    body: JSON.stringify({ content, cursorPosition: cursorPosition ?? null }),
  });
}
