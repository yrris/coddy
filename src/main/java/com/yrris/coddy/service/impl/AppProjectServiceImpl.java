package com.yrris.coddy.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yrris.coddy.ai.facade.AiCodeGeneratorFacade;
import com.yrris.coddy.ai.tool.ReactViteBuildService;
import com.yrris.coddy.config.AppDeployProperties;
import com.yrris.coddy.constant.AppConstant;
import com.yrris.coddy.exception.BusinessException;
import com.yrris.coddy.exception.ErrorCode;
import com.yrris.coddy.model.dto.app.*;
import com.yrris.coddy.model.entity.AppProject;
import com.yrris.coddy.model.entity.AppUser;
import com.yrris.coddy.model.enums.CodeGenTypeEnum;
import com.yrris.coddy.model.enums.UserRoleEnum;
import com.yrris.coddy.model.vo.AppVO;
import com.yrris.coddy.model.vo.LoginUserVO;
import com.yrris.coddy.model.vo.PageVO;
import com.yrris.coddy.repository.AppProjectRepository;
import com.yrris.coddy.repository.AppUserRepository;
import com.yrris.coddy.service.AppProjectService;
import com.yrris.coddy.service.ChatHistoryService;
import com.yrris.coddy.service.ScreenshotService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class AppProjectServiceImpl implements AppProjectService {

    private static final Logger log = LoggerFactory.getLogger(AppProjectServiceImpl.class);

    private static final int USER_LIST_MAX_PAGE_SIZE = 20;

    private static final int MAX_APP_NAME_LENGTH = 24;

    private static final String DEPLOY_KEY_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final Set<String> SORT_FIELD_ALLOW_LIST = Set.of(
            "id",
            "appName",
            "cover",
            "initPrompt",
            "codeGenType",
            "deployKey",
            "priority",
            "userId",
            "createTime",
            "updateTime",
            "deployedTime",
            "editTime"
    );

    private final AppProjectRepository appProjectRepository;

    private final AppUserRepository appUserRepository;

    private final AiCodeGeneratorFacade aiCodeGeneratorFacade;

    private final AppDeployProperties appDeployProperties;

    private final ChatHistoryService chatHistoryService;

    private final ReactViteBuildService reactViteBuildService;

    private final ObjectMapper objectMapper;

    private final ScreenshotService screenshotService;

    public AppProjectServiceImpl(
            AppProjectRepository appProjectRepository,
            AppUserRepository appUserRepository,
            AiCodeGeneratorFacade aiCodeGeneratorFacade,
            AppDeployProperties appDeployProperties,
            ChatHistoryService chatHistoryService,
            ReactViteBuildService reactViteBuildService,
            ObjectMapper objectMapper,
            ScreenshotService screenshotService
    ) {
        this.appProjectRepository = appProjectRepository;
        this.appUserRepository = appUserRepository;
        this.aiCodeGeneratorFacade = aiCodeGeneratorFacade;
        this.appDeployProperties = appDeployProperties;
        this.chatHistoryService = chatHistoryService;
        this.reactViteBuildService = reactViteBuildService;
        this.objectMapper = objectMapper;
        this.screenshotService = screenshotService;
    }

    @Override
    @Transactional
    public Long addApp(AppAddRequest request, LoginUserVO loginUser) {
        if (request == null || !StringUtils.hasText(request.getInitPrompt())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Initial prompt is required");
        }
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }

        String initPrompt = request.getInitPrompt().trim();
        CodeGenTypeEnum codeGenTypeEnum = resolveCodeGenType(request.getCodeGenType(), CodeGenTypeEnum.HTML_MULTI);

        AppProject appProject = new AppProject();
        appProject.setOwnerUserId(loginUser.getId());
        appProject.setInitPrompt(initPrompt);
        appProject.setAppName(buildDefaultAppName(initPrompt));
        appProject.setCodeGenType(codeGenTypeEnum.getValue());
        appProject.setPriority(AppConstant.DEFAULT_APP_PRIORITY);
        appProject.setIsDeleted(false);
        appProjectRepository.save(appProject);

        return appProject.getId();
    }

    @Override
    @Transactional
    public boolean updateApp(AppUpdateRequest request, LoginUserVO loginUser) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "App id is required");
        }
        if (!StringUtils.hasText(request.getAppName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "App name is required");
        }

        AppProject appProject = getExistingApp(request.getId());
        checkOwnerPermission(appProject, loginUser);

        appProject.setAppName(request.getAppName().trim());
        appProject.setEditTime(Instant.now());
        appProjectRepository.save(appProject);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteApp(Long appId, LoginUserVO loginUser) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "App id is required");
        }

        AppProject appProject = getExistingApp(appId);
        boolean isOwner = Objects.equals(appProject.getOwnerUserId(), loginUser == null ? null : loginUser.getId());
        if (!isOwner && !isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission to delete this app");
        }

        appProject.setIsDeleted(true);
        appProject.setEditTime(Instant.now());
        appProjectRepository.save(appProject);
        return true;
    }

    @Override
    public AppVO getAppVO(Long appId) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "App id is required");
        }
        AppProject appProject = getExistingApp(appId);
        return toAppVO(appProject, getUserVOMap(List.of(appProject)));
    }

    @Override
    public AppVO getAppVOForAdmin(Long appId) {
        return getAppVO(appId);
    }

    @Override
    public PageVO<AppVO> listMyApps(AppQueryRequest request, LoginUserVO loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }
        validateUserPageSize(request);

        PageRequest pageRequest = buildPageRequest(request, true);
        Specification<AppProject> specification = buildSpecification(request, loginUser.getId(), false);
        Page<AppProject> appPage = appProjectRepository.findAll(specification, pageRequest);

        return toPageVO(appPage, pageRequest);
    }

    @Override
    public PageVO<AppVO> listGoodApps(AppQueryRequest request) {
        validateUserPageSize(request);

        PageRequest pageRequest = buildPageRequest(request, true);
        Specification<AppProject> specification = buildSpecification(request, null, true);
        Page<AppProject> appPage = appProjectRepository.findAll(specification, pageRequest);

        return toPageVO(appPage, pageRequest);
    }

    @Override
    @Transactional
    public boolean deleteAppByAdmin(Long appId) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "App id is required");
        }

        AppProject appProject = getExistingApp(appId);
        appProject.setIsDeleted(true);
        appProject.setEditTime(Instant.now());
        appProjectRepository.save(appProject);
        return true;
    }

    @Override
    @Transactional
    public boolean updateAppByAdmin(AppAdminUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "App id is required");
        }

        AppProject appProject = getExistingApp(request.getId());

        if (StringUtils.hasText(request.getAppName())) {
            appProject.setAppName(request.getAppName().trim());
        }
        if (request.getCover() != null) {
            appProject.setCover(request.getCover().trim());
        }
        if (request.getPriority() != null) {
            appProject.setPriority(request.getPriority());
        }

        appProject.setEditTime(Instant.now());
        appProjectRepository.save(appProject);
        return true;
    }

    @Override
    public PageVO<AppVO> listAppsByAdmin(AppQueryRequest request) {
        PageRequest pageRequest = buildPageRequest(request, false);
        Specification<AppProject> specification = buildSpecification(request, null, false);
        Page<AppProject> appPage = appProjectRepository.findAll(specification, pageRequest);
        return toPageVO(appPage, pageRequest);
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, LoginUserVO loginUser) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "App id is required");
        }
        if (!StringUtils.hasText(message)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Message is required");
        }

        AppProject appProject = getExistingApp(appId);
        checkOwnerPermission(appProject, loginUser);

        CodeGenTypeEnum codeGenTypeEnum = resolveCodeGenType(appProject.getCodeGenType(), null);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Unsupported code generation type");
        }

        // Save user message to chat history
        chatHistoryService.saveChatMessage(appId, "USER", message);

        // Generate code and save AI response on completion
        Flux<String> aiFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appProject.getId());

        if (codeGenTypeEnum == CodeGenTypeEnum.REACT_VITE) {
            // REACT_VITE: stream contains typed JSON messages, extract AI text for history
            StringBuilder aiTextBuilder = new StringBuilder();
            return aiFlux
                    .doOnNext(chunk -> extractAiText(chunk, aiTextBuilder))
                    .doOnComplete(() -> {
                        chatHistoryService.saveChatMessage(appId, "ASSISTANT", aiTextBuilder.toString());
                        // Trigger build after generation completes
                        try {
                            reactViteBuildService.buildProject(appId);
                        } catch (Exception e) {
                            log.warn("React build failed for appId={}: {}", appId, e.getMessage());
                        }
                    });
        }

        // HTML modes: collect raw stream for history
        StringBuilder aiResponseBuilder = new StringBuilder();
        return aiFlux
                .doOnNext(aiResponseBuilder::append)
                .doOnComplete(() -> chatHistoryService.saveChatMessage(appId, "ASSISTANT", aiResponseBuilder.toString()));
    }

    private void extractAiText(String jsonChunk, StringBuilder builder) {
        try {
            JsonNode node = objectMapper.readTree(jsonChunk);
            String type = node.path("type").asText("");
            if ("AI_RESPONSE".equals(type)) {
                builder.append(node.path("data").asText(""));
            }
        } catch (Exception e) {
            // Not valid JSON, ignore
        }
    }

    @Override
    @Transactional
    public String deployApp(Long appId, LoginUserVO loginUser) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "App id is required");
        }
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }

        AppProject appProject = getExistingApp(appId);
        checkOwnerPermission(appProject, loginUser);

        CodeGenTypeEnum codeGenTypeEnum = resolveCodeGenType(appProject.getCodeGenType(), null);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Unsupported code generation type");
        }

        String sourceDirName = buildPreviewKey(codeGenTypeEnum, appId);
        Path sourceDir;
        if (codeGenTypeEnum == CodeGenTypeEnum.REACT_VITE) {
            // For React projects, deploy only the built dist/ directory
            Path projectDir = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, sourceDirName);
            if (!Files.exists(projectDir) || !Files.isDirectory(projectDir)) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "Generated code does not exist, generate code first");
            }
            sourceDir = projectDir.resolve("dist");
            if (!Files.exists(sourceDir)) {
                // Build if dist/ not found
                reactViteBuildService.buildProject(appId);
                sourceDir = projectDir.resolve("dist");
            }
        } else {
            sourceDir = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, sourceDirName);
        }
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Generated code does not exist, generate code first");
        }

        String deployKey = appProject.getDeployKey();
        if (!StringUtils.hasText(deployKey)) {
            deployKey = generateUniqueDeployKey();
        }

        Path deployDir = Paths.get(AppConstant.CODE_DEPLOY_ROOT_DIR, deployKey);
        try {
            recreateDirectory(deployDir);
            copyDirectory(sourceDir, deployDir);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Deploy failed: " + e.getMessage());
        }

        appProject.setDeployKey(deployKey);
        appProject.setDeployedTime(Instant.now());
        appProject.setEditTime(Instant.now());
        appProjectRepository.save(appProject);

        String deployUrl = normalizeHost(appDeployProperties.getHost()) + "/api/deployed/" + deployKey + "/";
        asyncGenerateScreenshot(appProject.getId(), deployUrl);
        return deployUrl;
    }

    @Override
    public String generateScreenshot(Long appId, LoginUserVO loginUser) {
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "App id is required");
        }

        AppProject appProject = getExistingApp(appId);
        checkOwnerPermission(appProject, loginUser);

        if (!StringUtils.hasText(appProject.getDeployKey())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "App must be deployed before taking a screenshot");
        }

        String deployUrl = normalizeHost(appDeployProperties.getHost())
                + "/api/deployed/" + appProject.getDeployKey() + "/";
        screenshotService.takeScreenshot(deployUrl, appId);

        String coverUrl = "/api/app/screenshot/image/" + appId + "?t=" + System.currentTimeMillis();
        appProject.setCover(coverUrl);
        appProject.setEditTime(Instant.now());
        appProjectRepository.save(appProject);

        return coverUrl;
    }

    @Async
    public void asyncGenerateScreenshot(Long appId, String deployUrl) {
        try {
            screenshotService.takeScreenshot(deployUrl, appId);
            String coverUrl = "/api/app/screenshot/image/" + appId + "?t=" + System.currentTimeMillis();
            AppProject appProject = appProjectRepository.findByIdAndIsDeletedFalse(appId).orElse(null);
            if (appProject != null) {
                appProject.setCover(coverUrl);
                appProjectRepository.save(appProject);
            }
        } catch (Exception e) {
            log.warn("Async screenshot failed for appId={}: {}", appId, e.getMessage());
        }
    }

    private String buildDefaultAppName(String initPrompt) {
        String sanitized = initPrompt.replaceAll("\\s+", " ").trim();
        if (sanitized.length() <= MAX_APP_NAME_LENGTH) {
            return sanitized;
        }
        return sanitized.substring(0, MAX_APP_NAME_LENGTH);
    }

    private AppProject getExistingApp(Long appId) {
        return appProjectRepository.findByIdAndIsDeletedFalse(appId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "App not found"));
    }

    private void checkOwnerPermission(AppProject appProject, LoginUserVO loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Login required");
        }
        if (!Objects.equals(appProject.getOwnerUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission to operate this app");
        }
    }

    private boolean isAdmin(LoginUserVO loginUser) {
        return loginUser != null && UserRoleEnum.isAdmin(loginUser.getUserRole());
    }

    private void validateUserPageSize(AppQueryRequest request) {
        long pageSize = request == null ? 10L : request.getPageSize();
        if (pageSize > USER_LIST_MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Page size must be <= 20");
        }
    }

    private PageRequest buildPageRequest(AppQueryRequest request, boolean withSafeLimit) {
        long pageNum = request == null ? 1L : Math.max(1L, request.getPageNum());
        long pageSize = request == null ? 10L : Math.max(1L, request.getPageSize());

        if (withSafeLimit) {
            pageSize = Math.min(pageSize, USER_LIST_MAX_PAGE_SIZE);
        } else {
            pageSize = Math.min(pageSize, 200L);
        }

        Sort sort = buildSort(request);
        return PageRequest.of((int) (pageNum - 1), (int) pageSize, sort);
    }

    private Sort buildSort(AppQueryRequest request) {
        if (request == null || !StringUtils.hasText(request.getSortField())) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String requestedField = request.getSortField().trim();
        if (!SORT_FIELD_ALLOW_LIST.contains(requestedField)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String entityField = switch (requestedField) {
            case "appName" -> "appName";
            case "cover" -> "cover";
            case "initPrompt" -> "initPrompt";
            case "codeGenType" -> "codeGenType";
            case "deployKey" -> "deployKey";
            case "priority" -> "priority";
            case "userId" -> "ownerUserId";
            case "createTime" -> "createdAt";
            case "updateTime" -> "updatedAt";
            case "deployedTime" -> "deployedTime";
            case "editTime" -> "editTime";
            default -> "id";
        };

        String rawOrder = request.getSortOrder();
        Sort.Direction direction = "asc".equalsIgnoreCase(rawOrder)
                || "ascend".equalsIgnoreCase(rawOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, entityField);
    }

    private Specification<AppProject> buildSpecification(AppQueryRequest request, Long fixedUserId, boolean onlyGoodApps) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (fixedUserId != null) {
                predicates.add(cb.equal(root.get("ownerUserId"), fixedUserId));
            }

            if (onlyGoodApps) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priority"), AppConstant.GOOD_APP_PRIORITY));
            }

            if (request != null) {
                if (request.getId() != null) {
                    predicates.add(cb.equal(root.get("id"), request.getId()));
                }
                if (StringUtils.hasText(request.getAppName())) {
                    predicates.add(cb.like(
                            cb.lower(root.get("appName")),
                            "%" + request.getAppName().trim().toLowerCase() + "%"
                    ));
                }
                if (StringUtils.hasText(request.getCover())) {
                    predicates.add(cb.like(
                            cb.lower(root.get("cover")),
                            "%" + request.getCover().trim().toLowerCase() + "%"
                    ));
                }
                if (StringUtils.hasText(request.getInitPrompt())) {
                    predicates.add(cb.like(
                            cb.lower(root.get("initPrompt")),
                            "%" + request.getInitPrompt().trim().toLowerCase() + "%"
                    ));
                }
                if (StringUtils.hasText(request.getCodeGenType())) {
                    predicates.add(cb.equal(root.get("codeGenType"), request.getCodeGenType().trim()));
                }
                if (StringUtils.hasText(request.getDeployKey())) {
                    predicates.add(cb.equal(root.get("deployKey"), request.getDeployKey().trim()));
                }
                if (request.getPriority() != null) {
                    predicates.add(cb.equal(root.get("priority"), request.getPriority()));
                }
                if (fixedUserId == null && request.getUserId() != null) {
                    predicates.add(cb.equal(root.get("ownerUserId"), request.getUserId()));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private PageVO<AppVO> toPageVO(Page<AppProject> appPage, PageRequest pageRequest) {
        List<AppProject> appProjects = appPage.getContent();
        Map<Long, LoginUserVO> userVOMap = getUserVOMap(appProjects);

        List<AppVO> records = appProjects.stream()
                .map(app -> toAppVO(app, userVOMap))
                .collect(Collectors.toList());

        PageVO<AppVO> pageVO = new PageVO<>();
        pageVO.setPageNum(pageRequest.getPageNumber() + 1L);
        pageVO.setPageSize(pageRequest.getPageSize());
        pageVO.setTotalRow(appPage.getTotalElements());
        pageVO.setRecords(records);
        return pageVO;
    }

    private Map<Long, LoginUserVO> getUserVOMap(List<AppProject> appProjects) {
        Set<Long> userIds = appProjects.stream()
                .map(AppProject::getOwnerUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<AppUser> users = appUserRepository.findAllByIdInAndIsDeletedFalse(userIds);
        Map<Long, LoginUserVO> result = new HashMap<>(users.size());
        for (AppUser user : users) {
            LoginUserVO userVO = new LoginUserVO();
            userVO.setId(user.getId());
            userVO.setEmail(user.getEmail());
            userVO.setDisplayName(user.getDisplayName());
            userVO.setAvatarUrl(user.getAvatarUrl());
            userVO.setAuthProvider(user.getAuthProvider());
            userVO.setUserRole(user.getUserRole());
            result.put(user.getId(), userVO);
        }
        return result;
    }

    private AppVO toAppVO(AppProject appProject, Map<Long, LoginUserVO> userVOMap) {
        AppVO appVO = new AppVO();
        appVO.setId(appProject.getId());
        appVO.setAppName(appProject.getAppName());
        appVO.setCover(appProject.getCover());
        appVO.setInitPrompt(appProject.getInitPrompt());
        appVO.setCodeGenType(appProject.getCodeGenType());
        appVO.setDeployKey(appProject.getDeployKey());
        appVO.setDeployedTime(appProject.getDeployedTime());
        appVO.setPriority(appProject.getPriority());
        appVO.setUserId(appProject.getOwnerUserId());
        appVO.setCreateTime(appProject.getCreatedAt());
        appVO.setUpdateTime(appProject.getUpdatedAt());
        appVO.setUser(userVOMap.get(appProject.getOwnerUserId()));

        CodeGenTypeEnum codeGenTypeEnum = resolveCodeGenType(appProject.getCodeGenType(), null);
        if (codeGenTypeEnum != null) {
            appVO.setPreviewKey(buildPreviewKey(codeGenTypeEnum, appProject.getId()));
        }

        return appVO;
    }

    private CodeGenTypeEnum resolveCodeGenType(String rawType, CodeGenTypeEnum defaultType) {
        CodeGenTypeEnum parsed = CodeGenTypeEnum.fromValue(rawType);
        if (parsed != null) {
            return parsed;
        }
        return defaultType;
    }

    private String buildPreviewKey(CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        return codeGenTypeEnum.getValue().toLowerCase(Locale.ROOT) + "_" + appId;
    }

    private String generateUniqueDeployKey() {
        for (int i = 0; i < 20; i++) {
            String candidate = randomDeployKey(6);
            if (!appProjectRepository.existsByDeployKeyAndIsDeletedFalse(candidate)) {
                return candidate;
            }
        }
        throw new BusinessException(ErrorCode.CONFLICT, "Failed to allocate deploy key, please retry");
    }

    private String randomDeployKey(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(DEPLOY_KEY_CHARS.length());
            builder.append(DEPLOY_KEY_CHARS.charAt(index));
        }
        return builder.toString();
    }

    private void recreateDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            deleteDirectory(path);
        }
        Files.createDirectories(path);
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(dir);
                Path destinationDir = target.resolve(relative);
                Files.createDirectories(destinationDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(file);
                Path destinationFile = target.resolve(relative);
                Files.copy(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void deleteDirectory(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private String normalizeHost(String rawHost) {
        if (!StringUtils.hasText(rawHost)) {
            return "http://localhost";
        }
        String normalized = rawHost.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
