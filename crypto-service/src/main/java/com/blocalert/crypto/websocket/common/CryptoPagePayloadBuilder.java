package com.blocalert.crypto.websocket.common;

import com.blocalert.crypto.dto.internal.CryptoSummary;
import com.blocalert.crypto.dto.internal.PageDTO;
import com.blocalert.crypto.dto.internal.Pagination;
import com.blocalert.crypto.dto.response.CryptoResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CryptoPagePayloadBuilder {

    public CryptoResponse buildPage(PageDTO pageDTO, List<CryptoSummary> cryptoSummaryList) {

        int totalPages = Math.max(1, (int) Math.ceil((double) cryptoSummaryList.size() / pageDTO.size()));
        int page = Math.min(pageDTO.page(), totalPages);

        int fromIndex = (page - 1) * pageDTO.size();
        int toIndex = Math.min(fromIndex + pageDTO.size(), cryptoSummaryList.size());
        List<CryptoSummary> paginated = cryptoSummaryList.subList(fromIndex, toIndex);

        return new CryptoResponse(paginated, new Pagination(page, pageDTO.size(), totalPages));
    }
}
