package application;

//required for text analyzer
import java.net.*;
import java.io.*;
import java.util.*;

//required for GUI
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

//required for database functionality
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * This program reads "The Raven" by Edgar Allen Poe from a URL
 * and displays a GUI allowing you to select the x most common words
 * in the poem by using information sorted in a MySQL Workbench database.
 * 
 * @author Richard Caraballo
 */
public class Main extends Application {

	/**
	 * Accesses poem "The Raven" from a URL, stores each word that appears along with its frequency
	 * in a MySQL Workbench database. Also launches the GUI which is used to retrieve the top x entries.
	 * Words are capitalized before entry into the database.
	 * @param args System parameters
	 * @throws Exception
	 * @see #insert(String, Integer)
	 */
	public static void main(String[] args) throws Exception{
			
		URL url = new URL("https://www.gutenberg.org/files/1065/1065-h/1065-h.htm");
		//must be encoded as UTF-8 to allow open and closed quotation marks from HTML to be read properly by Eclipse
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			
		String text = reader.readLine();
		boolean poemText = false;
			
		//stores each individual word in the poem
		String[] wordArray;
		//hashmap and variables for counting words
		HashMap<String,Integer> wordCountMap = new HashMap<String,Integer>();
		boolean match = false;
		String matchKey = "";
			
		//while the end of the text from the URL has not been reached, the "text" variable is set to each line
		//on the page and then removes that line's tags and punctuation
		while(text != null) {
				
			//removes html tags and replaces mdashes with spaces
			text = text.replaceAll("<.*?>", "");
			text = text.replace("&mdash;", " ");
				
			//removes apostrophes when they're not being used as contractions
			//to get rid of single-quotes
			text = text.replace("‘", "");
			text = text.replaceAll("’(?![[a-z][A-Z]])", "");
				
			//sets poemText to true or false when the beginning and end of the poem are reached
			//this lets us keep track of just the poem's text
			if(text.equals("The Raven")) {
				poemText = true;
			}
			if(text.equals("*** END OF THE PROJECT GUTENBERG EBOOK THE RAVEN ***")) {
				poemText = false;
			}
				
			//if the "text" variable is currently reading part of the poem, the line is split into individual words
			//which are stored in wordCountMap as keys, and their values are set to how many times they've appeared
			if(poemText) {
					
				//splits each line of text into individual words which are stored in wordArray
				wordArray = text.split("[ “”;!?.,-:]+");
					
				//loops through the array of every word in the poem
				for(int i = 0; i < wordArray.length; i++) {
						
					//capitalizes the first letter of every word in wordArray
					if(!wordArray[i].equals("")) {
						wordArray[i] = (wordArray[i].substring(0,1)).toUpperCase() + wordArray[i].substring(1);
					}
						
					//compares the current word in wordArray to every value in the hashmap (which stores one of each word)
					//and notes if there's a match (this will not run for the first word in wordArray)
					for(String word : wordCountMap.keySet()) {
						if(word.equals(wordArray[i])) {
							match = true;
							matchKey = word;
						}
					}
					//if the current word in wordArray matches a word in the hashmap, the counter for
					//the word in the hashmap increases
					//otherwise, the word has not been seen yet and is added to the hashmap
					if(match) {
						wordCountMap.put(matchKey, wordCountMap.get(matchKey) + 1);
						match = false;
					}else {
						wordCountMap.put(wordArray[i], 1);
					}
						
						
						
				}
					
			}
				
			//iterator moves to the next line of the text
			text = reader.readLine();
		}
		reader.close();
			
		//removes the blank lines from the hashmap
		wordCountMap.remove("");

		//accesses the database to store the words and their frequencies from the hashmap	
		for(Map.Entry<String,Integer> word : wordCountMap.entrySet()) {
			insert(word.getKey(), word.getValue());
		}

		
		//starts GUI
		launch(args);
	}
	
