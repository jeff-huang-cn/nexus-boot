package com.nexus.framework.excel;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Excel 导入导出工具类
 *
 * @author nexus
 */
public class ExcelUtils {

    /**
     * 导出 Excel
     *
     * @param response  HTTP响应
     * @param data      导出数据
     * @param clazz     数据类型
     * @param fileName  文件名（不包含扩展名）
     * @param sheetName sheet名称
     * @param <T>       数据类型
     * @throws IOException IO异常
     */
    public static <T> void export(HttpServletResponse response, List<T> data, Class<T> clazz,
            String fileName, String sheetName) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 编码文件名
        String encodedFileName = URLEncoder.encode(fileName + "_" + System.currentTimeMillis(), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

        // 使用 EasyExcel 导出
        EasyExcel.write(response.getOutputStream(), clazz)
                .sheet(sheetName)
                .doWrite(data);
    }

    /**
     * 导出 Excel（使用默认sheet名称）
     *
     * @param response HTTP响应
     * @param data     导出数据
     * @param clazz    数据类型
     * @param fileName 文件名（不包含扩展名）
     * @param <T>      数据类型
     * @throws IOException IO异常
     */
    public static <T> void export(HttpServletResponse response, List<T> data, Class<T> clazz,
            String fileName) throws IOException {
        export(response, data, clazz, fileName, "数据");
    }

    /**
     * 导入 Excel
     *
     * @param inputStream 输入流
     * @param clazz       数据类型
     * @param <T>         数据类型
     * @return 导入的数据列表
     */
    public static <T> List<T> importExcel(java.io.InputStream inputStream, Class<T> clazz) {
        return EasyExcel.read(inputStream, clazz, null)
                .sheet()
                .doReadSync();
    }
}
