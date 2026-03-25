package com.yrris.coddy.service.impl;

import com.yrris.coddy.model.dto.chat.ChatHistoryQueryRequest;
import com.yrris.coddy.model.entity.ChatHistory;
import com.yrris.coddy.model.vo.ChatHistoryVO;
import com.yrris.coddy.model.vo.PageVO;
import com.yrris.coddy.repository.ChatHistoryRepository;
import com.yrris.coddy.service.ChatHistoryService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ChatHistoryRepository chatHistoryRepository;

    public ChatHistoryServiceImpl(ChatHistoryRepository chatHistoryRepository) {
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @Override
    @Transactional
    public ChatHistory saveChatMessage(Long projectId, String senderType, String content) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setProjectId(projectId);
        chatHistory.setSenderType(senderType);
        chatHistory.setContent(content);
        chatHistory.setMessageStatus("SUCCESS");
        return chatHistoryRepository.save(chatHistory);
    }

    @Override
    public List<ChatHistoryVO> listByProject(Long projectId, Long cursorId, int pageSize) {
        int safePageSize = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));
        PageRequest pageRequest = PageRequest.of(0, safePageSize);

        List<ChatHistory> entities;
        if (cursorId != null && cursorId > 0) {
            entities = chatHistoryRepository.findByProjectIdAndIdLessThanOrderByIdDesc(projectId, cursorId, pageRequest);
        } else {
            entities = chatHistoryRepository.findByProjectIdOrderByIdDesc(projectId, pageRequest);
        }

        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public long countByProject(Long projectId) {
        return chatHistoryRepository.countByProjectId(projectId);
    }

    private static final Set<String> SORT_FIELD_ALLOW_LIST = Set.of("id", "createdAt", "projectId");

    @Override
    public PageVO<ChatHistoryVO> listByAdmin(ChatHistoryQueryRequest request) {
        if (request == null) {
            request = new ChatHistoryQueryRequest();
        }

        long pageNum = Math.max(1, request.getPageNum());
        long pageSize = Math.max(1, Math.min(request.getPageSize(), MAX_PAGE_SIZE));
        Sort sort = buildAdminSort(request);
        PageRequest pageRequest = PageRequest.of((int) (pageNum - 1), (int) pageSize, sort);

        Specification<ChatHistory> specification = buildAdminSpecification(request);
        Page<ChatHistory> page = chatHistoryRepository.findAll(specification, pageRequest);

        PageVO<ChatHistoryVO> pageVO = new PageVO<>();
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalRow(page.getTotalElements());
        pageVO.setRecords(page.getContent().stream().map(this::toVO).collect(Collectors.toList()));
        return pageVO;
    }

    @Override
    @Transactional
    public boolean deleteByAdmin(Long id) {
        chatHistoryRepository.deleteById(id);
        return true;
    }

    private Specification<ChatHistory> buildAdminSpecification(ChatHistoryQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getProjectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), request.getProjectId()));
            }
            if (StringUtils.hasText(request.getSenderType())) {
                predicates.add(cb.equal(root.get("senderType"), request.getSenderType().trim()));
            }
            if (StringUtils.hasText(request.getContent())) {
                predicates.add(cb.like(
                        cb.lower(root.get("content")),
                        "%" + request.getContent().trim().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildAdminSort(ChatHistoryQueryRequest request) {
        if (request == null || !StringUtils.hasText(request.getSortField())) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String requestedField = request.getSortField().trim();
        if (!SORT_FIELD_ALLOW_LIST.contains(requestedField)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortOrder())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, requestedField);
    }

    private ChatHistoryVO toVO(ChatHistory entity) {
        ChatHistoryVO vo = new ChatHistoryVO();
        vo.setId(entity.getId());
        vo.setProjectId(entity.getProjectId());
        vo.setSenderType(entity.getSenderType());
        vo.setContent(entity.getContent());
        vo.setMessageStatus(entity.getMessageStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
