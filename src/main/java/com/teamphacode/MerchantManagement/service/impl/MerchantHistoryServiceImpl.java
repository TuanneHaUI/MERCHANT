package com.teamphacode.MerchantManagement.service.impl;

import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import com.teamphacode.MerchantManagement.repository.MerchantHistoryRepository;
import com.teamphacode.MerchantManagement.service.MerchantHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;


@Service
public class MerchantHistoryServiceImpl implements MerchantHistoryService {

    private static final String HASH_KEY = "MerchantHistoryCache";
    private static final Logger logger = LoggerFactory.getLogger(MerchantHistoryServiceImpl.class);
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MerchantHistoryRepository merchantHistoryRepository;


    @Override
    public List<MerchantHistory> handleGetMerchantHistoryByMerchantIdOrAccountNo(
            Specification<MerchantHistory> spec, Pageable pageable) {
        logger.info("\uD83D\uDD0D Bắt đầu tìm kiếm");
        String cacheKey = generateCacheKey(pageable);
        Object cachedData = redisTemplate.opsForHash().get(HASH_KEY, cacheKey);
        if (cachedData != null && cachedData instanceof List) {
            logger.info("✅ Tìm kiếm trong redis thành công!");
            return (List<MerchantHistory>) cachedData;
        }
        Page<MerchantHistory> pageResult = merchantHistoryRepository.findAll(spec, pageable);
        List<MerchantHistory> resultList = pageResult.getContent();

        redisTemplate.opsForHash().put(HASH_KEY, cacheKey, resultList);
        redisTemplate.expire(HASH_KEY, Duration.ofHours(1));
        logger.info("✅ Tìm kiếm thành công!");
        return resultList;
    }

    private String generateCacheKey(Pageable pageable) {
        String page = String.valueOf(pageable.getPageNumber());
        String size = String.valueOf(pageable.getPageSize());
        String sort = pageable.getSort().toString().replace(":", "-");

        return "::page=" + page + "::size=" + size + "::sort=" + sort;
    }
}
