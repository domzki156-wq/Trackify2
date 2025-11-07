package oop.barcelo.trackify27.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import oop.barcelo.trackify27.db.MongoDBConnection;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.time.format.DateTimeFormatter;

public class TransactionFormController {
    @FXML private DatePicker datePicker;
    @FXML private TextField customerField;
    @FXML private TextField itemField;
    @FXML private TextField paymentField;
    @FXML private TextField revenueField;
    @FXML private TextField costField;
    @FXML private TextArea notesArea;
    @FXML private Button saveButton;

    private Runnable onSaved = null;

    public void setOnSaved(Runnable onSaved) { this.onSaved = onSaved; }

    @FXML
    private void initialize() {
        datePicker.setValue(java.time.LocalDate.now());
    }

    @FXML
    private void onSave() {
        String date = datePicker.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String customer = customerField.getText();
        String item = itemField.getText();
        String payment = paymentField.getText();
        double revenue = parseDoubleSafe(revenueField.getText());
        double cost = parseDoubleSafe(costField.getText());
        String notes = notesArea.getText();

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> coll = db.getCollection("transactions");

        Document doc = new Document();
        doc.append("date", date);
        doc.append("customer", customer);
        doc.append("item", item);
        doc.append("payment", payment);
        doc.append("revenue", revenue);
        doc.append("cost", cost);
        doc.append("notes", notes);

        coll.insertOne(doc);

        if (onSaved != null) onSaved.run();
    }

    private double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
    }
}
