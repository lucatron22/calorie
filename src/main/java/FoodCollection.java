import java.time.LocalDate;
import java.util.ArrayList;  

public class FoodCollection {
    public ArrayList<Food> foodCollection = new ArrayList<Food>();
    public int totalProtein;
    public int totalCalories;
    public int totalFat;
    public LocalDate currDate;

    public FoodCollection(ArrayList<Food> foods) {
        currDate = java.time.LocalDate.now(); //retrieve date (yr - month - day)
        for(Food arg : foods) { 
            foodCollection.add(arg); //add each food obj to foodCollection
            totalProtein += arg.nf_protein;
            totalCalories += arg.nf_calories;
            totalFat += arg.nf_total_fat;
        }
    }

    public String toString() {
        String output = "";

        output += "The meals had " + totalCalories + " calories, " + totalProtein + " grams of protein, and " + totalFat + " grams of fat.";

        return output += "\n" + "Date: " + currDate.toString();
    }

}
