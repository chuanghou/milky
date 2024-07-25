package com.stellariver.milky.demo.adapter.sse;

import com.stellariver.milky.common.base.Result;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
@RestController
@RequestMapping("/sse")
public class SseEmitterController {

    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    @PostMapping("/send")
    public Result<Void> send() throws IOException {
        for (SseEmitter sseEmitter : sseEmitters.values()) {
            sseEmitter.send(new Date());
        }
        return Result.success();
    }

    @PostMapping("/close")
    public Result<Void> close() {
        for (SseEmitter sseEmitter : sseEmitters.values()) {
            sseEmitter.complete();
        }
        return Result.success();
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestHeader String token) throws IOException {
        String userId = getUserId(token);

        SseEmitter sseEmitter = sseEmitters.compute(userId, (id, oldEmitter) -> {
            SseEmitter newEmitter = new SseEmitter(-1L);
            newEmitter.onTimeout(() -> sseEmitters.remove(id));
            newEmitter.onCompletion(() -> sseEmitters.remove(id));
            newEmitter.onError(throwable -> {
                log.error("emitter {} onError", id, throwable);
                sseEmitters.remove(id);
            });
            if (oldEmitter != null) {
                oldEmitter.complete();
            }
            return newEmitter;
        });

        sseEmitter.send(Message.subscribed());
        return sseEmitter;
    }

    private String getUserId(String token) {
        return token;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static public class Message<T> {

        Map<Header, String> header;
        T body;

        public static Message<Void> subscribed() {
            Map<Header, String> headers = new HashMap<>();
            headers.put(Header.SUBSCRIBED, String.valueOf(true));
            return Message.<Void>builder().header(headers).build();
        }
    }

    enum Header {
        SUBSCRIBED
    }

}
