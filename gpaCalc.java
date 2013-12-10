import java.util.*;
import java.io.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/* 
Todo: save new records back to file
*/

public class gpaCalc {
   static LinkedHashMap<String, Double> scale = new LinkedHashMap<String, Double>();
   static LinkedHashMap<String, Course> classes = new LinkedHashMap<String, Course>();
   static ArrayList<Course> newClasses = new ArrayList<Course>();

   static double totalGp;
   static int totalHours;
   static int numClasses;
   static double gpa;
   
   static Scanner userInput;
      
   //formatting/file constants
   static final int COL_SIZE = 39;
   static final String CONFIG = "config.txt";
   static final String GRADES = "report.txt";
   static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

   public static void main (String args[]) {
      System.out.println("GPA Calc v1.0");
      System.out.println("Written by: Matthew Rayermann");
      System.out.println("Type \"help\" for list of availble commands.\n");
      
      totalGp = 0.0;
      totalHours = 0;
      numClasses = 0;
      gpa = 0.0;
      
      File config = new File(CONFIG);
      File grades = new File(GRADES);
      
      userInput = new Scanner(System.in);
      
      //read in data
      readConfig(config);
      readGrades(grades, true);

      String input;
      System.out.print("> ");
      
      //user input loop
      while(!(input = userInput.nextLine().trim()).equals("quit")) {
         if(input.equals("classes"))
            printClasses();
         else if(input.equals("scale"))
            printScale();
         else if(input.equals("add"))
            addClass();
         else if(input.equals("remove"))
            removeClass();
         else if(input.equals("search"))
            searchClass();
         else if(input.equals("stats"))
            printStats();
         else if(input.equals("help"))
            printCommands();
         else if(input.equals("reload"))
            reload(grades);
         else if(input.equals("save"))
            save();
         else if(input.equals("generate"))
            generate();
         else
            System.out.println("Invalid command.");
         System.out.print("> ");
      }
      
      System.exit(-1);
   }
   
   //print out classes as a table
   public static void printClasses() {
      printBorder();
      System.out.println();
      Iterator i = classes.keySet().iterator();
      while(i.hasNext()) {
         String key = i.next().toString();
         String value = classes.get(key).toString();
         System.out.println(value);
         printBorder();
         System.out.println();
      }
   }
   
   public static void printBorder() {
      for(int d = 0; d < COL_SIZE; d++)
         System.out.print("-");
   }
   
   //print out grading scale
   public static void printScale() {
      Iterator i = scale.keySet().iterator();
      while(i.hasNext()) {
         String key = i.next().toString();
         String value = scale.get(key).toString();
         System.out.println(key + ": " + value);
      }
   }

   //print out general statistics
   public static void printStats() {
      gpa = totalGp / totalHours;      
      System.out.println("Total hours: " + totalHours);
      System.out.println("Total classes: " + numClasses);
      System.out.println("GPA: "+ gpa);
   }
   
   //add a class, same format as report.txt file
   public static void addClass() {
      System.out.println("Enter class details:");
      System.out.print(">> ");
      parseLine(userInput.nextLine().trim(), false);
   }
   
   //remove a class by name
   public static void removeClass() {
      System.out.println("Enter class name:");
      System.out.print(">> ");
      String names = userInput.nextLine().trim();
      String[] keys = names.split(",");
      for(int i = 0; i < keys.length; i++) {
         keys[i] = keys[i].trim();
         if(classes.containsKey(keys[i])) {
            totalGp -= classes.get(keys[i]).gradePoint;
            totalHours -= classes.get(keys[i]).hours;
            classes.remove(keys[i]);
            System.out.println("Removed: " + keys[i]);
         }
         else
            System.out.println("Not a valid class.");
      }
   }
   
   //search for a class by name
   public static void searchClass() {
      System.out.println("Enter class name:");
      System.out.print(">> ");
      String name = userInput.nextLine().trim();
      if(classes.containsKey(name)) {
         printBorder();
         System.out.println("\n" + classes.get(name).toString());
         printBorder();
         System.out.println();
      }
      else
         System.out.println("Not a valid class.");
   }
   
   //restore classes db to original state and reset all vars
   public static void reload(File grades) {
      classes.clear();
      totalHours = 0;
      gpa = 0.0;
      numClasses = 0;
      totalGp = 0;
      readGrades(grades, true);
   }

   //save new classes to the report file
   public static void save() {
      if(newClasses.size() > 0) {
         Iterator i = newClasses.iterator();
         try {
            FileWriter fw = new FileWriter(GRADES, true);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            fw.write("\n#" + sdf.format(cal.getTime()) + "\n");
            while(i.hasNext()) {
                Course c = (Course)i.next();
                fw.write(c.name + ", " + c.letterGrade + ", " + Integer.toString(c.hours) + "\n");
            }
            fw.close();
            newClasses.clear();
         }
         catch(IOException ioe) {
             System.err.println("IOException: " + ioe.getMessage());
         }
      }
      else { System.out.println("No changes to be made."); }    
   }

