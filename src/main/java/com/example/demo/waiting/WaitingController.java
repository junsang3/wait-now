package com.example.demo.waiting;

import com.example.demo.store.Store;
import com.example.demo.store.StoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class WaitingController {
    private final StoreService storeService;
    private final WaitingService waitingService;

    public WaitingController(StoreService storeService, WaitingService waitingService) {
        this.storeService = storeService;
        this.waitingService = waitingService;
    }

    @GetMapping("/stores/{storeId}")
    public String getStore(@PathVariable Long storeId, Model model) {
        Store store = storeService.findById(storeId);
        long waitingCount = waitingService.getWaitingCount(storeId);
        model.addAttribute("store", store);
        model.addAttribute("waitingCount", waitingCount);
        return "store";
    }

    @PostMapping("/stores/{storeId}/open")
    public String openStore(@PathVariable Long storeId, Model model) {
        Store store = storeService.open(storeId);
        model.addAttribute("store", store);
        return "admin :: closeStore";
    }

    @PostMapping("/stores/{storeId}/close")
    public String closeStore(@PathVariable Long storeId, Model model) {
        Store store = storeService.close(storeId);
        model.addAttribute("store", store);
        return "admin :: openStore";
    }

    @GetMapping("/waitings/{ticketToken}")
    public String getWaiting(@PathVariable String ticketToken, Model model) {
        Waiting waiting = waitingService.findByTicketToken(ticketToken);
        long position = waitingService.getPosition(waiting.getStore().getId(), waiting.getId());
        model.addAttribute("waiting", waiting);
        model.addAttribute("position", position);
        return "waiting";
    }

    @PostMapping("/waitings")
    public String createWaiting(@RequestParam Long storeId, @RequestParam int partySize, @RequestParam String phoneNumber) {
        String ticketToken = waitingService.issue(storeId, partySize, phoneNumber);
        return "redirect:/waitings/" + ticketToken;
    }

    @GetMapping("/admin/stores/{storeId}")
    public String getAdminStore(@PathVariable Long storeId, Model model) {
        Store store = storeService.findById(storeId);
        List<Waiting> waitings = waitingService.getWaitings(storeId, WaitingStatus.WAITING);
        model.addAttribute("store", store);
        model.addAttribute("waitings", waitings);
        return "admin";
    }
}
