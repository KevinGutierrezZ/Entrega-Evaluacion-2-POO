package cl.duoc.viviendas.dao;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Casa;
import java.io.IOException;
import java.util.List;

public interface ViviendaDAO {
    void guardarDepartamentos(List<Departamento> departamentos) throws IOException;
    void guardarCasas(List<Casa> casas) throws IOException;
    List<Departamento> cargarDepartamentos() throws IOException;
    List<Casa> cargarCasas() throws IOException;

}