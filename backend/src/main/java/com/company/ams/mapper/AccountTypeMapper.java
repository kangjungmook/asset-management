package com.company.ams.mapper;

import com.company.ams.entity.AccountType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountTypeMapper {

    List<AccountType> findAll();

    AccountType findById(@Param("typeId") Integer typeId);

    void insert(AccountType accountType);

    void update(AccountType accountType);

    void deleteById(@Param("typeId") Integer typeId);
}
