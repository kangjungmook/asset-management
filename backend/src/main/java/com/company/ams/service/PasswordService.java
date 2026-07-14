package com.company.ams.service;

import com.company.ams.common.exception.BusinessException;
import com.company.ams.common.exception.NotFoundException;
import com.company.ams.dto.PageResponse;
import com.company.ams.dto.PasswordRejectRequest;
import com.company.ams.dto.PasswordRequest;
import com.company.ams.dto.PasswordSummaryResponse;
import com.company.ams.entity.PasswordEntry;
import com.company.ams.entity.User;
import com.company.ams.mapper.PasswordMapper;
import com.company.ams.mapper.UserMapper;
import com.company.ams.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private static final int EXPIRING_WITHIN_DAYS = 30;

    private final PasswordMapper passwordMapper;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public PageResponse<PasswordEntry> findAll(Integer deptId, Integer userId, String keyword,
                                                Integer typeId, String status, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;

        List<PasswordEntry> content = passwordMapper.findAll(deptId, userId, keyword, typeId, status, offset, safeSize);
        long total = passwordMapper.countAll(deptId, userId, keyword, typeId, status);
        return new PageResponse<>(content, safePage, safeSize, total);
    }

    public PasswordEntry findById(Integer pwId) {
        PasswordEntry entry = passwordMapper.findById(pwId);
        if (entry == null) {
            throw new NotFoundException("패스워드 관리대장 항목을 찾을 수 없습니다.");
        }
        return entry;
    }

    public List<PasswordEntry> findExpiring(Integer deptId, Integer userId) {
        return passwordMapper.findExpiring(deptId, userId, EXPIRING_WITHIN_DAYS);
    }

    public PasswordSummaryResponse getSummary(Integer deptId, Integer userId) {
        Map<String, Object> row = passwordMapper.countByExpiryStatus(deptId, userId);
        return new PasswordSummaryResponse(
                toLong(row.get("expired")),
                toLong(row.get("expiringSoon")),
                toLong(row.get("normal"))
        );
    }

    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    @Transactional
    public PasswordEntry create(PasswordRequest request, AuthPrincipal actor) {
        PasswordEntry entry = new PasswordEntry();
        applyRequest(entry, request);
        entry.setRequesterName(hasText(request.getRequesterName()) ? request.getRequesterName() : actor.getName());
        entry.setLastModifier(actor.getName());
        passwordMapper.insert(entry);
        auditLogService.log(actor.getUserId(), "PASSWORD_ENTRY_CREATE", "pw:" + entry.getPwId(),
                "패스워드 대장 등록: accountId=" + entry.getAccountId());
        notifyApprovers(entry, actor);
        return entry;
    }

    @Transactional
    public PasswordEntry approve(Integer pwId, AuthPrincipal actor) {
        PasswordEntry entry = findById(pwId);
        if (!"PENDING".equals(entry.getStatus())) {
            throw new BusinessException("이미 처리된 항목입니다.");
        }
        LocalDateTime approvedAt = LocalDateTime.now();
        passwordMapper.approve(pwId, actor.getName(), approvedAt);
        auditLogService.log(actor.getUserId(), "PASSWORD_ENTRY_APPROVE", "pw:" + pwId,
                "패스워드 대장 승인: accountId=" + entry.getAccountId());
        notificationService.notify(entry.getUserId(), "PASSWORD_APPROVED",
                "패스워드 변경 승인 완료",
                actor.getName() + "님이 '" + entry.getAccountId() + "' 계정 비밀번호 변경을 승인했습니다.",
                "/manage/passwords");
        entry.setApproverName(actor.getName());
        entry.setApprovedAt(approvedAt);
        entry.setStatus("APPROVED");
        return entry;
    }

    @Transactional
    public PasswordEntry reject(Integer pwId, PasswordRejectRequest request, AuthPrincipal actor) {
        PasswordEntry entry = findById(pwId);
        if (!"PENDING".equals(entry.getStatus())) {
            throw new BusinessException("이미 처리된 항목입니다.");
        }
        LocalDateTime rejectedAt = LocalDateTime.now();
        passwordMapper.reject(pwId, actor.getName(), rejectedAt, request.getReason());
        auditLogService.log(actor.getUserId(), "PASSWORD_ENTRY_REJECT", "pw:" + pwId,
                "패스워드 대장 반려: accountId=" + entry.getAccountId() + ", 사유=" + request.getReason());
        notificationService.notify(entry.getUserId(), "PASSWORD_REJECTED",
                "패스워드 변경 반려",
                actor.getName() + "님이 '" + entry.getAccountId() + "' 계정 비밀번호 변경을 반려했습니다: " + request.getReason(),
                "/manage/passwords");
        entry.setRejectedBy(actor.getName());
        entry.setRejectedAt(rejectedAt);
        entry.setRejectReason(request.getReason());
        entry.setStatus("REJECTED");
        return entry;
    }

    private void notifyApprovers(PasswordEntry entry, AuthPrincipal actor) {
        User owner = userMapper.findById(entry.getUserId());
        Integer deptId = owner != null ? owner.getDeptId() : null;
        List<Integer> recipients = userMapper.findApprovalRecipientUserIds(deptId, actor.getUserId());
        notificationService.notifyMany(recipients, "PASSWORD_APPROVAL_REQUEST",
                "패스워드 승인 요청",
                actor.getName() + "님이 '" + entry.getAccountId() + "' 계정 비밀번호 변경 승인을 요청했습니다.",
                "/manage/passwords");
    }

    @Transactional
    public PasswordEntry update(Integer pwId, PasswordRequest request, AuthPrincipal actor) {
        PasswordEntry entry = findById(pwId);
        boolean wasRejected = "REJECTED".equals(entry.getStatus());
        applyRequest(entry, request);
        entry.setLastModifier(actor.getName());
        passwordMapper.update(entry);
        if (wasRejected) {
            passwordMapper.resetToPending(pwId);
            entry.setStatus("PENDING");
            entry.setRejectedBy(null);
            entry.setRejectedAt(null);
            entry.setRejectReason(null);
            notifyApprovers(entry, actor);
        }
        auditLogService.log(actor.getUserId(), "PASSWORD_ENTRY_UPDATE", "pw:" + pwId,
                "패스워드 대장 수정: accountId=" + entry.getAccountId());
        return entry;
    }

    @Transactional
    public void delete(Integer pwId, AuthPrincipal actor) {
        PasswordEntry entry = findById(pwId);
        passwordMapper.deleteById(pwId);
        auditLogService.log(actor.getUserId(), "PASSWORD_ENTRY_DELETE", "pw:" + pwId,
                "패스워드 대장 삭제: accountId=" + entry.getAccountId());
    }

    private void applyRequest(PasswordEntry entry, PasswordRequest request) {
        entry.setTypeId(request.getTypeId());
        entry.setRequesterName(request.getRequesterName());
        entry.setUserId(request.getUserId());
        entry.setAccountId(request.getAccountId());
        entry.setChangedAt(request.getChangedAt());
        entry.setExpireAt(request.getExpireAt());
        entry.setChangeReason(request.getChangeReason());
        entry.setNote(request.getNote());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
