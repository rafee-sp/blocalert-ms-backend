package com.blocalert.crypto.dto.response;

import com.blocalert.crypto.dto.internal.CryptoSummary;
import com.blocalert.crypto.dto.internal.Pagination;
import java.util.List;

public record CryptoResponse(
        List<CryptoSummary> cryptoList,
        Pagination pagination
) {}
