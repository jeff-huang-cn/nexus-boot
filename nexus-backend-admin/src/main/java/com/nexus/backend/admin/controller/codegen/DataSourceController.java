package com.nexus.backend.admin.controller.codegen;

import com.nexus.backend.admin.controller.codegen.vo.DataSourceConfigCreateReqVO;
import com.nexus.backend.admin.controller.codegen.vo.DataSourceConfigRespVO;
import com.nexus.backend.admin.controller.codegen.vo.DataSourceConfigUpdateReqVO;
import com.nexus.backend.admin.controller.codegen.vo.DataSourcePageReqVO;
import com.nexus.backend.admin.dal.dataobject.codegen.DataSourceConfigDO;
import com.nexus.backend.admin.service.codegen.DataSourceConfigService;
import com.nexus.framework.web.result.PageResult;
import com.nexus.framework.web.result.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据源配置管理控制器
 *
 * @author nexus
 */
@Slf4j
@RestController
@RequestMapping("/codegen/datasource")
@Validated
public class DataSourceController {

    @Resource
    private DataSourceConfigService dataSourceConfigService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('codegen:database:query')")
    public Result<PageResult<DataSourceConfigRespVO>> getPage(@Valid DataSourcePageReqVO pageReqVO) {

        PageResult<DataSourceConfigDO> pageResult = dataSourceConfigService.getPage(pageReqVO);

        List<DataSourceConfigRespVO> list = pageResult.getList().stream()
                .map(ds -> convertForList(ds))
                .collect(Collectors.toList());

        return Result.success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('codegen:database:query')")
    public Result<List<DataSourceConfigRespVO>> getList() {
        List<DataSourceConfigDO> list = dataSourceConfigService.getList();
        List<DataSourceConfigRespVO> voList = list.stream()
                .map(ds -> convertForList(ds))
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('codegen:database:query')")
    public Result<DataSourceConfigRespVO> getById(@PathVariable Long id) {
        DataSourceConfigDO dataSource = dataSourceConfigService.getById(id);
        return Result.success(convertForDetail(dataSource));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('codegen:database:create')")
    public Result<Long> create(@Valid @RequestBody DataSourceConfigCreateReqVO reqVO) {
        DataSourceConfigDO dataSource = new DataSourceConfigDO();
        BeanUtils.copyProperties(reqVO, dataSource);
        Long id = dataSourceConfigService.create(dataSource);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('codegen:database:update')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DataSourceConfigUpdateReqVO reqVO) {
        DataSourceConfigDO dataSource = new DataSourceConfigDO();
        BeanUtils.copyProperties(reqVO, dataSource);
        dataSource.setId(id);
        dataSourceConfigService.update(dataSource);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('codegen:database:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        dataSourceConfigService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("hasAuthority('codegen:database:query')")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        boolean success = dataSourceConfigService.testConnection(id);
        return Result.success(success);
    }

    /**
     * 列表转换 - 密码脱敏
     */
    private DataSourceConfigRespVO convertForList(DataSourceConfigDO dataSource) {
        if (dataSource == null) {
            return null;
        }
        DataSourceConfigRespVO vo = new DataSourceConfigRespVO();
        BeanUtils.copyProperties(dataSource, vo);

        // 标记是否有密码
        vo.setHasPassword(dataSource.getPassword() != null && !dataSource.getPassword().isEmpty());
        // 密码不返回，设置为null
        vo.setPassword(null);

        return vo;
    }

    /**
     * 详情转换 - 返回真实密码
     */
    private DataSourceConfigRespVO convertForDetail(DataSourceConfigDO dataSource) {
        if (dataSource == null) {
            return null;
        }
        DataSourceConfigRespVO vo = new DataSourceConfigRespVO();
        BeanUtils.copyProperties(dataSource, vo);

        // 详情查询返回真实密码
        vo.setHasPassword(dataSource.getPassword() != null && !dataSource.getPassword().isEmpty());

        return vo;
    }
}
