package com.nexus.backend.admin.controller.permission.vo.role;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色响应 VO
 *
 * @author nexus
 */
@Data
public class RoleRespVO {

    /**
     * 角色ID
     */
    @ExcelIgnore
    private Long id;

    /**
     * 角色名称
     */
    @ExcelProperty("角色名称")
    private String name;

    /**
     * 角色编码
     */
    @ExcelProperty("角色编码")
    private String code;

    /**
     * 显示顺序
     */
    @ExcelProperty("显示顺序")
    private Integer sort;

    /**
     * 角色状态：0-禁用 1-启用
     */
    @ExcelProperty("角色状态")
    private Integer status;

    /**
     * 角色类型：1-系统内置 2-自定义
     */
    @ExcelProperty("角色类型")
    private Integer type;

    /**
     * 备注
     */
    @ExcelProperty("备注")
    private String remark;

    /**
     * 创建时间
     */
    @ExcelIgnore
    private LocalDateTime dateCreated;

}
