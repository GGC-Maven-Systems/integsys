package ph.com.guanzongroup.integsys.views.child;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class APListController implements Initializable {

    @FXML
    private AnchorPane apRoot;

    @FXML
    private Label lblDisplay;

    @FXML
    private Button btnRemove;

    private Runnable onRemove;
    private Runnable onSelect;
    private boolean selected = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnRemove.setOnAction((e) -> {
            e.consume(); // prevent the remove click from also triggering select
            if (onRemove != null) {
                onRemove.run();
            }
        });

        apRoot.setOnMouseClicked((e) -> {
            toggleSelected();
            if (onSelect != null) {
                onSelect.run();
            }
        });
    }

    public void setDisplayText(String text) {
        lblDisplay.setText(text);
    }

    public void setOnRemove(Runnable callback) {
        this.onRemove = callback;
    }

    public void setOnSelect(Runnable callback) {
        this.onSelect = callback;
    }

    public void setSelected(boolean value) {
        selected = value;
        if (selected) {
            if (!apRoot.getStyleClass().contains("chip-selected")) {
                apRoot.getStyleClass().add("chip-selected");
            }
        } else {
            apRoot.getStyleClass().remove("chip-selected");
        }
    }

    public boolean isSelected() {
        return selected;
    }

    private void toggleSelected() {
        setSelected(!selected);
    }
}
