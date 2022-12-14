package com.silviotmalmeida.app.javafx;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import com.silviotmalmeida.app.JavaFXApplication;
import com.silviotmalmeida.app.entities.Department;
import com.silviotmalmeida.app.javafx.listeners.DataChangeListener;
import com.silviotmalmeida.app.javafx.utils.Alerts;
import com.silviotmalmeida.app.javafx.utils.Utils;
import com.silviotmalmeida.app.services.DepartmentService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;

// controlador da tela DepartmentList.fxml
// tem o papel de listener para a interface DataChangeListener, ou seja, é a classe que observa o evento emitido pelo DepartmentFormController
@Controller
@FxmlView("DepartmentList.fxml")
public class DepartmentListController implements Initializable, DataChangeListener {

    // injetando o contexto do spring, para permitir as demais injeções nos
    // controllers chamados a partir deste
    @Autowired
    private ApplicationContext springContext;

    // injetando o service da entidade Department
    @Autowired
    private DepartmentService service;

    // a anotação @FXML faz a ligação com os elementos da view
    @FXML
    private TableView<Department> tableViewDepartment;

    @FXML
    private TableColumn<Department, Integer> tableColumnId;

    @FXML
    private TableColumn<Department, String> tableColumnName;

    @FXML
    private TableColumn<Department, Department> tableColumnEDIT;

    @FXML
    private TableColumn<Department, Department> tableColumnREMOVE;

    @FXML
    private Button btNew;

    // atributo para armazenar a lista de departamentos a ser renderizada na tabela
    private ObservableList<Department> obsList;

    // referente método disparado pelo evento onAction do btNew
    @FXML
    public synchronized void onBtNewAction(ActionEvent event) {

        // obtendo o stage pai
        Stage parentStage = Utils.currentStage(event);

        // declarando um Department vazio, pois trata-se de create
        Department obj = new Department();

        // criando o formulário de create
        createDialogForm(obj, "DepartmentForm.fxml", parentStage);
    }

    // sobrecarregando o método de configuração da tela para inicialização
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // inicializando a tabela
        initializeNodes();
    }

    // método auxiliar responsável por configurar a tabela
    private void initializeNodes() {

        // definindo a ligação das colunas da tabela com os atributos da entidade
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));

        // obtendo o stage a partir da cena
        Stage stage = (Stage) JavaFXApplication.getMainScene().getWindow();

        // ajustando a altura da tabela para acompanhar a janela
        tableViewDepartment.prefHeightProperty().bind(stage.heightProperty());
    }

    // método responsável por popular a lista de departamentos a ser renderizada na
    // tabela
    public void updateTableView() {

        // se o service não estiver injetado, lança uma exceção
        if (service == null) {
            throw new IllegalStateException("Service was null");
        }

        // obtendo os dados do BD
        List<Department> list = this.service.findAll();

        // renderizando os dados na tabela
        this.obsList = FXCollections.observableArrayList(list);
        tableViewDepartment.setItems(this.obsList);
        initEditButtons();
        initRemoveButtons();
    }

    // método que vai criar uma tela diálogo com formulário
    // o atributo synchronized torna a execução sícrona nneste método
    private synchronized void createDialogForm(Department obj, String absoluteName, Stage parentStage) {

        // tratando as exceções
        try {

            // carregando a tela informada e incluindo o contexto do spring
            // será criado um
            FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
            loader.setControllerFactory(springContext::getBean);
            Pane pane = loader.load();

            // obtendo o controller da tela
            DepartmentFormController controller = loader.getController();

            // injetando o entidade Department e atualizando os dados do formulário, caso
            // existam
            controller.setDepartment(obj);
            controller.updateFormData();

            // increvendo este objeto como listener do controller para viabilizar
            // atualização dos dados da tabela
            controller.subscribeDataChangeListener(this);

            // criando uma nova janela e configurando como modal
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Enter Department data");
            dialogStage.setScene(new Scene(pane));
            dialogStage.setResizable(false);
            dialogStage.initOwner(parentStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.showAndWait();

        }
        // em caso de exceção, exibe um alerta
        catch (IOException e) {
            e.printStackTrace();
            Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(),
                    AlertType.ERROR);
        }
    }

    // sobrescrevendo o método a ser disparado pelo subject do DataChangeListener
    @Override
    public void onDataChanged() {
        updateTableView();
    }

    // método auxiliar responsável pela criação dos botões de editar
    // registros nas linhas da tabela
    private void initEditButtons() {

        // para cada linha será gerado um botão
        tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        tableColumnEDIT.setCellFactory(param -> new TableCell<Department, Department>() {

            // definindo o label do botão
            private final Button button = new Button("edit");

            // definindo o evento do botão
            @Override
            protected void updateItem(Department obj, boolean empty) {
                super.updateItem(obj, empty);
                if (obj == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(button);
                button.setOnAction(
                        event -> createDialogForm(obj, "DepartmentForm.fxml",
                                Utils.currentStage(event)));
            }
        });
    }

    // método auxiliar responsável pela criação dos botões de remover
    // registros nas linhas da tabela
    private void initRemoveButtons() {

        // para cada linha será gerado um botão
        tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        tableColumnREMOVE.setCellFactory(param -> new TableCell<Department, Department>() {

            // definindo o label do botão
            private final Button button = new Button("remove");

            // definindo o evento do botão
            @Override
            protected void updateItem(Department obj, boolean empty) {
                super.updateItem(obj, empty);
                if (obj == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(button);
                button.setOnAction(event -> removeEntity(obj));
            }
        });
    }

    // método auxiliar para lançar o alert de confirmação de exclusão,
    // bem como responsável por remover o registro
    private void removeEntity(Department obj) {

        // exibindo o alert de confirmação
        Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Are you sure to delete?");

        // se houver confirmação
        if (result.get() == ButtonType.OK) {

            // se o service não estiver injetado, lança uma exceção
            if (service == null) {
                throw new IllegalStateException("Service was null");
            }

            // tatando exceções
            try {

                // removendo o registro
                service.delete(obj.getId());

                // atualizando os dados da tabela
                updateTableView();

            }
            // caso ocorra exceção, exibe um alerta
            catch (Exception e) {
                Alerts.showAlert("Error removing object", null, e.getMessage(),
                        AlertType.ERROR);
            }
        }
        // senão
        else {

            // atualizando os dados da tabela
            updateTableView();
        }
    }
}