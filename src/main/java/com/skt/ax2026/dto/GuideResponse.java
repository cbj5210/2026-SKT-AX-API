package com.skt.ax2026.dto;

public record GuideResponse(
    TargetElement targetElement,
    String guideMessage,
    String actionStatus
) {
    public record TargetElement(
        int x,
        int y,
        String description
    ) {}
}
