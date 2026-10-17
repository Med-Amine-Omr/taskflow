package com.taskflow.api.dto;

import java.util.List;

public record MeResponse(String id, String username, String name, String email, List<String> roles) {
}
