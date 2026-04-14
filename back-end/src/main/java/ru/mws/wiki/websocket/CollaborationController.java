package ru.mws.wiki.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket/STOMP controller for real-time collaborative page editing.
 *
 * <p>Clients send editing events to {@code /app/pages/{pageId}/edit}
 * and receive updates via subscription to {@code /topic/pages/{pageId}}.</p>
 *
 * <p>Each edit event carries the patch (TipTap steps or full content),
 * the editor's username, and a timestamp for ordering.</p>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CollaborationController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Receives an edit event and broadcasts it to all collaborators on the page.
     *
     * @param pageId    the page being edited (path variable)
     * @param event     the edit event payload from the client
     * @param principal the authenticated user sending the event
     */
    @MessageMapping("/pages/{pageId}/edit")
    public void handleEdit(
            @DestinationVariable UUID pageId,
            @Payload EditEvent event,
            Principal principal) {

        String editor = principal != null ? principal.getName() : "anonymous";
        log.debug("Edit event on page {} from {}", pageId, editor);

        EditBroadcast broadcast = new EditBroadcast(
                pageId,
                editor,
                event.content(),
                event.cursorPosition(),
                Instant.now()
        );

        messagingTemplate.convertAndSend("/topic/pages/" + pageId, broadcast);
    }

    /**
     * Inbound edit event from a client collaborator.
     *
     * @param content        the updated TipTap JSON content
     * @param cursorPosition optional cursor position for presence indicators
     */
    public record EditEvent(
            String content,
            Integer cursorPosition
    ) {}

    /**
     * Broadcast message sent to all subscribers of a page topic.
     *
     * @param pageId         the page being edited
     * @param editor         username of the editor
     * @param content        updated TipTap JSON content
     * @param cursorPosition optional cursor position
     * @param timestamp      server-side timestamp of the event
     */
    public record EditBroadcast(
            UUID pageId,
            String editor,
            String content,
            Integer cursorPosition,
            Instant timestamp
    ) {}
}
