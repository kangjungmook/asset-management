package com.company.ams.mapper;

import com.company.ams.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenMapper {

    void insert(RefreshToken refreshToken);

    RefreshToken findById(@Param("tokenId") String tokenId);

    void revokeAllByUserId(@Param("userId") Integer userId);

    void deleteAllByUserId(@Param("userId") Integer userId);
}
