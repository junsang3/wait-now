package com.example.demo.waiting;

import com.example.demo.store.Store;
import com.example.demo.store.StoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Controller
public class WaitingController {
    private final StoreService storeService;
    private final WaitingService waitingService;
    private final WaitingSseEmitters emitters;

    @Value("${vapid.public-key}")
    private String vapidPublicKey;

    public WaitingController(StoreService storeService, WaitingService waitingService, WaitingSseEmitters emitters) {
        this.storeService = storeService;
        this.waitingService = waitingService;
        this.emitters = emitters;
    }

    @GetMapping("/stores/{storeId}")
    public String getStore(@PathVariable Long storeId, Model model) {
        Store store = storeService.findById(storeId);
        long waitingCount = waitingService.getWaitingCount(storeId);
        model.addAttribute("store", store);
        model.addAttribute("waitingCount", waitingCount);
        return "store";
    }

    @GetMapping("/stores/{storeId}/waitingCount")
    public String getStoreWaitingCount(@PathVariable Long storeId, Model model) {
        long waitingCount = waitingService.getWaitingCount(storeId);
        model.addAttribute("waitingCount", waitingCount);
        return "store :: waitingCount";
    }

    @GetMapping(value = "/stores/{storeId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter subscribeStore(@PathVariable Long storeId) {
        return emitters.subscribe(storeId);
    }

    @GetMapping("/waitings/{ticketToken}")
    public String getWaiting(@PathVariable String ticketToken, Model model) {
        Waiting waiting = waitingService.findByTicketToken(ticketToken);
        long position = waitingService.getPosition(waiting.getStore().getId(), waiting.getId());
        model.addAttribute("waiting", waiting);
        model.addAttribute("position", position);
        model.addAttribute("vapidPublicKey", vapidPublicKey);
        return "waiting";
    }

    @PostMapping("/waitings")
    public String createWaiting(@RequestParam Long storeId, @RequestParam int partySize, @RequestParam String phoneNumber) {
        String ticketToken = waitingService.issue(storeId, partySize, phoneNumber);
        emitters.send(storeId);
        return "redirect:/waitings/" + ticketToken;
    }

    @PostMapping("/waitings/cancel")
    public String cancelWaiting(@RequestParam String ticketToken) {
        Waiting waiting = waitingService.cancel(ticketToken);
        emitters.send(waiting.getStore().getId());
        return "redirect:/stores/" + waiting.getStore().getId();
    }

    @PostMapping("/waitings/{ticketToken}/subscribe")
    public ResponseEntity<Void> subscribePush(@PathVariable String ticketToken, @RequestBody JsonNode body) {
        String endpoint = body.get("endpoint").asString();
        String p256dh = body.get("keys").get("p256dh").asString();
        String auth = body.get("keys").get("auth").asString();
        waitingService.subscribePush(ticketToken, endpoint, p256dh, auth);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/login")
    public String getLogin(Model model) {
        return "login";
    }

    @GetMapping("/admin/stores/{storeId}")
    public String getAdminStore(@PathVariable Long storeId, Model model) {
        Store store = storeService.findById(storeId);
        List<Waiting> waitings = waitingService.getWaitings(storeId, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED));
        model.addAttribute("store", store);
        model.addAttribute("waitings", waitings);
        model.addAttribute("tabUrl", "/admin/stores/" + storeId + "/active");
        return "admin";
    }

    @GetMapping(value = "/admin/stores/{storeId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter subscribe(@PathVariable Long storeId) {
        return emitters.subscribe(storeId);
    }

    @PostMapping("/admin/stores/{storeId}/open")
    public String openStore(@PathVariable Long storeId) {
        waitingService.openStore(storeId);
        return "redirect:/admin/stores/" + storeId;
    }

    @PostMapping("/admin/stores/{storeId}/close")
    public String closeStore(@PathVariable Long storeId) {
        waitingService.closeStore(storeId);
        return "redirect:/admin/stores/" + storeId;
    }

    @GetMapping("/admin/stores/{storeId}/active")
    public String getActiveWaitings(@PathVariable Long storeId, Model model) {
        List<Waiting> waitings = waitingService.getWaitings(storeId, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED));
        model.addAttribute("waitings", waitings);
        model.addAttribute("tabUrl", "/admin/stores/" + storeId + "/active");
        return "admin :: tabs";
    }

    @GetMapping("/admin/stores/{storeId}/seated")
    public String getSeatedWaitings(@PathVariable Long storeId, Model model) {
        List<Waiting> waitings = waitingService.getWaitings(storeId, List.of(WaitingStatus.SEATED));
        model.addAttribute("waitings", waitings);
        model.addAttribute("tabUrl", "/admin/stores/" + storeId + "/seated");
        return "admin :: tabs";
    }

    @GetMapping("/admin/stores/{storeId}/canceled")
    public String getCanceledWaitings(@PathVariable Long storeId, Model model) {
        List<Waiting> waitings = waitingService.getWaitings(storeId, List.of(WaitingStatus.CANCELED));
        model.addAttribute("waitings", waitings);
        model.addAttribute("tabUrl", "/admin/stores/" + storeId + "/canceled");
        return "admin :: tabs";
    }

    @PostMapping("/admin/waitings/{waitingId}/call")
    public ResponseEntity<Void> callWaiting(@PathVariable Long waitingId, Model model) {
        Waiting waiting = waitingService.call(waitingId);
        emitters.send(waiting.getStore().getId());
        waitingService.sendPush(waiting);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/waitings/{waitingId}/seat")
    public ResponseEntity<Void> seatWaiting(@PathVariable Long waitingId, Model model) {
        Waiting waiting = waitingService.seat(waitingId);
        emitters.send(waiting.getStore().getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/waitings/{waitingId}/cancel")
    public ResponseEntity<Void> cancelWaiting(@PathVariable Long waitingId, Model model) {
        Waiting waiting = waitingService.cancel(waitingId);
        emitters.send(waiting.getStore().getId());
        return ResponseEntity.ok().build();
    }
}
