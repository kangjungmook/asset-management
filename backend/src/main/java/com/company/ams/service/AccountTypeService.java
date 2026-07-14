package com.company.ams.service;

import com.company.ams.common.exception.NotFoundException;
import com.company.ams.dto.AccountTypeRequest;
import com.company.ams.entity.AccountType;
import com.company.ams.mapper.AccountTypeMapper;
import com.company.ams.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountTypeService {

    private final AccountTypeMapper accountTypeMapper;
    private final AuditLogService auditLogService;

    public List<AccountType> findAll() {
        return accountTypeMapper.findAll();
    }

    @Transactional
    public AccountType create(AccountTypeRequest request, AuthPrincipal actor) {
        AccountType accountType = new AccountType();
        accountType.setTypeName(request.getTypeName());
        accountType.setDescription(request.getDescription());
        accountTypeMapper.insert(accountType);
        auditLogService.log(actor.getUserId(), "ACCOUNT_TYPE_CREATE", "type:" + accountType.getTypeId(),
                "계정유형 생성: " + accountType.getTypeName());
        return accountType;
    }

    @Transactional
    public AccountType update(Integer typeId, AccountTypeRequest request, AuthPrincipal actor) {
        AccountType accountType = accountTypeMapper.findById(typeId);
        if (accountType == null) {
            throw new NotFoundException("계정유형을 찾을 수 없습니다.");
        }
        accountType.setTypeName(request.getTypeName());
        accountType.setDescription(request.getDescription());
        accountTypeMapper.update(accountType);
        auditLogService.log(actor.getUserId(), "ACCOUNT_TYPE_UPDATE", "type:" + typeId,
                "계정유형 수정: " + accountType.getTypeName());
        return accountType;
    }

    @Transactional
    public void delete(Integer typeId, AuthPrincipal actor) {
        AccountType accountType = accountTypeMapper.findById(typeId);
        if (accountType == null) {
            throw new NotFoundException("계정유형을 찾을 수 없습니다.");
        }
        accountTypeMapper.deleteById(typeId);
        auditLogService.log(actor.getUserId(), "ACCOUNT_TYPE_DELETE", "type:" + typeId,
                "계정유형 삭제: " + accountType.getTypeName());
    }
}
