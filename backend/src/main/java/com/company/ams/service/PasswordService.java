package com.company.ams.service;

import com.company.ams.common.exception.NotFoundException;
import com.company.ams.dto.PageResponse;
import com.company.ams.dto.PasswordRequest;
import com.company.ams.entity.PasswordEntry;
import com.company.ams.mapper.PasswordMapper;
import com.company.ams.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private static final int EXPIRING_WITHIN_DAYS = 30;

    private final PasswordMapper passwordMapper;
    private final AuditLogService auditLogService;

    public PageResponse<PasswordEntry> findAll(Integer deptId, Integer userId, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;

        List<PasswordEntry> content = passwordMapper.findAll(deptId, userId, keyword, offset, safeSize);
        long total = passwordMapper.countAll(deptId, userId, keyword);
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

    @Transactional
    public PasswordEntry create(PasswordRequest request, AuthPrincipal actor) {
        PasswordEntry entry = new PasswordEntry();
        applyRequest(entry, request);
        entry.setRequesterId(request.getRequesterId() != null ? request.getRequesterId() : actor.getUserId());
        entry.setLastModifier(actor.getName());
        passwordMapper.insert(entry);
        auditLogService.log(actor.getUserId(), "PASSWORD_ENTRY_CREATE", "pw:" + entry.getPwId(),
                "패스워드 대장 등록: accountId=" + entry.getAccountId());
        return entry;
    }

    @Transactional
    public PasswordEntry update(Integer pwId, PasswordRequest request, AuthPrincipal actor) {
        PasswordEntry entry = findById(pwId);
        applyRequest(entry, request);
        entry.setLastModifier(actor.getName());
        passwordMapper.update(entry);
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
        entry.setUserId(request.getUserId());
        entry.setApproverId(request.getApproverId());
        entry.setAccountId(request.getAccountId());
        entry.setChangedAt(request.getChangedAt());
        entry.setExpireAt(request.getExpireAt());
        entry.setChangeReason(request.getChangeReason());
        entry.setNote(request.getNote());
    }
}
