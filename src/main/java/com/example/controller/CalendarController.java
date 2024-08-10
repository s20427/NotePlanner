package com.example.controller;

import com.example.model.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.time.LocalDate;
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

    public void setViewSelector(ComboBox<String> viewSelector, ResourceBundle resources) {
        this.viewSelector = viewSelector;
        this.resources = resources;
        this.viewSelector.setItems(FXCollections.observableArrayList(
                resources.getString("calendar.month"),
                resources.getString("calendar.week"),
                resources.getString("calendar.day")
        ));
        this.viewSelector.setValue(resources.getString("calendar.month"));
        this.viewSelector.setOnAction(event -> updateCalendarView(this.viewSelector.getValue()));
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

    public void handlePrevious() {
        if (currentDate != null) {
            switch (getLocalizedView(viewSelector.getValue())) {
                case "Month":
                    currentDate = currentDate.minusMonths(1);
                    break;
                case "Week":
                    currentDate = currentDate.minusWeeks(1);
                    break;
                case "Day":
                    currentDate = currentDate.minusDays(1);
                    break;
            }
            updateCalendarView(viewSelector.getValue());
        }
    }

    public void handleNext() {
        if (currentDate != null) {
            switch (getLocalizedView(viewSelector.getValue())) {
                case "Month":
                    currentDate = currentDate.plusMonths(1);
                    break;
                case "Week":
                    currentDate = currentDate.plusWeeks(1);
                    break;
                case "Day":
                    currentDate = currentDate.plusDays(1);
                    break;
            }
            updateCalendarView(viewSelector.getValue());
        }
    }

    public void updateCalendarView(String selectedView) {
        if (calendarView == null) {
            return;
        }

        String localizedView = getLocalizedView(selectedView);
        calendarView.setCenter(null); // Reset view
        switch (localizedView) {
            case "Month":
                displayMonthView();
                break;
            case "Week":
                displayWeekView();
                break;
            case "Day":
                displayDayView();
                break;
            default:
                throw new IllegalArgumentException("Unknown view: " + localizedView);
        }
    }

    private String getLocalizedView(String view) {
        if (view.equals(resources.getString("calendar.month"))) {
            return "Month";
        } else if (view.equals(resources.getString("calendar.week"))) {
            return "Week";
        } else if (view.equals(resources.getString("calendar.day"))) {
            return "Day";
        }
        return view;
    }

    private void showDialog(String message) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setContentText(message);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.OK);
        dialog.showAndWait();
    }

    private void displayMonthView() {
        if (currentDate != null) {
            YearMonth yearMonth = YearMonth.from(currentDate);
            LocalDate firstDayOfMonth = yearMonth.atDay(1);
            LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

            GridPane monthView = new GridPane();
            monthView.setGridLinesVisible(true);
            monthView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            BorderPane.setMargin(monthView, new javafx.geometry.Insets(10));
            VBox.setVgrow(monthView, Priority.ALWAYS);

            // Kolumny dla dni tygodnia
            for (int i = 0; i < 7; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(100 / 7.0);
                monthView.getColumnConstraints().add(cc);
            }

            // Wiersze dla tygodni miesiąca
            for (int i = 0; i < 6; i++) {
                RowConstraints rc = new RowConstraints();
                rc.setVgrow(Priority.ALWAYS);
                rc.setPercentHeight(100 / 6.0);
                monthView.getRowConstraints().add(rc);
            }

            int dayCounter = 1;

            // Nagłówki dni tygodnia (poniedziałek - niedziela)
            for (int day = 0; day < 7; day++) {
                Text dayName = new Text(LocalDate.of(2023, 1, day + 2)
                        .getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
                monthView.add(dayName, day, 0);
            }

            // Dni miesiąca
            int row = 1;
            for (int i = 0; i < (firstDayOfMonth.getDayOfWeek().getValue() % 7); i++) {
                monthView.add(new Text(""), i, row);
            }

            for (int day = firstDayOfMonth.getDayOfWeek().getValue() % 7; day < 7; day++) {
                Text dayText = new Text(String.valueOf(dayCounter++));
                monthView.add(dayText, day, row);
                addEventHandlers(dayText, LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), dayCounter - 1));
            }

            row++;
            while (dayCounter <= lastDayOfMonth.getDayOfMonth()) {
                for (int day = 0; day < 7; day++) {
                    if (dayCounter <= lastDayOfMonth.getDayOfMonth()) {
                        Text dayText = new Text(String.valueOf(dayCounter++));
                        monthView.add(dayText, day, row);
                        addEventHandlers(dayText, LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), dayCounter - 1));
                    }
                }
                row++;
            }

            calendarView.setCenter(monthView);
        }
    }

    private void displayWeekView() {
        if (currentDate != null) {
            LocalDate startOfWeek = currentDate.with(java.time.DayOfWeek.MONDAY);
            GridPane weekView = new GridPane();
            weekView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            weekView.setGridLinesVisible(true);
            BorderPane.setMargin(weekView, new javafx.geometry.Insets(10));
            VBox.setVgrow(weekView, Priority.ALWAYS);

            for (int i = 0; i < 7; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(100 / 7.0);
                weekView.getColumnConstraints().add(cc);
            }

            for (int day = 0; day < 7; day++) {
                LocalDate dayDate = startOfWeek.plusDays(day);
                weekView.add(new Text(dayDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + dayDate.getDayOfMonth()), day, 0);
            }

            calendarView.setCenter(weekView);
        }
    }

    private void displayDayView() {
        if (currentDate != null) {
            VBox dayView = new VBox();
            dayView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
            BorderPane.setMargin(dayView, new javafx.geometry.Insets(10));
            VBox.setVgrow(dayView, Priority.ALWAYS);
            dayView.getChildren().add(new Text(currentDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + currentDate.getDayOfMonth()));
            calendarView.setCenter(dayView);
        }
    }

    private void addEventHandlers(Text dayText, LocalDate date) {
        dayText.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showDialog("Dodaj wydarzenie na dzień " + dayText.getText());
            }
        });

        // Wyświetlanie wydarzeń w odpowiednim dniu
        for (Event event : events) {
            if (event.getDateTime().toLocalDate().equals(date)) {
                dayText.setText(dayText.getText() + "\n" + event.getTitle());
            }
        }
    }
}
