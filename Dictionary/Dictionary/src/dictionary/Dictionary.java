package dictionary;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class Dictionary extends Thread{
    private DictionaryGUI frame;
    protected ArrayList<WordData> wordList = new ArrayList<>();
    
    
    public Dictionary(DictionaryGUI frame) {
        this.frame = frame;
    }
    
    
    protected static class WordData{
        private String title;
        private String deffinition;
        
        public WordData(String title, String deffinition){
            this.title = title;
            this.deffinition = deffinition;
        }
        public String getTitle() {
            return title;
        }

        public String getDeffinition() {
            return deffinition;
        }
    }
    
    public void CurrencyTableUpdater(ArrayList<WordData> wordList) {
        java.awt.EventQueue.invokeLater(() -> {
            DefaultTableModel model = (DefaultTableModel) frame.jTable.getModel();
            model.setRowCount(0);
            if(wordList!=null){
                for (WordData wordData : wordList) {
                    Object[] rowData = { wordData.getTitle(), wordData.getDeffinition()};
                    model.addRow(rowData);
                }
            }
        });
    }
    private ArrayList<WordData> GetWordfromServer(String searchTerm){
        try {
            ArrayList<WordData> wordList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            
            String apiUrl = "https://pl.wikipedia.org/w/rest.php/v1/search/page?q=" + searchTerm;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader apiReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            
            while ((line = apiReader.readLine()) != null) {
                response.append(line);
            }
            apiReader.close();
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(response.toString()).getAsJsonObject();
            JsonArray pagesArray = jsonObject.getAsJsonArray("pages");

            if (pagesArray != null) {
                for (int i = 0; i < 5; i++) {
                    JsonObject page = pagesArray.get(i).getAsJsonObject();
                    String excerpt = page.get("excerpt").getAsString();
                    String title = page.get("title").getAsString();
                    excerpt = excerpt.replaceAll("(?i)<span class=\"searchmatch\">" + searchTerm + "</span>", "");
                    wordList.add(new WordData(title, excerpt));
                }
            }
            return wordList;
        } catch (IOException ex) {
            int option = JOptionPane.showOptionDialog(null,
                    "Wystąpił błąd podczas próby połączenia się z serwerem.\nCzy chcesz spróbować ponownie?",
                    "Błąd połączenia", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
                    new Object[] { "Ponów", "Zamknij" }, "Ponów");

            if (option == JOptionPane.NO_OPTION) {
                System.exit(0);
                return null;
            } else {
                GetWordfromServer(searchTerm);
            }
        }
        return null;
    } 
    
    public void search(){
        CurrencyTableUpdater(GetWordfromServer(frame.jSearch.getText()));
    }
    
    @Override
    public void run() {
      search();
    }

}

   