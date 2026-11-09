package cl.duoc.viviendas.repository;
import cl.duoc.viviendas.model.Vivienda;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Casa;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import cl.duoc.viviendas.dao.ViviendaDAO;
import cl.duoc.viviendas.dao.ViviendaDAOImpl;

public class ViviendaRepository {

private final ViviendaDAO dao = new ViviendaDAOImpl();

public void guardarDepartamentos(List<Vivienda> viviendas)
        throws IOException {

    List<Departamento> departamentos = viviendas.stream()
            .filter(v -> v instanceof Departamento)
            .map(v -> (Departamento) v)
            .toList();

    dao.guardarDepartamentos(departamentos);
}

public void guardarCasas(List<Vivienda> viviendas)
        throws IOException {

    List<Casa> casas = viviendas.stream()
            .filter(v -> v instanceof Casa)
            .map(v -> (Casa) v)
            .toList();

    dao.guardarCasas(casas);
}

public List<Departamento> cargarDepartamentos()
        throws IOException {

    return dao.cargarDepartamentos();
}

public List<Casa> cargarCasas() throws IOException {

    return dao.cargarCasas();
}

    public List<Vivienda> cargarTodas()
            throws IOException {
        List<Vivienda> viviendas = new ArrayList<>();
        viviendas.addAll(cargarDepartamentos());
        viviendas.addAll(cargarCasas());
        return viviendas;
    }
}