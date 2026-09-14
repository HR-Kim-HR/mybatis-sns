package net.likelion.bebc25.sns.dto;

public record LoginRequest(
        String email,
        String password
) {}
