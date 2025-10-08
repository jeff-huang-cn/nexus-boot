package com.nexus.backend.admin.controller.permission.vo.role;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色保存请求 VO
 *
 * @author nexus
 */
@Data
public class RoleSaveReqVO {

    /**
     * 角色ID（更新时需要）
     */
    @ExcelIgnore
    private Long id;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @ExcelProperty("角色名称")
    private String name;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
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
     * 备注
     */
    @ExcelProperty("备注")
    private String remark;

}
