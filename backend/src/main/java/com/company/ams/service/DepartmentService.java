package com.company.ams.service;

import com.company.ams.common.exception.BusinessException;
import com.company.ams.common.exception.NotFoundException;
import com.company.ams.dto.DepartmentRequest;
import com.company.ams.entity.Department;
import com.company.ams.mapper.DepartmentMapper;
import com.company.ams.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final AuditLogService auditLogService;

    public List<Department> findAll() {
        return departmentMapper.findAll();
    }

    @Transactional
    public Department create(DepartmentRequest request, AuthPrincipal actor) {
        Department department = new Department();
        department.setDeptName(request.getDeptName());
        departmentMapper.insert(department);
        auditLogService.log(actor.getUserId(), "DEPARTMENT_CREATE", "dept:" + department.getDeptId(),
                "부서 생성: " + department.getDeptName());
        return department;
    }

    @Transactional
    public Department update(Integer deptId, DepartmentRequest request, AuthPrincipal actor) {
        Department department = departmentMapper.findById(deptId);
        if (department == null) {
            throw new NotFoundException("부서를 찾을 수 없습니다.");
        }
        department.setDeptName(request.getDeptName());
        departmentMapper.update(department);
        auditLogService.log(actor.getUserId(), "DEPARTMENT_UPDATE", "dept:" + deptId,
                "부서 수정: " + department.getDeptName());
        return department;
    }

    @Transactional
    public void delete(Integer deptId, AuthPrincipal actor) {
        Department department = departmentMapper.findById(deptId);
        if (department == null) {
            throw new NotFoundException("부서를 찾을 수 없습니다.");
        }
        if (departmentMapper.countUsersByDeptId(deptId) > 0) {
            throw new BusinessException("소속 직원이 있는 부서는 삭제할 수 없습니다.", HttpStatus.CONFLICT);
        }
        departmentMapper.deleteById(deptId);
        auditLogService.log(actor.getUserId(), "DEPARTMENT_DELETE", "dept:" + deptId,
                "부서 삭제: " + department.getDeptName());
    }
}
