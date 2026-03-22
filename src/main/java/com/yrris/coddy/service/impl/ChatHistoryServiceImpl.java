package com.yrris.coddy.service.impl;

import com.yrris.coddy.model.entity.ChatHistory;
import com.yrris.coddy.model.vo.ChatHistoryVO;
import com.yrris.coddy.repository.ChatHistoryRepository;
import com.yrris.coddy.service.ChatHistoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