	/**
	 * Initializes components for the GUI and handles button events which display
	 * the entries from the database.
	 * @throws Exception
	 * @see #labelChange(int, Label[])
	 */
	public void start(Stage primaryStage) throws Exception{
		try {
			
			TilePane pane = new TilePane();
			pane.setTileAlignment(Pos.CENTER);
			
			//creating buttons and labels to add to the GUI
			Label leftLabel = new Label("How many words from the");
			Label rightLabel = new Label(" Raven do you want to see?");
			leftLabel.setFont(Font.font("ariel",FontWeight.BOLD,FontPosture.REGULAR,15));
			rightLabel.setFont(Font.font("ariel",FontWeight.BOLD,FontPosture.REGULAR,15));
			
			Button[] buttonArray = new Button[] {
				new Button(" Top 5 Words "),
				new Button("Top 10 Words"),
				new Button("Top 15 Words"),
				new Button("Top 20 Words")
			};
			
			Label[] wordLabelArray = new Label[] {
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label(),
					new Label()
			};
			
			pane.getChildren().add(leftLabel);
			pane.getChildren().add(rightLabel);
			pane.getChildren().addAll(buttonArray);
			pane.getChildren().addAll(wordLabelArray);
			
			//adding the layout with all of its components into the scene
			Scene scene = new Scene(pane,410,360);
			primaryStage.setTitle("Top X Words");
			primaryStage.setScene(scene);
			primaryStage.show();
			
			//handling button events for:
				//top 5 words
				buttonArray[0].setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent event) {
						labelChange(5, wordLabelArray);
					}
				});
				//top 10 words
				buttonArray[1].setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent event) {
						labelChange(10, wordLabelArray);
					}
				});
				//top 15 words
				buttonArray[2].setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent event) {
						labelChange(15, wordLabelArray);
					}
				});
				//top 20 words
				buttonArray[3].setOnAction(new EventHandler<ActionEvent>() {
					public void handle(ActionEvent event) {
						labelChange(20, wordLabelArray);
					}
				});
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Changes an array of labels to the first x entries from the sorted database.
	 * @param numEntries The number of entries to change.
	 * @param labelArray The array of labels to be replaced.
	 * @see #selectAll()
	 */
	public void labelChange(int numEntries, Label[] labelArray) {
		
		//clears the previous labels in case user has already hit a button
		for(int i = 0; i < 20; i++) {
			labelArray[i].setText("");
		}
		
		//sets label text to the most frequent words in order by pulling
		//from the database
		try {
			ResultSet database = selectAll();
			for(int i = 0; i < numEntries; i++) {
				database.next();
				labelArray[i].setText(database.getString("word") + ": " + database.getString("frequency"));
			}
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	
	/**
	 * Connects to the MySQL Workbench database.
	 * @return Connection
	 * @throws Exception
	 */
	public static Connection getConnection() throws Exception{
		
		try {
			String driver = "com.mysql.cj.jdbc.Driver";
			String url = "jdbc:mysql://localhost:3306/word_occurrences";
			String username = "root";
			String password = "Beefstew1997!";
			Class.forName(driver);
			
			Connection conn = DriverManager.getConnection(url,username,password);
			System.out.println("Connected");
			return conn;
			
		}catch(Exception e) {
			System.out.println(e);
		}
		
		return null;
	}
	
	/**
	 * Inserts (word,frequency) into the word_occurences table in MySQL Workbench.
	 * @param word A word from the poem.
	 * @param frequency How often word appears in the poem.
	 * @throws Exception
	 * @see #getConnection()
	 */
	public static void insert(String word, Integer frequency) throws Exception{
		Connection con = getConnection();
		try {
			PreparedStatement ins = con.prepareStatement("INSERT INTO word (word,frequency) VALUES ('"+ word +"',"+ frequency +")");
			ins.executeUpdate();
		}catch(Exception e) {
			System.out.println(e);
    	}finally {
			try {
				if(con!=null) {
					con.close();
				}
			}catch(Exception ex) {
				System.out.println(ex);
			}
			System.out.println("Insert Attempted");
		}
		
		
	}
	
	/**
	 * Selects all entries from the database and returns them in a result set
	 * sorted from most to least frequent.
	 * @return A ResultSet of all entries from the database in descending order of frequency.
	 * @throws Exception
	 * @see #getConnection()
	 */
	public static ResultSet selectAll() throws Exception{
		Connection con = getConnection();
		PreparedStatement select = con.prepareStatement("SELECT * FROM word ORDER BY frequency DESC;");
		
		ResultSet table = select.executeQuery();
		return table;
	}
	
	
}
