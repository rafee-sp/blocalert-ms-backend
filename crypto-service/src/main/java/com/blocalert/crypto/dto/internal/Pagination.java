package com.blocalert.crypto.dto.internal;

public record Pagination(
        int page,
        int size,
        int totalPages
) {
}
