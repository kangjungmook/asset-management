package com.company.ams.service;

import com.company.ams.common.exception.BusinessException;
import com.company.ams.common.exception.NotFoundException;
import com.company.ams.dto.PermissionGrantRequest;
import com.company.ams.dto.RoleGrantRequest;
import com.company.ams.dto.TempPasswordResponse;
import com.company.ams.dto.UserCreateRequest;
import com.company.ams.dto.UserDetailResponse;
import com.company.ams.dto.UserUpdateRequest;
import com.company.ams.entity.Permission;
import com.company.ams.entity.User;
import com.company.ams.mapper.AuditLogMapper;
import com.company.ams.mapper.NotificationMapper;
import com.company.ams.mapper.PasswordMapper;
import com.company.ams.mapper.PermissionMapper;
import com.company.ams.mapper.RefreshTokenMapper;
import com.company.ams.mapper.UserMapper;
import com.company.ams.mapper.UserPermissionMapper;
import com.company.ams.mapper.UserRoleMapper;
import com.company.ams.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> VALID_ROLES = Set.of("DEPT_MANAGER", "USER");
    private static final String TEMP_PW_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$";
    public static final String DEFAULT_INITIAL_PASSWORD = "1234";

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserPermissionMapper userPermissionMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordMapper passwordMapper;
    private final AuditLogMapper auditLogMapper;
    private final NotificationMapper notificationMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public List<User> findAll(Integer deptId, String keyword) {
        return userMapper.findAll(deptId, keyword);
    }

    public UserDetailResponse findDetail(Integer userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        return toDetail(user);
    }

    private UserDetailResponse toDetail(User user) {
        UserDetailResponse response = new UserDetailResponse();
        response.setUserId(user.getUserId());
        response.setEmployeeNo(user.getEmployeeNo());
        response.setName(user.getName());
        response.setDeptId(user.getDeptId());
        response.setDeptName(user.getDeptName());
        response.setIsAdmin(user.getIsAdmin());
        response.setIsActive(user.getIsActive());
        response.setMustChangePassword(user.getMustChangePassword());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setRoles(userRoleMapper.findRoleCodesByUserId(user.getUserId()));
        response.setPermissions(userPermissionMapper.findByUserId(user.getUserId()));
        return response;
    }

    @Transactional
    public User create(UserCreateRequest request, AuthPrincipal actor) {
        if (userMapper.existsByEmployeeNo(request.getEmployeeNo())) {
            throw new BusinessException("이미 존재하는 사번입니다.", HttpStatus.CONFLICT);
        }
        if ("DEPT_MANAGER".equals(request.getRole()) && request.getDeptId() == null) {
            throw new BusinessException("부서 관리자는 부서를 지정해야 합니다.");
        }
        User user = new User();
        user.setEmployeeNo(request.getEmployeeNo());
        user.setPassword(passwordEncoder.encode(DEFAULT_INITIAL_PASSWORD));
        user.setName(request.getName());
        user.setDeptId(request.getDeptId());
        user.setIsAdmin(false);
        user.setMustChangePassword(true);
        userMapper.insert(user);
        auditLogService.log(actor.getUserId(), "USER_CREATE", "user:" + user.getUserId(),
                "사용자 생성: " + user.getEmployeeNo());

        if (request.getRole() != null && !request.getRole().isBlank()) {
            if (!VALID_ROLES.contains(request.getRole())) {
                throw new BusinessException("유효하지 않은 역할입니다.");
            }
            userRoleMapper.insert(user.getUserId(), request.getRole(), actor.getUserId());
            auditLogService.log(actor.getUserId(), "ROLE_GRANT", "user:" + user.getUserId(),
                    "역할 부여: " + request.getRole());
        }
        return user;
    }

    @Transactional
    public User update(Integer userId, UserUpdateRequest request, AuthPrincipal actor) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        user.setName(request.getName());
        user.setDeptId(request.getDeptId());
        userMapper.update(user);
        auditLogService.log(actor.getUserId(), "USER_UPDATE", "user:" + userId,
                "사용자 수정: " + user.getEmployeeNo());
        return user;
    }

    @Transactional
    public void delete(Integer userId, AuthPrincipal actor) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        if (userId.equals(actor.getUserId())) {
            throw new BusinessException("본인 계정은 삭제할 수 없습니다.");
        }
        if (Boolean.TRUE.equals(user.getIsAdmin()) && Boolean.TRUE.equals(user.getIsActive())
                && userMapper.countActiveAdmins() <= 1) {
            throw new BusinessException("마지막 남은 활성 관리자 계정은 삭제할 수 없습니다.");
        }
        if (passwordMapper.existsByUserId(userId)) {
            throw new BusinessException("패스워드 관리대장에 등록된 계정이 있어 삭제할 수 없습니다. 비활성화를 이용해주세요.", HttpStatus.CONFLICT);
        }
        if (userRoleMapper.existsGrantedBy(userId) || userPermissionMapper.existsGrantedBy(userId)) {
            throw new BusinessException("다른 사용자에게 역할·권한을 부여한 이력이 있어 삭제할 수 없습니다. 비활성화를 이용해주세요.", HttpStatus.CONFLICT);
        }

        userRoleMapper.deleteAllByUserId(userId);
        userPermissionMapper.deleteAllByUserId(userId);
        refreshTokenMapper.deleteAllByUserId(userId);
        notificationMapper.deleteAllByUserId(userId);
        auditLogService.log(actor.getUserId(), "USER_DELETE", "user:" + userId,
                "사용자 삭제: " + user.getEmployeeNo());
        auditLogMapper.nullifyUser(userId);
        userMapper.deleteById(userId);
    }

    @Transactional
    public void deactivate(Integer userId, AuthPrincipal actor) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        if (userId.equals(actor.getUserId())) {
            throw new BusinessException("본인 계정은 비활성화할 수 없습니다.");
        }
        if (Boolean.TRUE.equals(user.getIsAdmin()) && Boolean.TRUE.equals(user.getIsActive())
                && userMapper.countActiveAdmins() <= 1) {
            throw new BusinessException("마지막 남은 활성 관리자 계정은 비활성화할 수 없습니다.");
        }
        userMapper.updateActive(userId, false);
        auditLogService.log(actor.getUserId(), "USER_DEACTIVATE", "user:" + userId,
                "사용자 비활성화: " + user.getEmployeeNo());
    }

    @Transactional
    public void activate(Integer userId, AuthPrincipal actor) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        userMapper.updateActive(userId, true);
        auditLogService.log(actor.getUserId(), "USER_ACTIVATE", "user:" + userId,
                "사용자 활성화: " + user.getEmployeeNo());
    }

    @Transactional
    public void grantRole(Integer userId, RoleGrantRequest request, AuthPrincipal actor) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        if (!VALID_ROLES.contains(request.getRole())) {
            throw new BusinessException("유효하지 않은 역할입니다.");
        }
        if ("DEPT_MANAGER".equals(request.getRole()) && user.getDeptId() == null) {
            throw new BusinessException("부서 관리자는 부서를 지정해야 합니다.");
        }
        if (userRoleMapper.exists(userId, request.getRole())) {
            return;
        }
        userRoleMapper.insert(userId, request.getRole(), actor.getUserId());
        auditLogService.log(actor.getUserId(), "ROLE_GRANT", "user:" + userId,
                "역할 부여: " + request.getRole());
    }

    @Transactional
    public void revokeRole(Integer userId, String role, AuthPrincipal actor) {
        userRoleMapper.delete(userId, role);
        auditLogService.log(actor.getUserId(), "ROLE_REVOKE", "user:" + userId,
                "역할 해제: " + role);
    }

    @Transactional
    public void grantPermission(Integer userId, PermissionGrantRequest request, AuthPrincipal actor) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        Permission permission = permissionMapper.findById(request.getPermId());
        if (permission == null) {
            throw new NotFoundException("권한을 찾을 수 없습니다.");
        }
        if (userPermissionMapper.exists(userId, request.getPermId())) {
            return;
        }
        userPermissionMapper.insert(userId, request.getPermId(), actor.getUserId());
        auditLogService.log(actor.getUserId(), "PERMISSION_GRANT", "user:" + userId,
                "권한 부여: " + permission.getPermCode());
    }

    @Transactional
    public void revokePermission(Integer userId, Integer permId, AuthPrincipal actor) {
        userPermissionMapper.delete(userId, permId);
        auditLogService.log(actor.getUserId(), "PERMISSION_REVOKE", "user:" + userId,
                "권한 해제: permId=" + permId);
    }

    @Transactional
    public TempPasswordResponse resetPassword(Integer userId, AuthPrincipal actor) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        String tempPassword = generateTempPassword();
        userMapper.updatePassword(userId, passwordEncoder.encode(tempPassword), true);
        auditLogService.log(actor.getUserId(), "PASSWORD_RESET", "user:" + userId,
                "임시 비밀번호 발급: " + user.getEmployeeNo());
        return new TempPasswordResponse(user.getEmployeeNo(), tempPassword);
    }

    private String generateTempPassword() {
        SecureRandom random = new SecureRandom();
        return random.ints(10, 0, TEMP_PW_CHARS.length())
                .mapToObj(TEMP_PW_CHARS::charAt)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    @Transactional
    public void changeOwnPassword(Integer userId, String currentPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(newPassword), false);
        auditLogService.log(userId, "PASSWORD_CHANGE", "user:" + userId, "본인 비밀번호 변경");
    }
}
