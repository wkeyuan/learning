package own.jadezhang.learning.apple.view.base;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.servlet.view.document.AbstractExcelView;
import own.jadezhang.learning.apple.domain.base.User;
import own.jadezhang.learning.apple.view.base.chart.ChartToImgMaker;
import own.jadezhang.learning.apple.view.base.chart.LineChartToImgMaker;
import own.jadezhang.learning.apple.view.base.chart.PieChartToImgMaker;
import own.jadezhang.learning.apple.view.base.excel.ExcelStyleFactory;
import own.jadezhang.learning.apple.view.base.excel.ExcelUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Zhang Junwei on 2016/10/28.
 */
public class DetailExcelView extends AbstractExcelView {

    //表头起始
    private static int HEADLINE_ROW = 3;

    @Override
    protected void buildExcelDocument(Map<String, Object> model, HSSFWorkbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<User> users = (List<User>) model.get("users");
        String fileName = "测试表格.xls";
        setResponse(request, response, fileName);
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(fileName);
        sheet.setDefaultColumnWidth((short) 10);
        sheet.setDefaultRowHeightInPoints(ExcelStyleFactory.ROW_HEIGHT_iN_POINTS);
        String[] header = {"序号", "姓名", "性别", "生日"};

        int colCount = header.length;
        int rowCount = users.size() + HEADLINE_ROW;
        //创建说明部分
        CellRangeAddress commentRangeAddress = new CellRangeAddress(0, 0, 0, colCount - 1);
        sheet.addMergedRegion(commentRangeAddress);
        ExcelUtil.setBorderForRegion(CellStyle.BORDER_THIN, commentRangeAddress, sheet, workbook);
        HSSFCellStyle commentStyle = ExcelStyleFactory.commentStyle(workbook);
        HSSFRow commentRow = sheet.createRow(0);
        commentRow.setHeightInPoints(100);
        HSSFCell commentCell = commentRow.createCell(0);
        commentCell.setCellStyle(commentStyle);
        commentCell.setCellValue("导入模板说明：\r\n1、设置默认：选择是/否\r\n2、合同时间：格式为yyyy-mm-dd\r\n备注：若合同信息超过20条，请复制黑色线框并在里面填写（黑色线框外为非工作区）");

        //创建标题
        CellRangeAddress titleRangeAddress = new CellRangeAddress(1, 1, 0, colCount - 1);
        sheet.addMergedRegion(titleRangeAddress);
        ExcelUtil.setBorderForRegion(CellStyle.BORDER_THIN, titleRangeAddress, sheet, workbook);

        HSSFCellStyle titleStyleStyle = ExcelStyleFactory.titleStyle(workbook);
        HSSFRow titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(ExcelStyleFactory.TITLE_HEIGHT_iN_POINTS);
        HSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellStyle(titleStyleStyle);
        titleCell.setCellValue(fileName);

        //创建头部
        HSSFCellStyle headerStyle = ExcelStyleFactory.headerStyle(workbook, IndexedColors.GREY_40_PERCENT.getIndex());
        HSSFRow headerRow = sheet.createRow(2);
        headerRow.setHeightInPoints(ExcelStyleFactory.ROW_HEIGHT_iN_POINTS);
        for (int i = 0; i < colCount; i++) {
            HSSFCell cell = headerRow.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(header[i]);
        }
        //锁定标题和头部
        //sheet.createFreezePane(0, HEADLINE_ROW);

        HSSFCellStyle cellStyle = ExcelStyleFactory.defaultStyle(workbook);
        HSSFCellStyle dateStyle = ExcelStyleFactory.dateCellStyle(workbook, "yyyy-MM");
        User user;
        int dataContentRow = users.size();
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        int[] commentRegion = {2, 2};
        for (int i = 0; i < dataContentRow; i++) {
            HSSFRow row = sheet.createRow(i + HEADLINE_ROW);
            row.setHeightInPoints(ExcelStyleFactory.ROW_HEIGHT_iN_POINTS);
            for (int j = 0; j < colCount; j++) {
                HSSFCell cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
                user = users.get(i);
                if (j == 0) {
                    cell.setCellValue(i + 1);
                }
                if (j == 1) {
                    cell.setCellValue(user.getName());
                    ExcelUtil.commentForCell("姓名\r\n15671569027", commentRegion, patriarch, cell);
                }
                if (j == 3) {
                    cell.setCellStyle(dateStyle);
                }
            }
        }
        String[] sexConstraint = {"男", "女"};
        ExcelUtil.explicitListConstraint(sexConstraint, new int[]{HEADLINE_ROW, rowCount - 1, 2, 2}, sheet);

        String pieImgPath = "D:\\imgPath.png";
        Map<String, String> nameOption = new HashMap<String, String>();
        nameOption.put(ChartToImgMaker.TITLE_KEY, "BMI值");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("正常", 23);
        data.put("偏胖", 3);
        data.put("偏瘦", 5);
        data.put("肥胖", 2);
        PieChartToImgMaker pieChartToImgMaker = new PieChartToImgMaker();
        pieChartToImgMaker.trans(nameOption, data, pieImgPath, 500,500);
        int[] chartImgPosition = {2, 15, 5, 10};
        ExcelUtil.pictureToPosition(pieImgPath, chartImgPosition, patriarch, workbook);

        nameOption.put(ChartToImgMaker.TITLE_KEY, "第一季度温度情况");
        nameOption.put(ChartToImgMaker.X_AXIS_KEY, "月份");
        nameOption.put(ChartToImgMaker.Y_AXIS_KEY, "温度");

        Map<String, Object> lineDate = new HashMap<String, Object>();
        List<String> seriesKeys= new ArrayList<String>();
        seriesKeys.add("1月份");
        seriesKeys.add("2月份");
        seriesKeys.add("3月份");
        lineDate.put(LineChartToImgMaker.SERIES_KEY_LIST, seriesKeys);
        List<Double> series1= new ArrayList<Double>();
        series1.add(23D);
        series1.add(25D);
        series1.add(21D);
        lineDate.put("最高",series1);
        List<Double> series2= new ArrayList<Double>();
        series2.add(8D);
        series2.add(11D);
        series2.add(5D);
        lineDate.put("最低",series2);
        List<Double> series3= new ArrayList<Double>();
        series3.add(15.5);
        series3.add(18.0);
        series3.add(13.0);
        lineDate.put("平均",series3);

        String lineChartImgPath = "D:\\lineImg.jpg";
        LineChartToImgMaker lineChartToImgMaker = new LineChartToImgMaker();
        lineChartToImgMaker.trans(nameOption, lineDate, lineChartImgPath, 1000, 800);
        chartImgPosition[1] = 30;
        chartImgPosition[2] = 10;
        chartImgPosition[3] = 20;
        ExcelUtil.pictureToPosition(lineChartImgPath, chartImgPosition, patriarch, workbook);

    }

    private void setResponse(HttpServletRequest request, HttpServletResponse response, String fileName) throws Exception {
        // 处理中文文件名
        response.setContentType("application/vnd.ms-excel");
        //兼容火狐
        if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
            response.setHeader("Content-disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1"));
        } else {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        }
    }
}
