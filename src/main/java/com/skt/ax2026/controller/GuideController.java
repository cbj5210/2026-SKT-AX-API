package com.skt.ax2026.controller;

import com.skt.ax2026.dto.GuideRequest;
import com.skt.ax2026.dto.GuideResponse;
import com.skt.ax2026.service.GuideService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/guide")
public class GuideController {

    private final GuideService guideService;

    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    @PostMapping("/analyze")
    public GuideResponse analyze(@RequestBody GuideRequest request) {
        return guideService.analyzeUiTree(request);
    }
}
