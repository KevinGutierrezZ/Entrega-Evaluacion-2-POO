package cl.duoc.viviendas.dao;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Casa;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ViviendaDAOImpl implements ViviendaDAO {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final Path archivoDepartamentos =
            Path.of("departamentos.json");

    private final Path archivoCasas =
            Path.of("casas.json");

    @Override
    public void guardarDepartamentos(List<Departamento> departamentos)
            throws IOException {

        try (Writer writer = Files.newBufferedWriter(archivoDepartamentos)) {
            gson.toJson(departamentos, writer);
        }
    }

    @Override
    public void guardarCasas(List<Casa> casas)
            throws IOException {

        try (Writer writer = Files.newBufferedWriter(archivoCasas)) {
            gson.toJson(casas, writer);
        }
    }

    @Override
    public List<Departamento> cargarDepartamentos()
            throws IOException {
        if (!Files.exists(archivoDepartamentos)) {
            return new ArrayList<>();
        }
        Type tipoLista = new TypeToken<List<Departamento>>() {}.getType();
        try (Reader reader = Files.newBufferedReader(archivoDepartamentos)) {
            List<Departamento> departamentos =
                    gson.fromJson(reader, tipoLista);
            if (departamentos == null) {
                return new ArrayList<>();
            }
            return departamentos;
        }
    }

    @Override
    public List<Casa> cargarCasas() throws IOException {
        if (!Files.exists(archivoCasas)) {
            return new ArrayList<>();
        }
        Type tipoLista = new TypeToken<List<Casa>>() {}.getType();
        try (Reader reader = Files.newBufferedReader(archivoCasas)) {
            List<Casa> casas = gson.fromJson(reader, tipoLista);
            if (casas == null) {
                return new ArrayList<>();
            }
            return casas;
        }
    }
}