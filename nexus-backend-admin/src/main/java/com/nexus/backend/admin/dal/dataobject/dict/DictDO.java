package com.nexus.backend.admin.dal.dataobject.dict;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nexus.framework.mybatis.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统字典 DO
 *
 * @author nexus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_dict")
public class DictDO extends BaseDO {

    /**
     * 字典ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 颜色类型
     */
    private String colorType;

    /**
     * CSS类名
     */
    private String cssClass;

    /**
     * 备注
     */
    private String remark;

}
