package com.example.controller;

import com.example.model.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

public class CalendarController {

    private ComboBox<String> viewSelector;
    private BorderPane calendarView;
    private LocalDate currentDate;
    private ResourceBundle resources;
    private ObservableList<Event> events;
    private CalendarView lastActiveView = CalendarView.MONTH;
    private Label dateInfoLabel;
    private Button previousButton;
    private Button nextButton;
    private Button addEventButton;
    private MainController mainController;
    private Region spacer;

    private void initializeButtons() {
        previousButton = new Button(resources.getString("calendar.previous"));
        nextButton = new Button(resources.getString("calendar.next"));
        dateInfoLabel = new Label(); // Upewnij się, że etykieta jest zainicjalizowana
        spacer = new Region(); // Upewnij się, że region jest zainicjalizowany
        addEventButton = new Button(resources.getString("event.addButton"));

        previousButton.setOnAction(event -> handlePrevious());
        nextButton.setOnAction(event -> handleNext());
        addEventButton.setOnAction(event -> handleAddEvent());
    }

    public void setViewSelector(ComboBox<String> viewSelector, ResourceBundle resources) {
        this.viewSelector = viewSelector;
        this.resources = resources;

        initializeButtons(); // Upewnij się, że przyciski są poprawnie inicjalizowane

        this.viewSelector.setItems(FXCollections.observableArrayList(
                resources.getString(CalendarView.MONTH.getResourceKey()),
                resources.getString(CalendarView.WEEK.getResourceKey()),
                resources.getString(CalendarView.DAY.getResourceKey())
        ));

        this.viewSelector.setValue(resources.getString(lastActiveView.getResourceKey()));
        this.viewSelector.setOnAction(event -> {
            CalendarView selectedView = CalendarView.fromLocalizedName(this.viewSelector.getValue(), resources);
            if (selectedView != null) {
                lastActiveView = selectedView;
                updateCalendarView(selectedView);
            }
        });

        HBox topControls = new HBox(10);
        topControls.getChildren().addAll(previousButton, nextButton, dateInfoLabel, spacer, viewSelector, addEventButton);
        topControls.setPadding(new Insets(0, 0, 10, 0));

        calendarView.setTop(topControls);
    }

    public void updateButtonLabels(ResourceBundle resources) {
        previousButton.setText(resources.getString("calendar.previous"));
        nextButton.setText(resources.getString("calendar.next"));
        addEventButton.setText(resources.getString("event.addButton"));
    }

