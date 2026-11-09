package cl.duoc.viviendas.controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import cl.duoc.viviendas.model.Vivienda;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Casa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import cl.duoc.viviendas.repository.ViviendaRepository;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Currency;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class ViviendaController {

    // Almacen de las viviendas registradas
    private ObservableList<Vivienda> listaViviendas =
            FXCollections.observableArrayList();

    // Lista filtrada
    private FilteredList<Vivienda> listaFiltrada;

    // Objeto encargado de guardar y cargar las viviendas
    private ViviendaRepository repository = new ViviendaRepository();

    // Elementos del formulario
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtSuperficie;
    @FXML
    private TextField txtHabitaciones;
    @FXML
    private TextField txtPiso;
    @FXML
    private ComboBox<String> cmbGastoComun;
    @FXML
    private ComboBox<String> cmbEstacionamiento;
    @FXML
    private ComboBox<String> cmbPatio;

    // Buscador
    @FXML
    private TextField txtBuscar;

    // Botones
    @FXML
    private Button btnRegistrar;
    @FXML
    private Button btnEliminar;


    // Tabla
    @FXML
    private TableView<Vivienda> tblViviendas;


    // Columnas de la tabla
    @FXML
    private TableColumn<Vivienda, String> colTipo;
    @FXML
    private TableColumn<Vivienda, String> colCodigo;
    @FXML
    private TableColumn<Vivienda, Number> colSuperficie;
    @FXML
    private TableColumn<Vivienda, Number> colHabitaciones;
    @FXML
    private TableColumn<Vivienda, Number> colCostoArriendo;


    // Mensajes
    @FXML
    private Label lblMensaje;

    @FXML
    private void initialize() {
        cmbTipo.getItems().addAll("Departamento", "Casa");
        cmbGastoComun.getItems().addAll("Sí", "No");
        cmbEstacionamiento.getItems().addAll("Sí", "No");
        cmbPatio.getItems().addAll("Sí", "No");
        cmbTipo.setValue("Departamento");
        cmbGastoComun.setValue("No");
        cmbEstacionamiento.setValue("No");
        cmbPatio.setValue("No");
        cambiarTipo();
        // Configuracion de columnas de la tabla
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colSuperficie.setCellValueFactory(new PropertyValueFactory<>("superficie"));
        colHabitaciones.setCellValueFactory(new PropertyValueFactory<>("habitaciones"));
        colCostoArriendo.setCellValueFactory(
                celda -> new ReadOnlyDoubleWrapper(
                        celda.getValue().calcularCostoArriendo()
                )
        );
        listaFiltrada = new FilteredList<>(listaViviendas, p -> true);

        txtBuscar.textProperty().addListener((observable, valorAnterior, nuevoValor) -> {
            aplicarFiltro(nuevoValor);
        });

        tblViviendas.setItems(listaFiltrada);
        colCostoArriendo.setCellFactory(column -> new TableCell<Vivienda, Number>() {

            @Override
            protected void updateItem(Number costo, boolean empty) {
                super.updateItem(costo, empty);
                if (empty || costo == null) {
                    setText(null);
                } else {
                    NumberFormat formato =
                            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"));
                    formato.setCurrency(Currency.getInstance("CLP"));
                    formato.setMaximumFractionDigits(0);
                    setText(formato.format(costo.doubleValue()));
                }
            }
        });

        // Cargar viviendas guardadas anteriormente
        try {
            listaViviendas.addAll(repository.cargarTodas());
        } catch (IOException e) {
            lblMensaje.setText(
                    "Error al cargar los archivos de viviendas: " + e.getMessage()
            );
        }
    }

    private void aplicarFiltro(String criterio) {
        String busqueda = criterio.toLowerCase().trim();
        listaFiltrada.setPredicate(vivienda -> {
            if (busqueda.isEmpty()) {
                return true;
            }
            String codigo = vivienda.getCodigo().toLowerCase();
            String tipo = vivienda.getTipo().toLowerCase();
            return codigo.contains(busqueda) || tipo.contains(busqueda);
        });
    }

    @FXML
    private void cambiarTipo() {
        boolean esDepartamento = "Departamento".equals(cmbTipo.getValue());
        txtPiso.setDisable(!esDepartamento);
        cmbGastoComun.setDisable(!esDepartamento);
        cmbEstacionamiento.setDisable(!esDepartamento);
        cmbPatio.setDisable(esDepartamento);
    }

    @FXML
    private void registrarVivienda() {

        try {
            // Obtener datos comunes del formulario
            String codigo = txtCodigo.getText().trim();
            String textoSuperficie = txtSuperficie.getText().trim()
                    .replace(",", ".");
            String textoHabitaciones = txtHabitaciones.getText().trim();

            // Validar código
            if (codigo.isEmpty()) {
                lblMensaje.setText("Error: el código es obligatorio.");
                return;
            }

            // Validar superficie
            double superficie;
            try {
                superficie = Double.parseDouble(textoSuperficie);
            } catch (NumberFormatException e) {
                lblMensaje.setText("Error: la superficie debe ser un número decimal.");
                return;
            }
            if (superficie < 20 || superficie > 500) {
                lblMensaje.setText("Error: la superficie debe estar entre 20 y 500 m².");
                return;
            }

            // Validar habitaciones
            int habitaciones;
            try {
                habitaciones = Integer.parseInt(textoHabitaciones);
            } catch (NumberFormatException e) {
                lblMensaje.setText("Error: las habitaciones deben ser un número entero.");
                return;
            }
            if (habitaciones <= 0) {
                lblMensaje.setText("Error: debe existir al menos una habitación.");
                return;
            }

            // Determinar el tipo de vivienda
            Vivienda nuevaVivienda;
            if ("Departamento".equals(cmbTipo.getValue())) {

                // Validar piso
                int piso;
                try {
                    piso = Integer.parseInt(txtPiso.getText().trim());
                } catch (NumberFormatException e) {
                    lblMensaje.setText("Error: el piso debe ser un número entero.");
                    return;
                }
                if (piso <= 0) {
                    lblMensaje.setText("Error: el piso debe ser mayor que cero.");
                    return;
                }

                // Crear departamento
                boolean gastoComunAlDia =
                        "Sí".equals(cmbGastoComun.getValue());
                boolean estacionamiento =
                        "Sí".equals(cmbEstacionamiento.getValue());
                Departamento departamento = new Departamento(
                        codigo,
                        superficie,
                        habitaciones,
                        piso,
                        gastoComunAlDia
                );

                // Asignar estacionamiento si corresponde
                if (estacionamiento) {
                    departamento.asignarEstacionamiento();
                }
                nuevaVivienda = departamento;

            } else if ("Casa".equals(cmbTipo.getValue())) {
                boolean tienePatio =
                        "Sí".equals(cmbPatio.getValue());
                nuevaVivienda = new Casa(
                        codigo,
                        superficie,
                        habitaciones,
                        tienePatio
                );

            } else {
                lblMensaje.setText("Error: debe seleccionar un tipo de vivienda.");
                return;
            }

            // Agregar vivienda a la lista
            listaViviendas.add(nuevaVivienda);

            // Guardar los cambios en los archivos JSON
            try {
                guardarDatos();
                lblMensaje.setText(
                        "Vivienda " + codigo + " registrada correctamente."
                );
                limpiarFormulario();
            } catch (IOException e) {

                // Si falla el guardado
                listaViviendas.remove(nuevaVivienda);
                lblMensaje.setText(
                        "Error al guardar la vivienda: " + e.getMessage()
                );
            }
        } catch (IllegalArgumentException e) {
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtSuperficie.clear();
        txtHabitaciones.clear();
        txtPiso.clear();
        cmbGastoComun.setValue("No");
        cmbEstacionamiento.setValue("No");
        cmbPatio.setValue("No");
        cmbTipo.setValue("Departamento");
        cambiarTipo();
    }

    private void guardarDatos() throws IOException {
        repository.guardarDepartamentos(listaViviendas);
        repository.guardarCasas(listaViviendas);
    }

    @FXML
    private void eliminarVivienda() {

        // Obtener la vivienda seleccionada
        Vivienda viviendaSeleccionada =
                tblViviendas.getSelectionModel().getSelectedItem();

        // Comprobar si existe una selección
        if (viviendaSeleccionada == null) {
            lblMensaje.setText(
                    "Debe seleccionar una vivienda para eliminar."
            );
            return;
        }

        // Guardar la posición original por si ocurre un error
        int posicion = listaViviendas.indexOf(viviendaSeleccionada);

        // Eliminar de la lista
        listaViviendas.remove(viviendaSeleccionada);

        // Guardar los cambios
        try {
            guardarDatos();
            lblMensaje.setText(
                    "Vivienda " + viviendaSeleccionada.getCodigo()
                            + " eliminada correctamente."
            );
        } catch (IOException e) {

            // Recuperar la vivienda si falla el guardado
            listaViviendas.add(posicion, viviendaSeleccionada);
            lblMensaje.setText(
                    "Error al guardar la eliminación: " + e.getMessage()
            );
        }
    }
}
