package com.yrris.coddy.repository;

import com.yrris.coddy.model.entity.ChatHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long>, JpaSpecificationExecutor<ChatHistory> {

    List<ChatHistory> findByProjectIdAndIdLessThanOrderByIdDesc(Long projectId, Long cursorId, Pageable pageable);

    List<ChatHistory> findByProjectIdOrderByIdDesc(Long projectId, Pageable pageable);

    long countByProjectId(Long projectId);
}