    public void setCalendarView(BorderPane calendarView) {
        this.calendarView = calendarView;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public void setEvents(ObservableList<Event> events) {
        this.events = events;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public CalendarView getLastActiveView() {
        return lastActiveView;
    }

    public void handlePrevious() {
        if (currentDate != null) {
            switch (lastActiveView) {
                case MONTH:
                    currentDate = currentDate.minusMonths(1);
                    break;
                case WEEK:
                    currentDate = currentDate.minusWeeks(1);
                    break;
                case DAY:
                    currentDate = currentDate.minusDays(1);
                    break;
            }
            updateCalendarView(lastActiveView);
        }
    }

    public void handleNext() {
        if (currentDate != null) {
            switch (lastActiveView) {
                case MONTH:
                    currentDate = currentDate.plusMonths(1);
                    break;
                case WEEK:
                    currentDate = currentDate.plusWeeks(1);
                    break;
                case DAY:
                    currentDate = currentDate.plusDays(1);
                    break;
            }
            updateCalendarView(lastActiveView);
        }
    }

    private void handleAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fxml/event.fxml"), resources);
            Parent root = loader.load();

            EventController eventController = loader.getController();
            eventController.setMainController(mainController);

            Stage stage = new Stage();
            stage.setTitle(resources.getString("event.title"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(calendarView.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateCalendarView(CalendarView selectedView) {
        if (resources == null) {
            throw new IllegalStateException("Resources not set");
        }

        switch (selectedView) {
            case MONTH:
                displayMonthView();
                break;
            case WEEK:
                displayWeekView();
                break;
            case DAY:
                displayDayView();
                break;
        }
    }

    private void displayMonthView() {
        if (currentDate != null) {
            YearMonth yearMonth = YearMonth.from(currentDate);
            LocalDate firstDayOfMonth = yearMonth.atDay(1);
            LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

            dateInfoLabel.setText(firstDayOfMonth.getMonth().getDisplayName(TextStyle.FULL, resources.getLocale()) + " " + firstDayOfMonth.getYear());

            GridPane monthView = new GridPane();
            monthView.setGridLinesVisible(true);
            monthView.setMaxWidth(Double.MAX_VALUE);  // Zapobiegamy wystawaniu poza marginesy

            VBox monthContainer = new VBox(monthView);
            monthContainer.setPadding(new Insets(10));  // Dodanie paddingu

            VBox.setVgrow(monthContainer, Priority.ALWAYS);
            VBox.setVgrow(monthView, Priority.ALWAYS);

            // Stylowanie siatki kalendarza
            monthView.setStyle("-fx-border-color: transparent;");

            // Ustawienie ograniczonej wysokości dla wiersza z dniami tygodnia
            RowConstraints dayNameRow = new RowConstraints();
            dayNameRow.setMinHeight(25);
            dayNameRow.setMaxHeight(25);
            monthView.getRowConstraints().add(dayNameRow);

            // Dodaj nagłówki dni tygodnia zaczynając od niedzieli
            for (int i = 0; i < 7; i++) {
                Text dayName = new Text(LocalDate.of(2024, 1, i == 0 ? 7 : i)
                        .getDayOfWeek().getDisplayName(TextStyle.SHORT, resources.getLocale()));
                dayName.setStyle("-fx-font-size: 14px; -fx-fill: #333333;");
                dayName.setTextAlignment(TextAlignment.CENTER);
                GridPane.setHalignment(dayName, HPos.CENTER);
                monthView.add(dayName, i, 0);
            }

            // Ustawienia kolumn i wierszy
            for (int i = 0; i < 7; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setHgrow(Priority.ALWAYS);
                cc.setMinWidth(100);  // Minimalna szerokość kolumny dni tygodnia
                monthView.getColumnConstraints().add(cc);
            }

            int dayCounter = 1;
            int startDayColumn = firstDayOfMonth.getDayOfWeek().getValue();  // 1 to Poniedziałek, 7 to Niedziela
            startDayColumn = (startDayColumn == 7) ? 0 : startDayColumn;  // Przesuwamy niedzielę na pierwszy indeks

            int row = 1;

            for (int day = startDayColumn; day < 7; day++) {
                Text dayText = new Text(String.valueOf(dayCounter++));
                dayText.setTextAlignment(TextAlignment.CENTER);
                GridPane.setValignment(dayText, VPos.TOP); // Wycentrowanie numeru dnia do góry
                GridPane.setHalignment(dayText, HPos.CENTER);
                monthView.add(dayText, day, row);
            }

            row++;
            while (dayCounter <= lastDayOfMonth.getDayOfMonth()) {
                RowConstraints rc = new RowConstraints();
                rc.setVgrow(Priority.ALWAYS);

                for (int day = 0; day < 7; day++) {
                    if (dayCounter <= lastDayOfMonth.getDayOfMonth()) {
                        Text dayText = new Text(String.valueOf(dayCounter++));
                        dayText.setTextAlignment(TextAlignment.CENTER);
                        GridPane.setValignment(dayText, VPos.TOP); // Wycentrowanie numeru dnia do góry
                        GridPane.setHalignment(dayText, HPos.CENTER);
                        monthView.add(dayText, day, row);
                    }
                }

                // Dodajemy tylko te wiersze, które mają faktycznie dni
                if (dayCounter <= lastDayOfMonth.getDayOfMonth() + 7) {
                    monthView.getRowConstraints().add(rc);
                }

                row++;
            }

            calendarView.setCenter(monthContainer);
        }
    }

    private void displayWeekView() {
        if (currentDate != null) {
            LocalDate startOfWeek = currentDate.with(java.time.DayOfWeek.MONDAY);
            LocalDate endOfWeek = currentDate.with(java.time.DayOfWeek.SUNDAY);

            String weekInfo = startOfWeek.getDayOfMonth() + " " + startOfWeek.getMonth().getDisplayName(TextStyle.FULL, resources.getLocale()) + " " + startOfWeek.getYear() + " - " +
                    endOfWeek.getDayOfMonth() + " " + endOfWeek.getMonth().getDisplayName(TextStyle.FULL, resources.getLocale()) + " " + endOfWeek.getYear();
            dateInfoLabel.setText(weekInfo);

            GridPane weekView = new GridPane();
            weekView.setGridLinesVisible(true);
            weekView.setMaxWidth(Double.MAX_VALUE);  // Zapobiegamy wystawaniu poza marginesy

            // Dodanie paddingu za pomocą VBox
            VBox weekContainer = new VBox(weekView);

            ScrollPane scrollPane = new ScrollPane(weekContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPadding(new Insets(5, 5, 5, 5)); // Ustawienie paddingu na 5px ze wszystkich stron

            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            VBox.setVgrow(weekView, Priority.ALWAYS);

            // Stylowanie siatki kalendarza (usunięcie obramowania)
            weekView.setStyle("-fx-border-color: transparent;");

            // Ustawienie ograniczonej wysokości dla wiersza z dniami tygodnia
            RowConstraints dayNameRow = new RowConstraints();
            dayNameRow.setMinHeight(25);
            dayNameRow.setMaxHeight(25);
            dayNameRow.setPrefHeight(25);
            weekView.getRowConstraints().add(dayNameRow);

            // Kolumna na godziny z minimalną i maksymalną szerokością
            ColumnConstraints hourColumn = new ColumnConstraints();
            hourColumn.setMinWidth(50);  // Minimalna szerokość na godziny
            hourColumn.setMaxWidth(50);  // Maksymalna szerokość na godziny
            weekView.getColumnConstraints().add(hourColumn);

            // Kolumny na dni tygodnia, z równomiernym podziałem
            for (int i = 0; i < 7; i++) {
                ColumnConstraints dayColumn = new ColumnConstraints();
                dayColumn.setHgrow(Priority.ALWAYS);
                dayColumn.setMinWidth(100);  // Minimalna szerokość kolumny dni tygodnia
                weekView.getColumnConstraints().add(dayColumn);
            }

            // Wiersze: jedna godzina na wiersz (24 wiersze, od 0 do 23)
            for (int i = 0; i < 24; i++) {
                RowConstraints rc = new RowConstraints();
                rc.setVgrow(Priority.ALWAYS);
                rc.setPrefHeight(50);  // Ustawienie jednolitej preferowanej wysokości wierszy
                rc.setMinHeight(50);
                weekView.getRowConstraints().add(rc);
            }

            // Dodanie nagłówków dni tygodnia
            for (int day = 0; day < 7; day++) {
                LocalDate dayDate = startOfWeek.plusDays(day);
                Text dayHeader = new Text(dayDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + dayDate.getDayOfMonth());
                dayHeader.setStyle("-fx-font-size: 14px; -fx-fill: #333333;");
                GridPane.setHalignment(dayHeader, HPos.CENTER);
                weekView.add(dayHeader, day + 1, 0);
            }

            // Dodanie godzin w pierwszej kolumnie
            for (int hour = 0; hour < 24; hour++) {
                Text hourText = new Text(hour + ":00");
                hourText.setStyle("-fx-font-size: 12px; -fx-fill: #333333;"); // Stylizacja godzin
                weekView.add(hourText, 0, hour + 1);
            }

            // Dodanie wydarzeń w odpowiednich godzinach
            for (Event event : events) {
                LocalDateTime eventDateTime = event.getDateTime();
                LocalDate eventDate = eventDateTime.toLocalDate();
                if (!eventDate.isBefore(startOfWeek) && !eventDate.isAfter(endOfWeek)) {
                    int dayColumn = eventDate.getDayOfWeek().getValue() - 1; // Dzień tygodnia, poniedziałek jako 0
                    int hourRow = eventDateTime.getHour(); // Godzina wydarzenia

                    Label eventLabel = new Label(event.getTitle());
                    eventLabel.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-padding: 5px; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-wrap-text: true;");
                    weekView.add(eventLabel, dayColumn + 1, hourRow + 1); // Dodajemy +1, ponieważ pierwsza kolumna to godziny
                }
            }

            calendarView.setCenter(scrollPane);  // Ustawienie scrollPane jako głównego widoku
        }
    }

    private void displayDayView() {
        if (currentDate != null) {
            String dayInfo = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, resources.getLocale()) + ", " +
                    currentDate.getDayOfMonth() + " " + currentDate.getMonth().getDisplayName(TextStyle.FULL, resources.getLocale()) + " " + currentDate.getYear();
            dateInfoLabel.setText(dayInfo);

            GridPane dayView = new GridPane();
            dayView.setGridLinesVisible(true);

            VBox dayContainer = new VBox(dayView);

            ScrollPane scrollPane = new ScrollPane(dayContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setPadding(new Insets(5, 5, 5, 5)); // Ustawienie paddingu na 5px ze wszystkich stron

            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            VBox.setVgrow(dayView, Priority.ALWAYS);

            // Stylowanie siatki kalendarza
            dayView.setStyle("-fx-border-color: transparent;");

            // Kolumna na godziny z minimalną i maksymalną szerokością
            ColumnConstraints hourColumn = new ColumnConstraints();
            hourColumn.setMinWidth(50);  // Minimalna szerokość na godziny
            hourColumn.setMaxWidth(50);  // Maksymalna szerokość na godziny
            dayView.getColumnConstraints().add(hourColumn);

            // Kolumna na Eventy, zajmująca resztę przestrzeni
            ColumnConstraints eventColumn = new ColumnConstraints();
            eventColumn.setHgrow(Priority.ALWAYS);
            eventColumn.setMinWidth(100);  // Minimalna szerokość kolumny dla eventów
            dayView.getColumnConstraints().add(eventColumn);

            // Wiersze: jedna godzina na wiersz (24 wiersze, od 0 do 23)
            for (int i = 0; i < 24; i++) {
                RowConstraints rc = new RowConstraints();
                rc.setVgrow(Priority.ALWAYS);
                rc.setPrefHeight(50);  // Ustawienie jednolitej preferowanej wysokości wierszy
                rc.setMinHeight(50);
                dayView.getRowConstraints().add(rc);
            }

            // Dodanie godzin w pierwszej kolumnie
            for (int hour = 0; hour < 24; hour++) {
                Text hourText = new Text(hour + ":00");
                hourText.setStyle("-fx-font-size: 12px; -fx-fill: #333333;");
                dayView.add(hourText, 0, hour);
            }

            // Dodanie wydarzeń w odpowiednich godzinach
            for (Event event : events) {
                LocalDateTime eventDateTime = event.getDateTime();
                LocalDate eventDate = eventDateTime.toLocalDate();
                if (eventDate.equals(currentDate)) {
                    int hourRow = eventDateTime.getHour();

                    Label eventLabel = new Label(event.getTitle());
                    eventLabel.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-padding: 5px; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-wrap-text: true;");
                    dayView.add(eventLabel, 1, hourRow);
                }
            }

            calendarView.setCenter(scrollPane);  // Ustawienie scrollPane jako głównego widoku
        }
    }
}