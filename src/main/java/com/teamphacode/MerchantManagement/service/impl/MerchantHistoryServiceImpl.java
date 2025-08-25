package com.teamphacode.MerchantManagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamphacode.MerchantManagement.domain.MerchantHistory;
import com.teamphacode.MerchantManagement.domain.dto.response.ResultPaginationDTO;
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
    public ResultPaginationDTO handleGetMerchantHistoryByMerchantIdOrAccountNo(
            Specification<MerchantHistory> spec, Pageable pageable) {

        logger.info("==>>> Bắt đầu tìm kiếm<<<==");

        String cacheKey = generateCacheKey(pageable, spec);

        Object cachedObj = redisTemplate.opsForHash().get(HASH_KEY, cacheKey);
        if (cachedObj != null) {
            try {
                ResultPaginationDTO cachedData = new ObjectMapper()
                        .convertValue(cachedObj, ResultPaginationDTO.class);
                logger.info("✅ Tìm kiếm trong Redis thành công!");
                return cachedData;
            } catch (IllegalArgumentException e) {
                logger.warn("⚠️ Lỗi chuyển đổi cachedData sang ResultPaginationDTO, bỏ qua cache", e);
            }
        }

        Page<MerchantHistory> pageResult = merchantHistoryRepository.findAll(spec, pageable);

        ResultPaginationDTO dto = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1); // Pageable index bắt đầu từ 0
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageResult.getTotalPages());
        meta.setTotal(pageResult.getTotalElements());

        dto.setMeta(meta);
        dto.setResult(pageResult.getContent());

        redisTemplate.opsForHash().put(HASH_KEY, cacheKey, dto);
        redisTemplate.expire(HASH_KEY, Duration.ofHours(1));

        logger.info("✅ Tìm kiếm thành công!");
        return dto;
    }


    private String generateCacheKey(Pageable pageable, Specification<MerchantHistory> spec) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("page:").append(pageable.getPageNumber())
                .append(":size:").append(pageable.getPageSize());

        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            keyBuilder.append(":sort:");
            pageable.getSort().forEach(order -> {
                keyBuilder.append(order.getProperty())
                        .append(",")
                        .append(order.getDirection())
                        .append(";");
            });
        }

        if (spec != null) {
            keyBuilder.append(":filter:").append(spec.toString());
        }

        return keyBuilder.toString();
    }

//    private String generateCacheKey(Pageable pageable) {
//        String page = String.valueOf(pageable.getPageNumber());
//        String size = String.valueOf(pageable.getPageSize());
//        String sort = pageable.getSort().toString().replace(":", "-");
//
//        return "::page=" + page + "::size=" + size + "::sort=" + sort;
//    }
}
