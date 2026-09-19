package com.blocalert.crypto.service.impl;

import com.blocalert.crypto.dto.enums.SortMetric;
import com.blocalert.dto.CryptoDetails;
import com.blocalert.crypto.dto.internal.CryptoSummary;
import com.blocalert.crypto.dto.internal.MarketStatsData;
import com.blocalert.crypto.events.event.CryptoDataUpdatedEvent;
import com.blocalert.crypto.events.event.CryptoPriceEvent;
import com.blocalert.crypto.events.event.MarketStatsUpdatedEvent;
import com.blocalert.crypto.events.publisher.CryptoPricePublisher;
import com.blocalert.crypto.exception.ResourceNotFoundException;
import com.blocalert.crypto.repository.CryptoCacheRepository;
import com.blocalert.crypto.service.CoingeckoService;
import com.blocalert.crypto.service.CryptoService;
import com.blocalert.dto.CryptoPrice;
import com.blocalert.dto.HistorySummary;
import com.blocalert.dto.TopMovers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.blocalert.crypto.utils.Utils.nullSafe;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoServiceImpl implements CryptoService {

    private final ApplicationEventPublisher eventPublisher;
    private final CoingeckoService coingeckoService;
    private final CryptoCacheRepository cryptoCacheRepository;
    private final CryptoPricePublisher cryptoPricePublisher;

    @Override
    public void fetchAndCacheCryptoData() {

        log.info("fetchAndCacheCryptoData called");

        List<CryptoDetails> cryptoList = coingeckoService.fetchCryptoMarketData();
        List<CryptoSummary> cryptoSummaryList = CryptoSummary.mapToSummary(cryptoList);

        cryptoCacheRepository.saveCryptoDetails(cryptoList);
        cryptoCacheRepository.saveCryptoSummary(cryptoSummaryList);

        eventPublisher.publishEvent(new CryptoDataUpdatedEvent(
                                                buildCryptoBroadcastEvent(cryptoList),
                                                cryptoSummaryList
                                        ));

        List<CryptoPrice> cryptoPriceList =  cryptoList.stream()
                .map(CryptoServiceImpl::mapToCryptoPrice)
                .toList();

        cryptoPricePublisher.publishEvent(new CryptoPriceEvent(cryptoPriceList));
        log.info("Cached {} cryptos and published events", cryptoList.size());
    }

    @Override
    public void fetchAndCacheMarketStats() {

        log.info("fetchAndCacheMarketStats called");

        MarketStatsData marketStats = coingeckoService.fetchMarketStats();
        cryptoCacheRepository.saveMarketStats(marketStats);

        eventPublisher.publishEvent(new MarketStatsUpdatedEvent(marketStats));
        log.info("Cached market stats and published events");
    }

    @Override
    public List<CryptoSummary> getCachedCryptoData() {

        log.info("getCachedCryptoData called");

        List<CryptoSummary> cachedCryptoList = cryptoCacheRepository.findCryptoSummaryList();

        if (cachedCryptoList != null && !cachedCryptoList.isEmpty())
            return cachedCryptoList;

        log.warn("CryptoMarketLite is null in cache - refetching");

        fetchAndCacheCryptoData();

        cachedCryptoList = cryptoCacheRepository.findCryptoSummaryList();

        if (cachedCryptoList == null || cachedCryptoList.isEmpty())
            throw new ResourceNotFoundException("Cached crypto list is empty");

        return cachedCryptoList;
    }

    @Override
    public MarketStatsData getCachedMarketStats() {

        log.info("getCachedMarketStats called");

        MarketStatsData cachedMarketStats = cryptoCacheRepository.findMarketStats();

        if (cachedMarketStats != null)
            return cachedMarketStats;

        log.warn("MarketStatsData is null in cache - refetching");
        fetchAndCacheMarketStats();

        cachedMarketStats = cryptoCacheRepository.findMarketStats();

        if (cachedMarketStats == null)
            throw new ResourceNotFoundException("Cached crypto market data is empty");

        return cachedMarketStats;
    }

    @Override
    public CryptoDetails getCryptoDetail(String cryptoId) {
        log.info("getCryptoDetail called for {}",cryptoId);
        return cryptoCacheRepository.findById(cryptoId);
    }

    @Override
    public List<CryptoSummary> searchCrypto(String searchTermRaw) {

        log.info("searchCrypto called with term: {}", searchTermRaw);

        if (!StringUtils.hasText(searchTermRaw))
            return Collections.emptyList();

        String searchTerm = searchTermRaw.toLowerCase();

        return getCachedCryptoData()
                .stream()
                .filter(crypto -> {
                    String name = crypto.name();
                    String symbol = crypto.symbol();
                    return (name != null && name.toLowerCase().contains(searchTerm)) ||
                            (symbol != null && symbol.toLowerCase().contains(searchTerm));
                })
                .toList();
    }

    @Override
    public Map<String, Object> getChartData(String cryptoId, String timeframe) {

        log.info("getChartData called for {}", timeframe);
        return coingeckoService.getCryptoChartData(cryptoId, getDays(timeframe));
    }

    @Override
    public Map<String, BigDecimal> getCryptoPrice(String cryptoId) {
        CryptoDetails cryptoData = getCryptoDetail(cryptoId);
        return Map.of("price", cryptoData.current_price());
    }

    @Override
    public Map<String, CryptoPrice> getCryptosDetailByIds(Set<String> cryptoIds) {

        return cryptoCacheRepository
                .findByIds(cryptoIds)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> mapToCryptoPrice(entry.getValue())
                ));

    }

    @Override
    public CryptoDetails validateAndGetCrypto(String cryptoQuery) {

        log.info("validateAndGetCrypto called for {}", cryptoQuery);

        String normalized = cryptoQuery.trim().toLowerCase();
        List<CryptoDetails> allCoins = cryptoCacheRepository.getAllCryptoDetails();

        Optional<CryptoDetails> match = allCoins.stream()
                .filter(c -> c.symbol().equalsIgnoreCase(normalized))
                .findFirst();

        if (match.isEmpty()) {
            match = allCoins.stream()
                    .filter(c -> c.name().equalsIgnoreCase(normalized))
                    .findFirst();
        }

        return Optional.of(match)
                .get().orElse(null);
    }


    @Override
    @SuppressWarnings("unchecked")
    public HistorySummary getHistorySummary(String cryptoQuery, String timeframe) {

        CryptoDetails cryptoDetails = validateAndGetCrypto(cryptoQuery);

        if(cryptoDetails == null) return null;

        String cryptoId = cryptoDetails.id();

        Map<String, Object> chartData = getChartData(cryptoId, getDays(timeframe));
        List<List<Number>> prices = (List<List<Number>>) chartData.get("prices");

        if(prices == null) return null;

        double first = prices.getFirst().get(1).doubleValue();
        double last = prices.getLast().get(1).doubleValue();
        double high = prices.stream().mapToDouble(p -> p.get(1).doubleValue()).max().orElse(last);
        double low = prices.stream().mapToDouble(p -> p.get(1).doubleValue()).min().orElse(last);
        double percentChange = ((last - first) / first) * 100;

        return new HistorySummary(timeframe, first, low, high, low, percentChange);
    }

    @Override
    public List<TopMovers> getTopMovers(String metic, boolean descending, Double minAbsoluteChange, int limit) {

        SortMetric sortMetric = SortMetric.of(metic);

        List<CryptoSummary> cryptoList = getCachedCryptoData();

        Comparator<CryptoSummary> comparator = switch (sortMetric) {
            case MARKET_CAP -> Comparator.comparing(c ->nullSafe(c.market_cap()));
            case PRICE_CHANGE_24H -> Comparator.comparing(c -> nullSafe(c.price_change_percentage_24h()));
            case MARKET_CAP_RANK -> Comparator.comparing(CryptoSummary::market_cap_rank,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };

        return cryptoList.stream()
                .filter(c -> minAbsoluteChange == null ||
                        (c.price_change_percentage_24h() != null &&
                                Math.abs(c.price_change_percentage_24h()) >= minAbsoluteChange))
                .sorted(descending ? comparator.reversed() : comparator)
                .limit(limit)
                .map(this::mapToTopMovers)
                .toList();

    }

    private TopMovers mapToTopMovers(CryptoSummary crypto) {
        return new TopMovers(crypto.id(), crypto.name(), crypto.symbol(), crypto.current_price(), crypto.price_change_percentage_24h());
    }

    private Map<String, CryptoDetails> buildCryptoBroadcastEvent(List<CryptoDetails> cryptoDetails) {

        return  cryptoDetails
                .stream()
                .collect(Collectors.toMap(CryptoDetails::id, Function.identity()));
    }

    public static CryptoPrice mapToCryptoPrice(CryptoDetails cryptoDetails) {

        return new CryptoPrice(
                cryptoDetails.id(),
                cryptoDetails.symbol(),
                cryptoDetails.name(),
                cryptoDetails.image(),
                cryptoDetails.current_price()
        );
    }

    private String getDays(String timeframe) {

        return switch (timeframe) {
            case "7D" -> "7";
            case "1M" -> "30";
            case "6M" -> "180";
            case "1Y" -> "365";
            default -> "1";
        };
    }
}