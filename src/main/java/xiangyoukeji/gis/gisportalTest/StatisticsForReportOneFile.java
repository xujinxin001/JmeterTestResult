package xiangyoukeji.gis.gisportalTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StatisticsForReportOneFile {

    public static void main(String[] args) throws IOException {
        System.out.printf("hello~");

        //String[] args2 = new String["F:\\my_work\\xiangyoukeji\\自动化测试结果\\result","222"];
        //E:\3_E_pan\2_it_skill\3_java\demo\ideaIC\learn\2021\202111\JmeterTestResult\src\main\resources\result

        //String resultPath = "E:"+File.separator+"3_E_pan"+File.separator+"2_it_skill"+File.separator+"3_java";
        String resultPath = "E:\\3_E_pan\\2_it_skill\\3_java\\demo\\ideaIC\\learn\\2021\\202111\\JmeterTestResult\\src\\main\\resources\\result";
        System.out.println("resultPath value： "+ resultPath.toString());

        String[] args2 = new String[]{resultPath,"222","333"};
        System.out.println("resultPath value： "+ args2.toString());

        //args = args2;


        String resultFilePath = args[0]; // 运行时指定参数
        System.out.println("resultFilePath value is ： "+ resultFilePath);
        //System.exit(0);

        String statisticSaveLocation = resultFilePath + File.separator + "statistics";


        File resultDir = new File(resultFilePath);
        List<File> resultFiles = Arrays.asList(resultDir.listFiles());
        List<String> summaryResult = new ArrayList<>();
        for (File resultFile : resultFiles) {
            String resultFileName = resultFile.getName();
            List<File> everyResultFiles = Arrays.asList(resultFile.listFiles());
            if ("statistics".equals(resultFileName)) {
                continue;
            }
            List<String> headerLine = getHeaderLine();
            for (File everyResultFile : everyResultFiles) {
                String everyResultFileName = everyResultFile.getName(); // 如5_70_11
                if (everyResultFile == null) {
                    continue;
                }
                File statisticsJsonFile = null;
                try {
                    statisticsJsonFile = getStatisticsJsonFile(everyResultFile);
                    String json = FileUtils.readFileToString(statisticsJsonFile);
                    JSONObject jsonObject = JSON.parseObject(json);
                    JSONObject totalJsonObj = jsonObject.getJSONObject("Total");
                    String colval = getColVal(totalJsonObj, everyResultFile);
                    colval = resultFileName + "," + colval;
                    headerLine.add(colval);
                } catch (Exception e) {
                    System.out.println(resultFileName + "_" + everyResultFileName + "出了点小问题");
                }

            }
            summaryResult.addAll(headerLine);

        }
        removeDumplicateHeader(summaryResult);
        FileUtils.writeLines(new File(statisticSaveLocation + File.separator + "summary.csv"), summaryResult);

    }

    private static void removeDumplicateHeader(List<String> summaryResult) {
        int i = 0;
        Iterator<String> it = summaryResult.iterator();
        while (it.hasNext()) {
            String line = it.next();
            if (line.contains("案列名称") && i != 0) {
                it.remove();
            }
            i++;
        }
    }


    private static String getColVal(JSONObject totalJsonObj, File everyResultFile) throws Exception {
//        String sample = totalJsonObj.getString("sample");
//        String transaction = totalJsonObj.getString("transaction");
        String sampleCount = totalJsonObj.getString("sampleCount");
        String errorCount = totalJsonObj.getString("errorCount");
        String errorPct = totalJsonObj.getString("errorPct") + "%";
        String meanResTime = totalJsonObj.getString("meanResTime");
        String str =
                sampleCount + "," +
                        errorCount + "," +
                        errorPct + "," +
                        meanResTime;
        if (Integer.parseInt(errorCount) == 1) {
            String errorReason = findErrorReason(everyResultFile);
            str += "," + errorReason;
        }

        return str;
    }


    private static String findErrorReason(File everyResultFile) throws Exception {
        File[] fileArr = everyResultFile.listFiles();
        if (fileArr == null) {
            throw new Exception("空");
        }
        List<File> files = Arrays.asList(fileArr);
        File resultTxtFile = null;
        for (File file : files) {
            if (!file.isDirectory()) {
                resultTxtFile = file;
                break;
            }

        }
        String line = FileUtils.readLines(resultTxtFile).get(1);

        String str = line.split(",")[3] + ":" + line.split(",")[4];
        return str;
    }


    private static File getStatisticsJsonFile(File everyResultFile) throws Exception {
        File[] fileArr = everyResultFile.listFiles();
        if (fileArr == null) {
            throw new Exception("空");
        }
        List<File> files = Arrays.asList(fileArr);
        File reportDir = null;
        for (File file : files) {
            if (file.isDirectory()) {
                reportDir = file;
                break;
            }

        }
        File[] statisticsFiles = reportDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.equals("statistics.json")) {
                    return true;
                } else {
                    return false;
                }

            }
        });
        return statisticsFiles[0];
    }

    private static List<String> getHeaderLine() {
        String header = "案列名称,sampleCount,errorCount,errorPct,meanResTime(ms),失败原因";
        List<String> list = new ArrayList<>();
        list.add(header);
        return list;
    }


}
