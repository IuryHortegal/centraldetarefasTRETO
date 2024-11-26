package com.example.tasknetwork;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TaskNetwork extends Application {

    static class Task {
        String title;
        List<String> observations;

        public Task(String title) {
            this.title = title;
            this.observations = new ArrayList<>();
        }
    }

    static class Link {
        private final StringProperty name;
        private final StringProperty url;

        public Link(String name, String url) {
            this.name = new SimpleStringProperty(name);
            this.url = new SimpleStringProperty(url);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getUrl() {
            return url.get();
        }

        public void setUrl(String url) {
            this.url.set(url);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty urlProperty() {
            return url;
        }
    }

    private final List<Task> tasks = new ArrayList<>();
    private final List<Circle> icons = new ArrayList<>();
    private final Pane circleContainer = new Pane(); // Contêiner para círculos
    private final VBox observationBox = new VBox(10); // Contêiner para observações
    private final List<Link> links = new ArrayList<>(); // Lista para nomes e links
    private final TextField searchField = new TextField(); // Campo de pesquisa

    @Override
    public void start(Stage primaryStage) {
        // Layout principal
        BorderPane root = new BorderPane();

        // Cabeçalho
        StackPane header = new StackPane();
        Label headerLabel = new Label("Central de Micro Tarefas");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: green;");
        header.getChildren().add(headerLabel);
        root.setTop(header);

        // Corpo principal
        circleContainer.setPrefSize(600, 600);
        root.setCenter(circleContainer);

        // Observação no lado direito
        observationBox.setPrefWidth(200);
        observationBox.setStyle("-fx-padding: 10; -fx-background-color: #f4f4f4; -fx-border-color: #ddd;");
        root.setRight(observationBox);

        // Tabela no lado esquerdo
        VBox leftBox = createLinkTable();
        leftBox.setPrefWidth(250);
        leftBox.setStyle("-fx-padding: 10; -fx-background-color: #f4f4f4; -fx-border-color: #ddd;");
        root.setLeft(leftBox);

        // Controles para adicionar/remover tarefas e pesquisar
        HBox controls = new HBox(10);
        TextField taskField = new TextField();
        taskField.setPromptText("Título da Tarefa");
        Button addButton = new Button("Adicionar Tarefa");
        Button removeButton = new Button("Remover Última");
        searchField.setPromptText("Pesquisar Observação");
        Button searchButton = new Button("Pesquisar");
        controls.getChildren().addAll(taskField, addButton, removeButton, searchField, searchButton);
        root.setBottom(controls);

        // Adicionar tarefa
        addButton.setOnAction(e -> {
            String title = taskField.getText().trim();
            if (!title.isEmpty()) {
                tasks.add(new Task(title));
                taskField.clear();
                drawTasksInCircle(); // Redesenha as tarefas
            }
        });

        // Remover última tarefa
        removeButton.setOnAction(e -> {
            if (!tasks.isEmpty()) {
                tasks.remove(tasks.size() - 1);
                drawTasksInCircle(); // Redesenha as tarefas
            }
        });

        // Pesquisar palavra nas observações
        searchButton.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                searchObservations(query);
            } else {
                resetHighlighting(); // Restaura os destaques
            }
        });

        // Inicializar tarefas e links
        tasks.add(new Task("Gerenciar E-mails"));
        tasks.add(new Task("Organizar Agenda"));
        tasks.add(new Task("Atualizar Relatórios"));
        tasks.add(new Task("Manutenção"));
        tasks.add(new Task("Reunião de Equipe"));
        tasks.add(new Task("Atender Chamadas"));
        tasks.add(new Task("Monitorar Sistema"));

        links.add(new Link("Google", "https://www.google.com"));
        links.add(new Link("GitHub", "https://www.github.com"));

        // Desenhar tarefas no início
        drawTasksInCircle();

        // Configuração da cena
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Central de Micro Tarefas");
        primaryStage.show();
    }

    // Criar tabela para nomes e links no canto esquerdo
    private VBox createLinkTable() {
        TableView<Link> table = new TableView<>();

        // Coluna Nome
        TableColumn<Link, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        // Coluna URL
        TableColumn<Link, String> urlColumn = new TableColumn<>("Link");
        urlColumn.setCellValueFactory(cellData -> cellData.getValue().urlProperty());

        // Campo para adicionar novo link
        TextField nameField = new TextField();
        nameField.setPromptText("Nome");
        TextField urlField = new TextField();
        urlField.setPromptText("Link");
        Button addLinkButton = new Button("Adicionar Link");
        addLinkButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String url = urlField.getText().trim();
            if (!name.isEmpty() && !url.isEmpty()) {
                links.add(new Link(name, url));
                table.getItems().setAll(links); // Atualiza a tabela
                nameField.clear();
                urlField.clear();
            }
        });

        // Botão para remover link selecionado
        Button removeLinkButton = new Button("Remover Link Selecionado");
        removeLinkButton.setOnAction(e -> {
            Link selectedLink = table.getSelectionModel().getSelectedItem();
            if (selectedLink != null) {
                links.remove(selectedLink);
                table.getItems().setAll(links); // Atualiza a tabela
            }
        });

        // Configurar a tabela
        table.getColumns().addAll(nameColumn, urlColumn);
        table.setPrefHeight(400);
        table.getItems().setAll(links);

        VBox linkControls = new VBox(10, nameField, urlField, addLinkButton, removeLinkButton, table);
        return linkControls;
    }

    // Pesquisar palavra nas observações
    private void searchObservations(String query) {
        resetHighlighting(); // Limpar destaques anteriores

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            boolean taskHighlighted = false;

            for (String observation : task.observations) {
                if (observation.toLowerCase().contains(query.toLowerCase())) {
                    taskHighlighted = true;
                    highlightObservation(i, observation, query); // Destacar a observação
                }
            }

            if (taskHighlighted) {
                icons.get(i).setFill(Color.RED); // Destacar o círculo da tarefa
            }
        }
    }

    private void highlightObservation(int taskIndex, String observation, String query) {
        handleTaskClick(taskIndex); // Exibir a tarefa correspondente
        observationBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> (HBox) node)
                .forEach(hbox -> {
                    Label label = (Label) hbox.getChildren().get(0);
                    if (label.getText().contains(observation)) {
                        label.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                });
    }

    private void resetHighlighting() {
        icons.forEach(circle -> circle.setFill(Color.WHITE)); // Restaurar cor dos círculos
        drawTasksInCircle(); // Redesenhar a interface
    }

    private void drawTasksInCircle() {
        circleContainer.getChildren().clear();
        icons.clear();

        double centerX = circleContainer.getPrefWidth() / 2;
        double centerY = circleContainer.getPrefHeight() / 2;
        double radius = 200;

        double angleStep = 360.0 / tasks.size();
        for (int i = 0; i < tasks.size(); i++) {
            double angle = Math.toRadians(i * angleStep);
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            Circle circle = new Circle(x, y, 30, Color.WHITE);
            circle.setStroke(Color.GREEN);

            int index = i; // Índice da tarefa
            circle.setOnMouseClicked(event -> handleTaskClick(index));

            Text text = new Text(tasks.get(i).title);
            text.setX(x - 30);
            text.setY(y - 40);
            text.setFill(Color.BLACK);

            circleContainer.getChildren().addAll(circle, text);
            icons.add(circle);
        }

        drawConnections();
    }

    private void drawConnections() {
        for (int i = 0; i < icons.size(); i++) {
            Circle circleA = icons.get(i);
            for (int j = i + 1; j < icons.size(); j++) {
                Circle circleB = icons.get(j);

                Line line = new Line(
                        circleA.getCenterX(), circleA.getCenterY(),
                        circleB.getCenterX(), circleB.getCenterY()
                );
                line.setStroke(Color.LIGHTGRAY);
                line.setStrokeWidth(1);
                circleContainer.getChildren().add(line);
            }
        }
    }

    private void handleTaskClick(int taskIndex) {
        Task clickedTask = tasks.get(taskIndex);

        observationBox.getChildren().clear();
        Label titleLabel = new Label("Tarefa: " + clickedTask.title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        observationBox.getChildren().add(titleLabel);

        for (int i = 0; i < clickedTask.observations.size(); i++) {
            String observation = clickedTask.observations.get(i);

            HBox observationItem = new HBox(5);
            Label observationLabel = new Label("- " + observation);
            Button removeButton = new Button("Remover");
            int observationIndex = i;

            removeButton.setOnAction(e -> {
                clickedTask.observations.remove(observationIndex);
                handleTaskClick(taskIndex);
            });

            observationItem.getChildren().addAll(observationLabel, removeButton);
            observationBox.getChildren().add(observationItem);
        }

        TextField newObservationField = new TextField();
        newObservationField.setPromptText("Nova Observação");
        Button addObservationButton = new Button("Adicionar Observação");

        addObservationButton.setOnAction(e -> {
            String newObservation = newObservationField.getText().trim();
            if (!newObservation.isEmpty()) {
                clickedTask.observations.add(newObservation);
                newObservationField.clear();
                handleTaskClick(taskIndex);
            }
        });

        observationBox.getChildren().addAll(newObservationField, addObservationButton);
    }

    public static void main(String[] args) {
        launch(args);
    }
}