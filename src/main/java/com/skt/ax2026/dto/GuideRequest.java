package com.skt.ax2026.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GuideRequest(
    String userGoal,
    List<String> failedElements,
    List<UiNode> uiTree,
    int screenWidth,
    int screenHeight
) {
    public record UiNode(
        String text,
        @JsonProperty("resource_id") String resourceId,
        int x,
        int y,
        boolean clickable
    ) {}
}
