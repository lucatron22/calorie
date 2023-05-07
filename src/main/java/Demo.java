import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import io.github.cdimascio.dotenv.Dotenv;

public class Demo {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String name;
        ArrayList<Food> foodList = new ArrayList<Food>();
        boolean fileExists = new File("foodAnalysis.xlsx").exists();
        
        while (true) {
            System.out.print("Enter meal (-1 to exit): ");
            name = scanner.nextLine();
            if (name.equals("-1")) {
                break;
            }
            JSONObject meal = new JSONObject();
            meal.put("query", name);
            HttpClient client = buildClient();
            HttpRequest request = buildRequest(meal);
            Food mealStats = fetchAPI(client, request);
            if (fileExists) {
                appendFoodExcel(mealStats);
            } else {
                buildMealExcel();
                appendFoodExcel(mealStats);
                fileExists = true;
            }
            foodList.add(mealStats);
        }
        FoodCollection fc = new FoodCollection(foodList);
        writeToFile(fc);
        System.out.println(fc.toString());
    }

    public static HttpClient buildClient() {
        HttpClient client = HttpClient.newBuilder().build();
        return client;
    }

    public static HttpRequest buildRequest(JSONObject meal) {
        Dotenv dotenv = Dotenv.load();
        return HttpRequest.newBuilder()
            .uri(URI.create("https://trackapi.nutritionix.com/v2/natural/nutrients"))
            .header("Content-Type", "application/json")
            .header("x-app-id", "6dd7e6b5")
            .header("x-app-key", dotenv.get("API_KEY"))
            .POST(HttpRequest.BodyPublishers.ofString(meal.toString()))
            .build();

    }

    public static Food fetchAPI(HttpClient client, HttpRequest request) throws Exception {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        JSONObject json = new JSONObject(responseBody);
        JSONArray array = json.getJSONArray("foods");
        JSONObject foodStats = array.getJSONObject(0);
        Food food = new Food(
            foodStats.getString("food_name"),
            foodStats.getDouble("nf_calories"),
            foodStats.getDouble("nf_protein"),
            foodStats.getDouble("nf_total_carbohydrate"),
            foodStats.getDouble("nf_total_fat")
        );

        return food;
    }

    public static void buildMealExcel() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        String filePath = System.getProperty("user.dir") + "/src/main/java/foodAnalysis.xlsx";
        FileOutputStream out = new FileOutputStream(new File(filePath));
  
        XSSFSheet spreadsheet
            = workbook.createSheet(" Food Data ");
        XSSFRow row;
        Map<String, Object[]> foodData
            = new TreeMap<String, Object[]>();

       foodData.put("1", new Object[] {"Date", "Name", "Calories", "Protein", "Fat" });

        Set<String> keyid = foodData.keySet();
        int rowid = 0;

        for (String key : keyid) {
            row = spreadsheet.createRow(rowid++);
            Object[] objectArr = foodData.get(key);
            int cellid = 0;

            for (Object obj : objectArr) {
                Cell cell = row.createCell(cellid++);
                cell.setCellValue((String)obj);
            }
        }

       workbook.write(out);
       out.close();
       System.out.println("Success");
    }

    public static void appendFoodExcel(Food food) throws Exception {
        String filePath = System.getProperty("user.dir") + "/src/main/java/foodAnalysis.xlsx";
        FileInputStream inputStream = new FileInputStream(new File(filePath));
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
    
        int lastRowNum = sheet.getLastRowNum() + 1; // Add 1 to get the next row
        XSSFRow row = sheet.createRow(lastRowNum);
    
        row.createCell(0).setCellValue(java.time.LocalDate.now().toString());
        row.createCell(1).setCellValue(food.food_name);
        row.createCell(2).setCellValue(food.nf_calories);
        row.createCell(3).setCellValue(food.nf_protein);
        row.createCell(4).setCellValue(food.nf_total_fat);
    
        inputStream.close();
    
        FileOutputStream outputStream = new FileOutputStream(filePath);
        workbook.write(outputStream);
        outputStream.close();
    }
    

    public static void writeToFile(FoodCollection fc) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("foodAnalysi.txt", true))) {
            bw.write(fc.toString());
            bw.newLine();
            System.out.println("FoodCollection has been written to the file.");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
