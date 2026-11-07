package ph.com.guanzongroup.integsys.views.child;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class APStatusController implements Initializable {

    @FXML
    private AnchorPane apMainAnchor;
    @FXML
    private Label lblDisplay;
    @FXML
    private Button btnAction;
    @FXML
    private FontAwesomeIconView iconAction;

    private boolean pbIsAddNew = true;   // true = trash/delete behavior
    private boolean pbIsActive = true;   // meaningful only when pbIsAddNew == false
    private boolean pbSelected = false;

    private Runnable poOnRemove;
    private Consumer<Boolean> poOnToggleActive;
    private Runnable poOnSelect;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Same base look as APList chips
        if (!apMainAnchor.getStyleClass().contains("chip-container")) {
            apMainAnchor.getStyleClass().add("chip-container");
        }

        btnAction.setOnAction(e -> {
            e.consume();
            handleAction();
        });

        apMainAnchor.setOnMouseClicked(e -> {
            setSelected(!pbSelected);
            if (poOnSelect != null) {
                poOnSelect.run();
            }
        });
    }

    public void setDisplayText(String text) {
        lblDisplay.setText(text);
    }

    public void setMode(boolean isAddNew) {
        this.pbIsAddNew = isAddNew;
        refreshIcon();
        refreshHighlight();
    }

    public void setActive(boolean isActive) {
        this.pbIsActive = isActive;
        refreshIcon();
        refreshHighlight();
    }

    private void refreshIcon() {
        if (pbIsAddNew) {
            iconAction.setGlyphName("TRASH");
        } else {
            iconAction.setGlyphName(pbIsActive ? "CHECK" : "CLOSE");
        }
    }

    private void refreshHighlight() {
        apMainAnchor.setStyle(""); // never use inline styles
        if (!pbIsAddNew && !pbIsActive) {
            if (!apMainAnchor.getStyleClass().contains("chip-inactive")) {
                apMainAnchor.getStyleClass().add("chip-inactive");
            }
        } else {
            apMainAnchor.getStyleClass().remove("chip-inactive");
        }
    }

    private void handleAction() {
        if (pbIsAddNew) {
            if (poOnRemove != null) {
                poOnRemove.run();
            }
        } else {
            boolean lbNewState = !pbIsActive;
            setActive(lbNewState);
            if (poOnToggleActive != null) {
                poOnToggleActive.accept(lbNewState);
            }
        }
    }

    public void setOnRemove(Runnable action) {
        this.poOnRemove = action;
    }

    public void setOnToggleActive(Consumer<Boolean> action) {
        this.poOnToggleActive = action;
    }

    public void setOnSelect(Runnable action) {
        this.poOnSelect = action;
    }

    public void setSelected(boolean selected) {
        this.pbSelected = selected;
        if (selected) {
            if (!apMainAnchor.getStyleClass().contains("chip-selected")) {
                apMainAnchor.getStyleClass().add("chip-selected");
            }
        } else {
            apMainAnchor.getStyleClass().remove("chip-selected");
        }
    }
}
