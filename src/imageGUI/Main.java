package imageGUI;

import io.indico.Indico;
import io.indico.api.results.IndicoResult;
import io.indico.api.utils.IndicoException;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Main extends Application {

    private Stage stage;
    private ObservableList<String> photosList = FXCollections.observableArrayList();
    private final String[] EXTENSIONS = new String[]{"jpg", "png"}; //TODO: check which extensions are handled by indico
    private Map<String, Object> parameters = new HashMap<>();
    private ObservableList<PieChart.Data> pieChartData;

    @FXML
    private ListView<String> photosView;
    @FXML
    private ImageView imageView;
    @FXML
    private PieChart pieChart;
    @FXML
    private Label fileNameMessage;

    @FXML
    private void changeDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory");
        directoryChooser.setInitialDirectory(new File("C:\\Users\\ProBook\\Desktop\\fotoSort"));
        File directory = directoryChooser.showDialog(stage);

        for (File image : directory.listFiles()) {      //TODO: NullPointerException
            for (final String extension : EXTENSIONS) {
                if (image.getPath().endsWith(extension)) {
                    photosList.add(image.getPath()); //TODO: zeby pokazywalo getName() a nie getPath() -> trzeba mapy z paths i names
                }
            }
        }
        photosView.setItems(photosList);
    }

    @FXML
    private void displayData() throws IndicoException, IOException {
        //display photo
        fileNameMessage = new Label("Filename says that it is ");
        imageView.setImage(new Image("file:" + photosView.getSelectionModel().getSelectedItem()));

        //display chart
        Indico indico = new Indico("1026d4b1cea8318b801f45df22bcb2b5");
        parameters.put("threshold", 0.01);   //only return category with likelihood greater than 0.01
        parameters.put("top_n", 7);         //only return 7 of the most likely category
        IndicoResult single = indico.imageRecognition.predict(photosView.getSelectionModel().getSelectedItem(), parameters);
        Map<String, Double> result = single.getImageRecognition();

        pieChartData = FXCollections.observableArrayList();

        double othersValue = 0.0;

        for (Map.Entry<String, Double> entry : result.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " "
                    + (Math.round(entry.getValue() * 100) + "%"), entry.getValue()));
            othersValue += entry.getValue();
        }
        pieChartData.add(new PieChart.Data("Others " + Math.round(othersValue * 100) + "%", othersValue));

        pieChart.setData(pieChartData);
        pieChart.setTitle("What is on the picture?");

    }

    @FXML
    private void reset() {
        imageView.setImage(null);
        pieChartData.clear();
        pieChart.setData(null);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent layout = FXMLLoader.load(getClass().getResource("IndicoGUI.fxml"));
        stage = primaryStage;
        stage.setTitle("IndicoGUI");

        stage.setScene(new Scene(layout, 1000, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