   //writes all classes to the report file, overwrites old file
   public static void generate() {
      System.out.println("Are you sure? y/n");
      System.out.print(">> ");
      String answer = userInput.nextLine().trim();
      while(!answer.equals("y") && !answer.equals("n")) {
         System.out.println("Invalid response, use y/n.");
         System.out.print(">> ");         
         answer = userInput.nextLine().trim();        
      }
      if(answer.equals("y")) {
         Iterator i = classes.keySet().iterator();
         try {
            FileWriter fw = new FileWriter(GRADES, false);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            fw.write("#" + sdf.format(cal.getTime()) + "\n");
            while(i.hasNext()) {
                Course c = classes.get(i.next());
                fw.write(c.name + ", " + c.letterGrade + ", " + Integer.toString(c.hours) + "\n");
            }
            fw.close();
         }
         catch(IOException ioe) {
             System.err.println("IOException: " + ioe.getMessage());
         }
      }
      else { System.out.println("Operation aborted."); }
   }
   
   //print out available commands
   public static void printCommands() {
      System.out.println("<classes> print out classes taken");
      System.out.println("<scale> print out grading scale");
      System.out.println("<stats> print out grade stats");
      System.out.println("<add> add or modify a class");
      System.out.println("<remove> remove a class by name");
      System.out.println("<search> search for a class by name");
      System.out.println("<reload> reload grade report");
      System.out.println("<save> save new classes to the grade report");
      System.out.println("<generate> overwrite current report with");
      System.out.println("<help> display all commands");
      System.out.println("<quit> exit program");
   }
   

   //read in config file (grading scale)
   public static void readConfig(File config) {
      System.out.println("Loading scale from " + config.toString() + "...");
      try {
         Scanner configScanner = new Scanner(config);
         while(configScanner.hasNext()) {
            String line = configScanner.nextLine().trim();
            try {
               if(!line.isEmpty() && line.charAt(0) != '#') {
                  String keys[] = line.split(",");
                  for(int i = 0; i < keys.length; i++)
                     keys[i] = keys[i].trim();
                  scale.put(keys[0], Double.parseDouble(keys[1]));
               }
            }
            catch (Exception e) {
               System.out.println("\nError parsing \"" + line + "\".");
               System.out.println("Wrong format.");
            }
         }
      }
      catch(IOException e) {
         System.out.println("Could not open config file. Exiting.");
         System.exit(-1);
      }
   }
   
   //read in report file (grade report)
   public static void readGrades(File grades, boolean initRead) {
      System.out.println("Loading classes from " + grades.toString() + "...");
      try {
         Scanner gradesScanner = new Scanner(grades);
         while(gradesScanner.hasNext()) {
            String line = gradesScanner.nextLine().trim();
            if(!line.isEmpty() && line.charAt(0) != '#') {
               parseLine(line, initRead);
            }
         }
      }
      catch(IOException e) {
         System.out.println("Could not open grade report file. Exiting.");
         System.exit(-1);
      }
      System.out.println("Loaded " + numClasses + " classes.");
   }
   
   //parse line of report file
   public static void parseLine(String line, boolean initRead) {
      String keys[] = line.split(",");
      for(int i = 0; i < keys.length; i++)
         keys[i] = keys[i].trim();
      try {
         int hours = Integer.parseInt(keys[2]);
         double gp = scale.get(keys[1]) * hours;
         if(!classes.containsKey(keys[0]) && !initRead)
            newClasses.add(new Course(keys[0], keys[1], gp, hours));
         classes.put(keys[0], new Course(keys[0], keys[1], gp, hours));
         totalGp += gp;
         totalHours += hours;
         numClasses++;
      }
      catch (NullPointerException e) {
         System.out.println("Error parsing \"" + line + "\".");
         System.out.println("No match found for letter grade in scale.");
      }
      catch (Exception e) {
         System.out.println("Error parsing \"" + line + "\".");
         System.out.println("Wrong format.");
      }
   }
}

class Course {   
   String name;
   String letterGrade;
   Double gradePoint;
   int hours;
   
   //formatting constants
   final int MAX_NAME_SIZE = 12;
   final int MAX_LETT_SIZE = 2;
   final int MAX_GNUM_SIZE = 5;
   
   public Course(String n, String lg, Double gp, int h) {
      name = n;
      letterGrade = lg;
      gradePoint = gp;
      hours = h;
   }
   
   //print out object in table row format
   public String toString() {
      String ret = "| ";
      
      ret += name;
      int extraWhite = MAX_NAME_SIZE - name.length();
      for(int w = 0; w < extraWhite; w++)
         ret += " ";
         
      ret += (" | " + hours + " hours | ");
      
      ret += letterGrade;
      extraWhite = MAX_LETT_SIZE - letterGrade.length();
      for(int w = 0; w < extraWhite; w++)
         ret += " ";
         
      ret += (" | " + gradePoint);
      extraWhite = MAX_GNUM_SIZE - Double.toString(gradePoint).length();
      for(int w = 0; w < extraWhite; w++)
         ret += " ";

      ret += " |";
      return ret;
   }   
}
