package com.yrris.coddy.service;

import com.yrris.coddy.model.entity.ChatHistory;
import com.yrris.coddy.model.vo.ChatHistoryVO;

import java.util.List;

public interface ChatHistoryService {

    ChatHistory saveChatMessage(Long projectId, String senderType, String content);

    List<ChatHistoryVO> listByProject(Long projectId, Long cursorId, int pageSize);

    long countByProject(Long projectId);
}
